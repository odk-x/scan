#include "StatCollector.h"
#include "Addons.h"
#include "FileUtils.h"

#include <iostream>
#include <fstream>

#include <opencv2/core/core.hpp>

#define DEBUG 0
//#define PRINT_MISS_LOCATIONS

using namespace std;
using namespace cv;

Json::Value getCValue(const Json::Value& classification){
	if(classification.isObject()){
		return classification.get("value", false);
	} else {
		return classification;
	}
}
//Iterates over a field's segments and items to determine it's value.
//This is a copy of the function in processor.cpp
Json::Value computeFieldValueCopy(const Json::Value& field){
	Json::Value output;
	const Json::Value segments = field["segments"];
	for ( size_t i = 0; i < segments.size(); i++ ) {
		const Json::Value segment = segments[i];
		const Json::Value items = segment["items"];
		if( items.isNull() ){
			//If the segment already has a value,
			//probably from a qrcode, use that.
			return segment.get("value", Json::Value());
		}
		for ( size_t j = 0; j < items.size(); j++ ) {
			const Json::Value classification = items[j].get("classification", false);
			Json::Value cValue;
			if(classification.isObject()){
				cValue = classification.get("value", false);
			} else {
				cValue = classification;
			}
			Json::Value itemValue = items[j]["value"];
			switch ( cValue.type() )
			{
				case Json::stringValue:
					//This case isn't used right now.
					//It's for classifiers that picks up letters.
					//The idea is to concatenate all the letters into a word.
					output = Json::Value(output.asString() +
						             cValue.asString());
				break;
				case Json::booleanValue:
					if(!itemValue.isNull()){
						if(!itemValue.isString()) {
							//Hack to convert ints to strings.
							string s = itemValue.toStyledString();
							itemValue = Json::Value(s.substr(0, s.length() - 1));
						}
						//This case is for selects.
						//The values of the filled (i.e. true) items
						//are stored in a space delimited string.
						if(cValue.asBool()){
							if( output.asString().length() == 0 ){
								output = Json::Value(itemValue.asString());
							}
							else{
								output = Json::Value(output.asString() +
									     field.get("delimiter", " ").asString() +
									     itemValue.asString());
							}
						}
						else {
							//Set the output to be a string.
							//If this is not done, we get a null when no bubbles are selected.
							output = Json::Value(output.asString());
						}
						break;
					}
					//Fall through and count the boolean as a 1 or 0
					//for a tally.
				case Json::intValue:
				case Json::uintValue:
					output = Json::Value(output.asInt() + cValue.asInt());
				break;
				case Json::realValue:
					output = Json::Value(output.asDouble() + cValue.asDouble());
				break;
				default:
				break;
			}
		}
	}
	return output;
}

//Compares 2 segments
//returns false if the found segment wasn't found
void StatCollector::compareItems(const Json::Value& foundSeg, const Json::Value& actualSeg){
	#if DEBUG > 0
	cout << "Comparing segments..." << endl;
	#endif
	
	numSegments++;
	if( foundSeg.get("notFound", false).asBool() ) {
		missedSegments++;
		//return;
	}
	
	const Json::Value foundItems = foundSeg["items"];
	const Json::Value actualItems = actualSeg["items"];

	if( foundItems.size() != actualItems.size()) {
		cout << "found" << foundItems << endl;
		cout << "actual" << actualItems << endl;
		throw new Exception();
	}

	for( size_t i = 0; i < foundItems.size(); i++){
		bool found = getCValue(foundItems[i]["classification"]).asBool();
		bool actual = getCValue(actualItems[i]["classification"]).asBool();
		
		if(found && actual){
			tp++;
			#ifdef PRINT_MISS_LOCATIONS
			cout << "true positive at:" << endl;
			cout << "\t" << foundItems[i]["absolute_location"][0u].asInt() << ", " << foundItems[i]["absolute_location"][1u].asInt() << endl;
			#endif
		}
		else if(found && !actual){
			#ifdef PRINT_MISS_LOCATIONS
			cout << "false positive at:" << endl;
			cout << "\t" << foundItems[i]["absolute_location"][0u].asInt() << ", " << foundItems[i]["absolute_location"][1u].asInt() << endl;
			#endif
			fp++;
		}
		else if(!found && actual){
			#ifdef PRINT_MISS_LOCATIONS
			cout << "false negative at:" << endl;
			cout << "\t" << foundItems[i]["absolute_location"][0u].asInt() << ", " << foundItems[i]["absolute_location"][1u].asInt() << endl;
			#endif
			fn++;
		}
		else{
			tn++;
			#ifdef PRINT_MISS_LOCATIONS
				cout << "true negative at:" << endl;
				cout << "\t" << foundItems[i]["absolute_location"][0u].asInt() << ", " << foundItems[i]["absolute_location"][1u].asInt() << endl;
			#endif
		}
	}
}
void StatCollector::compareFields(const Json::Value& foundField, const Json::Value& actualField, ComparisonMode mode){
	#if DEBUG > 0
	cout << "Comparing fields..." << endl;
	#endif
	const Json::Value fSegments = foundField["segments"];
	const Json::Value aSegments = actualField["segments"];

	assert( fSegments.size() == aSegments.size());

	for( size_t i = 0; i < fSegments.size(); i++){

		if(mode == COMP_BUBBLE_VALS){
			compareItems(fSegments[i], aSegments[i]);
		}

	}

	const Json::Value fValue = computeFieldValueCopy(foundField);
	const Json::Value aValue = computeFieldValueCopy(actualField);
	if(aValue.isNull()){
		assert(!aSegments[size_t(0)].isMember("items"));
		assert(!fSegments[size_t(0)].isMember("items"));
		//cout << "null field: " << actualField << endl;
		return;
	}
	if(fValue == aValue){
		//cout << "correctFields: " << fValue << aValue << endl;
		correctFields++;
	} else {
		//cout << "incorrectFields: " << fValue << aValue << endl;
		incorrectFields++;
	}
	if(aValue.type() == Json::intValue){
		histogram[abs(fValue.asInt() - aValue.asInt())]++;
	}
}
void StatCollector::compareFiles(const string& foundPath, const string& actualPath, ComparisonMode mode){
	Json::Value foundRoot, actualRoot;

	parseJsonFromFile(foundPath.c_str(), foundRoot);
	parseJsonFromFile(actualPath.c_str(), actualRoot);
	#if DEBUG > 0
	cout << "Files parsed." << endl;
	#endif
	
	const Json::Value fFields = foundRoot["fields"];
	const Json::Value aFields = actualRoot["fields"];
	/*
	Note that we expect unique labels for this to work, and that unlabeled0 and unlabeled1 are reserved labels.
	*/
	for( size_t i = 0; i < fFields.size(); i++){
		const Json::Value fFieldLabel = fFields[i].get("name", "unlabeled0");

		//Omit select fields that are only meant to be select field in Collect.
		if(fFieldLabel == "provincia") continue;
		if(fFieldLabel == "distrito") continue;
		if(fFieldLabel == "communidade") continue;
		if(fFieldLabel == "APE_name") continue;
		if(fFieldLabel == "mes") continue;
		if(fFieldLabel == "ano") continue;

		for( size_t j = 0; j < aFields.size(); j++){
			const Json::Value aFieldLabel = aFields[j].get("name", "unlabeled1");
			//unlabeled0 does not match unlabeled1, thus those fields are ignored.
			if(fFieldLabel == aFieldLabel) {
				compareFields(fFields[i], aFields[j], mode);
			}
		}
	}
}
void StatCollector::recomputeFieldValues(const string& inpath, const string& outpath) const{
	Json::Value foundRoot;
	parseJsonFromFile(inpath.c_str(), foundRoot);

	Json::Value fFields = foundRoot["fields"];
	Json::Value outFields;

	for( size_t i = 0; i < fFields.size(); i++){
		Json::Value field = fFields[i];
		Json::Value fFieldLabel = fFields[i].get("name", "unlabeled0");

		if(fFieldLabel == "provincia") continue;
		if(fFieldLabel == "distrito") continue;
		if(fFieldLabel == "communidade") continue;
		if(fFieldLabel == "APE_name") continue;
		if(fFieldLabel == "mes") continue;
		if(fFieldLabel == "ano") continue;

		Json::Value computedValue = computeFieldValueCopy(field);

		if(computedValue.isNull()) continue;


		field["value"] = computedValue;

		fFields[i] = field;
	}

	foundRoot["fields"] = fFields;

	//Create the json output file
	ofstream outfile(outpath.c_str(), ios::out | ios::binary);
	outfile << foundRoot << endl;
	outfile.close();
}
double vecSum(vector <double> v){
	double sum = 0;
	for(size_t i = 0; i < v.size(); i++){
		sum += v[i];
	}
	return sum;
}
void StatCollector::print(ostream& myOut) const{

	myOut << linebreak << endl << endl;
	
	if(numImages > 0){
		myOut << "Form alignment stats: "<< endl;
		myOut << "Errors: " << errors << endl;
		myOut << "Images Tested: " << numImages << endl;
		myOut << "Percent Success: " << 100.f * formAlignmentRatio() << "%" << endl;
		if(!offsets.empty()){
			myOut << "Accuracy\n(Differences of found bubble positions from expected bubble positions): " << endl;
			Scalar mean, stddev;
			meanStdDev(Mat(offsets), mean, stddev);
			myOut << "Mean:" << norm(mean) << "\t\t" << "Std. Deviation:" << norm(stddev) << endl;
		}
		if(numSegments > 0){
			myOut << "\tSegment alignment stats for successful form alignments: "<< endl;
			myOut << "\tMissed Segments: " << missedSegments << endl;
			myOut << "\tSegments Attempted: " << numSegments << endl;
			myOut << "\tPercent Success: " << 100.f * segmentAlignmentRatio() << "%" << endl;
			myOut << "\t\tBubble classification stats for successful form alignments: "<< endl;
		}
	}
	else{
		myOut << "\t\tBubble classification stats: "<< endl;
	}
	
	myOut << "\t\tTrue positives: "<< tp << endl;
	myOut << "\t\tFalse positives: " << fp << endl;
	myOut << "\t\tTrue negatives: "<< tn << endl;
	myOut << "\t\tFalse negatives: " << fn << endl;
	myOut << "\t\tPercent Correct: " << 100.f * correctClassificationRatio() << "%" << endl;
	
	myOut << "Correct fields: " << correctFields << endl;
	myOut << "Incorrect fields: " << incorrectFields << endl;

	int incorrectNumericFields = 0;
	int correctNumericFields = 0;
	std::map<int, int>::const_iterator mapita;
	for ( mapita=histogram.begin() ; mapita != histogram.end(); mapita++ ){
		if((*mapita).first == 0) {
			correctNumericFields = (*mapita).second;
		} else {
			incorrectNumericFields += (*mapita).second;
		}
	}

	myOut << "Correct numeric fields: " << correctNumericFields << endl;
	myOut << "Incorrect numeric fields: " << incorrectNumericFields << endl;

	myOut << "Percent correct fields: " << (100.f * correctFields) / (correctFields + incorrectFields) << endl;
/*
	if(numImages > 0){
		myOut << endl << "Total success rate: " << 100.f *
		                                           (numImages > 0 ? formAlignmentRatio() : 1.0) *
		                                           (numSegments > 0 ? segmentAlignmentRatio() : 1.0) *
		                                           correctClassificationRatio() << "%" << endl;
	}
*/
	myOut << "Average image processing time: "<< vecSum(times) / times.size() << " seconds" << endl;

	myOut << "Numeric field error histogram:" << endl;
	std::map<int, int>::const_iterator mapit;
	for ( mapit=histogram.begin() ; mapit != histogram.end(); mapit++ ){
		myOut << (*mapit).first << ", " << (*mapit).second << endl;
	}


	myOut << linebreak << endl;
}
void StatCollector::printHistRows(const string& condition, ostream& myOut) const{
	//header: myOut << "countDifference, numberOfFields, condition" << endl;
	std::map<int, int>::const_iterator mapit;
	for ( mapit=histogram.begin() ; mapit != histogram.end(); mapit++ ){
		myOut << (*mapit).first << ", " << (*mapit).second << ", " << condition << endl;
	}
}

void StatCollector::printAsRow(ostream& myOut) const{

	if(numImages <= 0 || offsets.empty() || numSegments <= 0){
		myOut << "error" << endl;
		return;
	}

	//Form Alignment
	myOut << numImages - errors << ", " << errors << ", " << formAlignmentRatio() << ", ";
	
	//Segment Alignment
	myOut << numSegments - missedSegments << ", " << missedSegments << ", " << segmentAlignmentRatio() << ", ";

	//Bubble Classification
	myOut << fn << ", " << fp << ", " << tn << ", " << tp << ", " << correctClassificationRatio();
	myOut << endl;
}

ostream& operator<<(ostream& os, const StatCollector& sc){
	sc.print(os);
	return os;
}
