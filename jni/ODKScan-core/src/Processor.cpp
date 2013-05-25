/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
#include "configuration.h"
#include "Processor.h"
#include "FileUtils.h"
#include "PCA_classifier.h"
#include "Aligner.h"
#include "SegmentAligner.h"
#include "AlignmentUtils.h"
#include "Addons.h"

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>

#include <json/json.h>

//QRCode stuff:
#include "ImageReaderSource.h"
#include <zxing/common/Counted.h>
#include <zxing/Binarizer.h>
#include <zxing/MultiFormatReader.h>
#include <zxing/Result.h>
#include <zxing/ReaderException.h>
//#include <zxing/common/GlobalHistogramBinarizer.h>
#include <zxing/common/HybridBinarizer.h>
#include <exception>
#include <zxing/Exception.h>
#include <zxing/common/IllegalArgumentException.h>
#include <zxing/BinaryBitmap.h>
#include <zxing/DecodeHints.h>
#include <zxing/qrcode/QRCodeReader.h>
#include <zxing/multi/qrcode/QRCodeMultiReader.h>
#include <zxing/multi/ByQuadrantReader.h>
//#include <zxing/multi/MultipleBarcodeReader.h>
//#include <zxing/multi/GenericMultipleBarcodeReader.h>

using namespace zxing;
using namespace zxing::multi;
using namespace zxing::qrcode;

#include <iostream>
#include <fstream>
#include <map>
#include <time.h>

// This sets the resolution of the form at which to perform segment alignment and classification.
// It is a percentage of the size specified in the template.
#define SCALEPARAM 1.0

// Creates a buffer around segments proportional to their size.
// I think .5 is the largest value that won't cause ambiguous cases.
#define SEGMENT_BUFFER .25

#ifdef OUTPUT_BUBBLE_IMAGES
	#include "NameGenerator.h"
	NameGenerator namer;
#endif

using namespace std;
using namespace cv;

//Serialize the JSON into an unstyled string.
//See also: Json::Value::toStyledString
const string stringify(const Json::Value& theJson){
	stringstream ss;
	Json::FastWriter writer;
	ss << writer.write( theJson );
	return ss.str();
}
//Iterates over a field's segments and items to determine it's value.
Json::Value computeFieldValue(const Json::Value& field){
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
//Generate a marked up version of the form image that shows classifications and values.
Mat markupForm(const Json::Value& bvRoot, const Mat& inputImage, bool drawCounts) {
	Mat markupImage;
	cvtColor(inputImage, markupImage, CV_GRAY2RGB);
	const Json::Value fields = bvRoot["fields"];
	for ( size_t i = 0; i < fields.size(); i++ ) {
		const Json::Value field = fields[i];
		
		float avgWidth = 0;
		float avgY = 0;
		int endOfField = 0;

		Scalar boxColor = getColor(int(i));
	
		const Json::Value segments = field["segments"];
		
		for ( size_t j = 0; j < segments.size(); j++ ) {
			const Json::Value segment = segments[j];

			//Draw segment rectangles:
			vector<Point> quad = orderCorners( jsonArrayToQuad(segment["quad"]) );
			const Point* p = &quad[0];
			int n = (int) quad.size();
			polylines(markupImage, &p, &n, 1, true, boxColor, 1, CV_AA);
			
			if( segment.get("notFound", false).asBool() ) {
				polylines(markupImage, &p, &n, 1, true, .25 * boxColor, 1, CV_AA);
			}
			else{
				polylines(markupImage, &p, &n, 1, true, boxColor, 1, CV_AA);
			}
			
			//Compute some stuff to figure out where to draw output on the form
			avgWidth += norm(quad[0] - quad[1]);
			if(endOfField < quad[1].x){
				endOfField = quad[1].x;
			}
			avgY += quad[3].y;// + quad[1].y + quad[2].y + quad[3].y) / 4;
			

			const Json::Value items = segment["items"];
			for ( size_t k = 0; k < items.size(); k++ ) {
				const Json::Value Item = items[k];
				Point ItemLocation(jsonToPoint(Item["absolute_location"]));
				Json::Value classification = Item["classification"];
				Json::Value cValue = classification["value"];
				double confidence = abs(classification.get("confidence", 1.0).asDouble()) / 2.0;
				
				if(cValue.isBool()){
					circle(markupImage, ItemLocation, 2, 	getColor(cValue.asBool()) * confidence, 1, CV_AA);
				}
				else if(cValue.isInt()){
					circle(markupImage, ItemLocation, 2, 	getColor(cValue.asInt()) * confidence, 1, CV_AA);
				}
				else if(cValue.isString()){

					putText(markupImage, cValue.asString(), ItemLocation + Point(0, -10),
						FONT_HERSHEY_SIMPLEX, 0.8, Scalar::all(0), 3, CV_AA);
					putText(markupImage, cValue.asString(), ItemLocation + Point(0, -10),
						FONT_HERSHEY_SIMPLEX, 0.8, boxColor, 2, CV_AA);
				}
				else{
					cout << "Don't know what this is: " << cValue << endl;
				}
			}
		}
		if(field.isMember("value")){
			Point textBoxTL;
			if(drawCounts && avgWidth > 0){
				avgWidth /= segments.size();
				avgY /= segments.size();
				textBoxTL = Point(endOfField + 5, (int)avgY - 5);
			}
			if(field.isMember("markup_location")){
				textBoxTL = Point(field["markup_location"]["x"].asInt(), field["markup_location"]["y"].asInt());
			}
			stringstream ss;
			ss << field.get("value", "");
			string markupString(ss.str());
			markupString = markupString.substr(0, markupString.length() - 1);
			putText(markupImage, markupString, textBoxTL,
			        FONT_HERSHEY_SIMPLEX, 0.8, Scalar::all(0), 3, CV_AA);
			putText(markupImage, markupString, textBoxTL,
			        FONT_HERSHEY_SIMPLEX, 0.8, boxColor, 2, CV_AA);
		}
	}
	return markupImage;
}
//deprecated
Json::Value minifyJsonOutput(const Json::Value& JsonOutput){
	Json::Value minifiedOutput;
	Json::Value minifiedOutputFields;
	const Json::Value fields = JsonOutput["fields"];
	for ( size_t i = 0; i < fields.size(); i++ ) {
		Json::Value minifiedOutputField;
		Json::Value minifiedOutputSegments;
		const Json::Value field = fields[i];
		const Json::Value segments = field["segments"];
		for ( size_t j = 0; j < segments.size(); j++ ) {
			Json::Value minifiedOutputSegment;
			const Json::Value segment = segments[j];
			minifiedOutputSegment["image_path"] = segment["image_path"];
			minifiedOutputSegments.append(minifiedOutputSegment);
		}
		minifiedOutputField["segments"] = minifiedOutputSegments;
		if ( field.isMember("value") ) {
			minifiedOutputField["value"] = field["value"];
		}
		minifiedOutputField["name"] = field["name"];
		minifiedOutputFields.append(minifiedOutputField);
	}
	minifiedOutput["fields"] = minifiedOutputFields;
	return minifiedOutput;
}
// Get current date/time, format is YYYY-MM-DD.HH:mm:ss
// src: http://stackoverflow.com/questions/997946/c-get-current-time-and-date
const std::string currentDateTime() {
    time_t     now = time(0);
    struct tm  tstruct;
    char       buf[80];
    tstruct = *localtime(&now);
    // Visit http://www.cplusplus.com/reference/clibrary/ctime/strftime/
    // for more information about date/time format
    strftime(buf, sizeof(buf), "%Y-%m-%d.%X", &tstruct);
    return buf;
}
class Processor::ProcessorImpl
{
public:
	string trainingDataPath;
private:
	Mat formImage;

	Aligner aligner;

	Json::Value root;
	//Json::Value JsonOutput;

	typedef std::map<const std::string, cv::Ptr< PCA_classifier> > ClassiferMap;
	ClassiferMap classifiers;

	string templPath;

	#ifdef TIME_IT
		clock_t init;
	#endif

//Creates a classifier based on the given JSON classifier specification.
//Classifiers are cached in memory via the "classifiers" map. 
//Trained classifier parameters are cached on the file system.
//Cached data is keyed by the classifier's training_data_uri and dimensions.
Ptr<PCA_classifier>& getClassifier(const Json::Value& classifier) {
	//NOTE: training_data_uri must be a directory with no leading or trailing slashes.
	const string& training_data_uri = classifier["training_data_uri"].asString();

	Size scaled_classifier_size = SCALEPARAM * Size(classifier["classifier_width"].asInt(),
	                                                classifier["classifier_height"].asInt());
	ostringstream ss;
	ss << scaled_classifier_size.height << 'x' << scaled_classifier_size.width;
	string key(training_data_uri + ss.str());

	ClassiferMap::iterator it = classifiers.find(key);

	//If the classifier is not loaded into memory...
	if( it == classifiers.end() ) {
		//PCA_classifier classifier = classifiers[key];
		classifiers[key] = Ptr<PCA_classifier>(new PCA_classifier);
		
		string dataPath = trainingDataPath + training_data_uri;
		string cachedDataPath = dataPath + "/cached_classifier_data_" + ss.str() + ".yml";

		classifiers[key]->set_classifier_params(classifier);

		try{
			//Uncomment this to always retrain.
			//throw new exception();
			classifiers[key]->load(cachedDataPath);
			#ifdef DEBUG_PROCESSOR
				cout << "found cached classifier..." << endl;
			#endif
		}
		catch(...){
			#ifdef DEBUG_PROCESSOR
				cout << "training new classifier..." << endl;
			#endif
			vector<string> filepaths;
			if(!fileExists(dataPath)){
				CV_Error( -1, "Could not find classifier data: " + dataPath);
			}
			CrawlFileTree(dataPath, filepaths);//TODO: Check if this dir exists and show a log message if not.
			
			/*
			#ifdef DEBUG_PROCESSOR
				cout << dataPath << endl;
				cout << acutal_classifier_size.width << endl;
				cout << filepaths.size() << endl;
				cout << classifiers.size() << endl;
			#endif
			*/

			//TODO: Move more of this code into the PCA classifier class.
			const Json::Value advanced = classifier.get("advanced", Json::Value());
			bool success = classifiers[key]->train_PCA_classifier( filepaths,
				                                               scaled_classifier_size,
				                                               advanced.get("eigenvalues", 9).asInt(),
			                                                       advanced.get("flip_training_data", true).asBool());
			if( !success ) {
				LOGI("\n\nCould not train classifier.\n\n");
				return classifiers[key];
			}

			classifiers[key]->save(cachedDataPath);

			#ifdef DEBUG_PROCESSOR
				cout << "trained" << endl;
			#endif
		}
	}
	return classifiers[key];
}
Json::Value segmentFunction(Json::Value& segmentJsonOut, const Json::Value& extendedSegment) {
	Mat segmentImg;
	vector <Point> segBubbleLocs;
	vector <int> bubbleVals;
	Rect segmentRect( SCALEPARAM * Point(extendedSegment.get( "segment_x", INT_MIN ).asInt(),
	                                     extendedSegment.get( "segment_y", INT_MIN ).asInt()),
	                                     SCALEPARAM * Size(extendedSegment.get("segment_width", INT_MIN).asInt(),
	                                                       extendedSegment.get("segment_height", INT_MIN).asInt()));
	//Transfromation and offest are used to get absolute bubble locations.
	Mat transformation = (Mat_<double>(3,3) << 1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 1.0);
	Point offset = segmentRect.tl();


		Rect expandedRect = resizeRect(segmentRect, 1 + SEGMENT_BUFFER);
	
		//Reduce the segment buffer if it goes over the image edge.
		if(expandedRect.x < 0){
			expandedRect.x = 0;
		}
		if(expandedRect.y < 0){
			expandedRect.y = 0;
		}
		if(expandedRect.br().x > formImage.cols){
			expandedRect.width = formImage.cols - expandedRect.x;
		}
		if(expandedRect.br().y > formImage.rows){
			expandedRect.height = formImage.rows - expandedRect.y;
		}

	//Get the cropped (transformed) segment image:
	//QR codes are not aligned because the interpolation
	//can actually make detection less reliable.
	if(extendedSegment.get("type", 0) == "qrcode"){
		//note that the expected ROI is used
		//because we don't want misalined qrcodes to be cut off
		//And for some reason detection is more reliable when there is a large margin.
		segmentImg = formImage(expandedRect);
		segmentJsonOut["quad"] = quadToJsonArray(rectToQuad( segmentRect ));
	}
	else if(extendedSegment.get("align_segment", true).asBool()) {
		segmentImg = formImage(expandedRect);

		vector<Point2f> quad;
		findSegment(segmentImg, segmentRect - expandedRect.tl(), quad);

		#define MAX_SIZE_VARIATION 0.4
		if(testQuad(quad, segmentRect, MAX_SIZE_VARIATION)){
			#ifdef DEBUG_PROCESSOR
				//This makes a stream of dots so we can see how fast things are going.
				//we get a ! when things go wrong.
				cout << '.' << flush;
			#endif
			transformation = quadToTransformation(quad, segmentRect.size());
			offset = expandedRect.tl();
			Mat alignedSegment(0, 0, CV_8U);
			//debugShow(formImage(segmentRect));
			warpPerspective(segmentImg, alignedSegment, transformation, segmentRect.size());//debugShow(alignedSegment);
			segmentImg = alignedSegment;
			segmentJsonOut["quad"] = quadToJsonArray( quad, offset );
		}
		else{
			#ifdef DEBUG_PROCESSOR
				//Bad quad alignment detected
				cout << "!" << flush;
			#endif
			//segmentJsonOut["quad"] = quadToJsonArray(quad, expandedRect.tl());
			segmentJsonOut["notFound"] = true;
			//If the quad is not found we don't do segment alignment and go with what's
			//in the template even though there's a good chance it's wrong.
			segmentImg = formImage(segmentRect);
			segmentJsonOut["quad"] = quadToJsonArray(rectToQuad( segmentRect ));
		}
	}
	else {
		segmentImg = formImage(segmentRect);
		segmentJsonOut["quad"] = quadToJsonArray(rectToQuad( segmentRect ));
	}

	//Do classification stuff:
	Json::Value items = extendedSegment["items"];
	if(!items.isNull()){
		Json::Value itemsJsonOut;
		Json::Value classifierJson = extendedSegment["classifier"];
		double alignment_radius = classifierJson.get("alignment_radius", 2.0).asDouble();
		Ptr<PCA_classifier> classifier = getClassifier(classifierJson);
		vector<Point> locations;
		vector<Point> deltas;
		//Align items:
		for (size_t i = 0; i < items.size(); i++) {
			Point initLocation = SCALEPARAM * Point(items[i]["item_x"].asDouble(), items[i]["item_y"].asDouble());
			Point itemLocation = initLocation;
			if(alignment_radius > .1){
				itemLocation = classifier->align_item(segmentImg, initLocation, alignment_radius);
			}
			locations.push_back(itemLocation);
			deltas.push_back(itemLocation - initLocation);

		}

		//Catch runaway items:
		Point avgDelta = Point(0,0);
		for (size_t i = 0; i < deltas.size(); i++) {
			avgDelta += deltas[i];
		}
		avgDelta *= 1.0 / items.size();

		for (size_t i = 0; i < locations.size(); i++) {
			//Draw a circle centered at the average delta, with the radius porportional to the alignment_radius.
			//If the bubble's delta does not fall within that cirlce it is a runaway.
			if(norm(deltas[i] - avgDelta) >  (alignment_radius / 2)){
				locations[i] = locations[i] - deltas[i] + avgDelta;
			}
		}

		//Classify items
		for (size_t i = 0; i < items.size(); i++) {
			Json::Value itemJsonOut = items[i];
			itemJsonOut["classification"] = classifier->classify_item(segmentImg, locations[i]);
			/*
			Point rectOffset = segmentRect.tl() - expandedRect.tl();
			Mat absoluteLocation = transformation.inv() * Mat(Point3d( locations[i].x  + rectOffset.x,
					locations[i].y  + rectOffset.y, 1.0)) + Mat(Point3d(-rectOffset.x, -rectOffset.y, 0));
			*/
			Mat absoluteLocation = transformation.inv() * Mat(Point3d( locations[i].x,
					locations[i].y, 1.0));
			itemJsonOut["absolute_location"] = pointToJson(
				Point( absoluteLocation.at<double>(0u,0u) / absoluteLocation.at<double>(2, 0u),
				       absoluteLocation.at<double>(1,0u) / absoluteLocation.at<double>(2, 0u)) +
				offset);

			itemsJsonOut.append(itemJsonOut);
		}
		segmentJsonOut["items"] = itemsJsonOut;
	}  else if(extendedSegment.get("type", 0) == "qrcode"){
		LOGI("scanning qr code...");
		
		//Blowing up the image can make decoding work sometimes,
		//but making it too big can also break it. AFAICT 2x is optimal.
		Mat largeSegment;
		resize(segmentImg, largeSegment, 2*segmentImg.size());
		
		/*
		Mat tmp;
		//Add a border can also make decoding work sometimes.
		int borderSize = 40;
		copyMakeBorder( segmentImg, tmp, borderSize, borderSize, borderSize, borderSize, BORDER_CONSTANT, 0 );
		debugShow(tmp);
		*/
		//I modifieid this zxing class to take OpenCV mats
		Ref<LuminanceSource> source = ImageReaderSource::create(largeSegment);

		string result;
		try {
			Ref < Binarizer > binarizer;
			binarizer = new HybridBinarizer(source);
			DecodeHints hints(DecodeHints::DEFAULT_HINT);
			//Not sure if this makes any difference.
			hints.setTryHarder(true);
			Ref < BinaryBitmap > binary(new BinaryBitmap(binarizer));
			Ref<Reader> reader(new MultiFormatReader);
			vector<Ref<Result> > results(1, reader->decode(binary, hints));
			//LOGI("decoded!");
			result = results[0]->getText()->getText();
		} catch (const ReaderException& e) {
			//This happens when the qrcode is not recognized.
			result = string(e.what());
		} catch (const zxing::IllegalArgumentException& e) {
			result = "zxing::IllegalArgumentException: "
					+ string(e.what());
		} catch (const zxing::Exception& e) {
			result = "zxing::Exception: " + string(e.what());
		} catch (const std::exception& e) {
			result = "std::exception: " + string(e.what());
		}
		segmentJsonOut["value"] = result;
		cout << "qrcode: " << result << endl;
	}
	
	//Output the segment image:
	Mat segment_out, tmp;
	cvtColor(segmentImg, segment_out, CV_GRAY2RGB);
	resize(segment_out, tmp, 2*segment_out.size());
	segment_out = tmp;

	string segmentOutPath;
	string segmentName;
	try{
		segmentOutPath = extendedSegment.get("output_path", 0).asString() + "segments/";
		segmentName = extendedSegment["name"].asString() + "_image_" +
		              intToStr(extendedSegment.get("index", 0).asInt()) + ".jpg";

		/*
		rectangle(segment_out, expectedBubbleLocs[i] - .5 * classifier_size,
		          expectedBubbleLocs[i] + .5 * classifier_size,
		          colors[bubbleVals[i]]);

		circle(segment_out, segBubbleLocs[i], 1, Scalar(255, 2555, 255), -1);
		*/

		imwrite(segmentOutPath + segmentName, segment_out);
		segmentJsonOut["image_path"] = segmentOutPath + segmentName;
	}
	catch(...){
		LOGI(("Could not output segment to: " + segmentOutPath + segmentName).c_str());
	}
	return segmentJsonOut;
}
Json::Value fieldFunction(const Json::Value& field, const Json::Value& parentProperties){
	Json::Value extendedField = Json::Value(parentProperties);
	extend(extendedField, field);
	const Json::Value segments = extendedField["segments"];
	Json::Value outField = Json::Value(field);
	Json::Value outSegments;

	//This field is just used to block out features from some region.
	if(field.get("mask", false).asBool()) {
		cout << "mask" << endl;
		return Json::Value();
	}

	#ifdef OUTPUT_BUBBLE_IMAGES
		//namer.setPrefix(field.get("classifier", Json::Value()).get("training_data_uri", "na").asString());
		namer.setPrefix(field.get("label", "unlabeled").asString());
	#endif

	for ( size_t j = 0; j < segments.size(); j++ ) {
		const Json::Value segment = segments[j];
		Json::Value segmentJsonOut(segment);
		segmentJsonOut["index"] = (int)j;

		Json::Value extendedSegment = Json::Value(extendedField);
		extend(extendedSegment, segmentJsonOut);

		segmentJsonOut = segmentFunction(segmentJsonOut, extendedSegment);

		if(!segmentJsonOut.isNull()){
			outSegments.append(segmentJsonOut);
		}
	}
	outField["segments"] = outSegments;

	Json::Value value = computeFieldValue(outField);
	if(!value.isNull()){
		outField["value"] = value;
	}
	outField.removeMember("fields");
	outField.removeMember("items");
	outField.removeMember("classifier");
	return outField;
}
Json::Value formFunction(const Json::Value& templateRoot){
	const Json::Value fields = templateRoot["fields"];
	Json::Value outForm = Json::Value(templateRoot);
	Json::Value outFields;
	for ( size_t i = 0; i < fields.size(); i++ ) {
		const Json::Value field = fields[i];
		Json::Value outField = fieldFunction(field, templateRoot);
		if(!outField.isNull()){
			outFields.append(outField);
		}
	}
	outForm["fields"] = outFields;
	outForm["form_scale"] = SCALEPARAM;
	outForm["timestamp"] = currentDateTime();
	outForm.removeMember("items");
	outForm.removeMember("classifier");
	return outForm;
}

public:

ProcessorImpl()  {}

bool setTemplate(const char* templatePathArg) {
	#ifdef DEBUG_PROCESSOR
		cout << "setting template..." << endl;
	#endif
	//TODO: Remove templPath variable?
	templPath = addSlashIfNeeded(templatePathArg);
	bool success = parseJsonFromFile(templPath + "template.json", root);
	//root["template_path"] = templPath;
	return success;
}
bool loadFormImage(const char* imagePath, const char* calibrationFilePath) {
	#ifdef DEBUG_PROCESSOR
		LOGI("loading form image...");
		cout << flush;
	#endif
	#ifdef TIME_IT	
		init = clock();
	#endif
	Mat temp;
	
	formImage = imread(imagePath, 0);
	if(formImage.empty()) return false;
	
	//Automatically rotate90 if the image is wider than it is tall
	//We want to keep the orientation consistent because:
	//1. It seems to make a slight difference in alignment. (SURF is not *completely* rotation invariant)
	//2. Undistortion
	//Maybe need to watch out incase a clock-wise rotation results in upsidown photos on some phones.
	if(formImage.cols > formImage.rows){
		transpose(formImage, temp);
		flip(temp,formImage, 1);
	}

	if(calibrationFilePath){
		Mat cameraMatrix, distCoeffs;
		Mat map1, map2;
		Size imageSize = formImage.size();
		
		//string calibPathString(calibrationFilePath);
		
		if( !fileExists(calibrationFilePath) ) return false;
		
		FileStorage fs(calibrationFilePath, FileStorage::READ);
		fs["camera_matrix"] >> cameraMatrix;
		fs["distortion_coefficients"] >> distCoeffs;
		
		initUndistortRectifyMap(cameraMatrix, distCoeffs, Mat(),
		                        getOptimalNewCameraMatrix(cameraMatrix,
		                                                  distCoeffs, imageSize, 0, imageSize, 0),
		                        imageSize, CV_16SC2, map1, map2);

		remap(formImage, temp, map1, map2, INTER_LINEAR);
		formImage = temp;
	}
/*
	string path = string(imagePath);
	imageDir = path.substr(0, path.find_last_of("/") + 1);
*/	
	#ifdef TIME_IT
		LOGI("LoadFormImage time: ");
		ostringstream ss;
		ss << (double)(clock()-init) / ((double)CLOCKS_PER_SEC);
		LOGI( ss.str().c_str() );
	#endif
	#ifdef DEBUG_PROCESSOR
		cout << "loaded" << endl;
	#endif
	return true;
}
bool alignForm(const char* alignedImageOutputPath, size_t formIdx) {
	#ifdef DEBUG_PROCESSOR
		LOGI("aligning form...");
		cout << endl;
	#endif
	#ifdef TIME_IT	
		init = clock();
	#endif
	Mat straightenedImage;

	Size form_sz(root.get("width", 0).asInt(), root.get("height", 0).asInt());

	if( form_sz.width <= 0 || form_sz.height <= 0) {
		CV_Error(CV_StsError, "Invalid form dimension in template.");
	}

	//If the image was not set (because form detection didn't happen) set it.
	if( aligner.currentImg.empty() ) aligner.setImage(formImage);

	//TODO: Move this try/catch into alignFormImage perhaps?
	try {
		aligner.alignFormImage( straightenedImage, SCALEPARAM * form_sz, formIdx );
	} catch(cv::Exception& e){
		return false;
	}

	formImage = straightenedImage;	
	//JsonOutput["aligned_image_path"] = alignedImageOutputPath;

	#ifdef TIME_IT
		LOGI("alignForm time: ");
		ostringstream ss;
		ss << (double)(clock()-init) / ((double)CLOCKS_PER_SEC);
		LOGI( ss.str().c_str() );
	#endif
	#ifdef DEBUG_PROCESSOR
		cout << "aligned" << endl;
	#endif
	return writeFormImage(alignedImageOutputPath);
}
bool processForm(const string& outputPath, const string& jsonOutputPath, const string& markedupImagePath, bool minifyJson) {
	#ifdef  DEBUG_PROCESSOR
		LOGI("Processing form");
		cout << endl;
	#endif
	#ifdef TIME_IT	
		init = clock();
	#endif
	
	if( !root || formImage.empty() ){
		cout << "Unable to process form. Error code: " <<
		        (int)!root << (int)formImage.empty() << endl;
		return false;
	}
	root["output_path"] = outputPath;
	const Json::Value JsonOutput = formFunction(root);

	#ifdef  DEBUG_PROCESSOR
		cout << "done" << endl;
	#endif
	
	#ifdef  DEBUG_PROCESSOR
		cout << "outputting bubble vals..." << endl;
	#endif
	
	//Create the marked up image:
	imwrite(markedupImagePath, markupForm(JsonOutput, formImage, true));
	
	//Create the json output file
	ofstream outfile(jsonOutputPath.c_str(), ios::out | ios::binary);
	if(minifyJson){
		Json::FastWriter writer;
		outfile << writer.write( minifyJsonOutput(JsonOutput) );
	}
	else{
		outfile << JsonOutput;
	}
	outfile.close();

	#ifdef TIME_IT
		LOGI("Process time: ");
		ostringstream ss;
		ss << (double)(clock()-init) / ((double)CLOCKS_PER_SEC);
		LOGI( ss.str().c_str() );
	#endif
	return true;
}
//Writes the form image to a file.
bool writeFormImage(const char* outputPath) {
	return imwrite(outputPath, formImage);
}
//Loads feature data for the given template into memory.
//This can be called multiple times, so multiple templates are loaded into memeory for detection.
bool loadFeatureData(const char* templatePathArg) {
	string templatePath = addSlashIfNeeded(templatePathArg);
	aligner.loadFeatureData(templatePath + "form.jpg",
	                        templatePath + "template.json",
	                        templatePath + "cached_features.yml");

	return true;
}
//Detect which form we are using from among the loaded feature data sets.
//A return value of 0 indicates the first data loaded, 1 indicates the second, etc..
int detectForm(){
	int formIdx;
	try{
		LOGI("Detecting form...");
		aligner.setImage(formImage);
		formIdx = (int)aligner.detectForm();
	}
	catch(cv::Exception& e){
		LOGI(e.what());
		return -1;
	}
	return formIdx;
}
};

/* This stuff hooks the Processor class up to the implementation class: */
Processor::Processor() : processorImpl(new ProcessorImpl()){
	processorImpl->trainingDataPath = "training_examples/";
	LOGI("Processor successfully constructed.");
}
Processor::Processor(const char* appRootDir) : processorImpl(new ProcessorImpl()){
	processorImpl->trainingDataPath = addSlashIfNeeded(appRootDir) + "training_examples/";
	LOGI("Processor successfully constructed.");
}
bool Processor::loadFormImage(const char* imagePath, const char* calibrationFilePath){
	return processorImpl->loadFormImage(imagePath, calibrationFilePath);
}
bool Processor::loadFeatureData(const char* templatePath){
	try{
		return processorImpl->loadFeatureData(templatePath);
	}
	catch(cv::Exception& e){
		LOGI(e.what());
		return false;
	}
}
int Processor::detectForm(){
	return processorImpl->detectForm();
}
bool Processor::setTemplate(const char* templatePath){
	return processorImpl->setTemplate(templatePath);
}
bool Processor::alignForm(const char* alignedImageOutputPath, int formIdx){
	if(formIdx < 0) return false;
	try{
		return processorImpl->alignForm(alignedImageOutputPath, (size_t)formIdx);
	}
	catch (...) {
	    return false;
	}
}
bool Processor::processForm(const char* outputPath, bool minifyJson) {
	try{
		string normalizedOutDir = addSlashIfNeeded(outputPath);
		return processorImpl->processForm(normalizedOutDir, normalizedOutDir + "output.json",
				normalizedOutDir + "markedup.jpg", minifyJson);
	}
	catch (...) {
	    return false;
	}
}
const string Processor::scanAndMarkup(const char* outputPath) {
	try{
		string normalizedOutDir = addSlashIfNeeded(outputPath);
		if(processorImpl->processForm(normalizedOutDir, normalizedOutDir + "output.json",
				normalizedOutDir + "markedup.jpg", false)) {
			return "";//success
		} else {
			return "Could not process form.";
		}
	}
	catch(cv::Exception& e){
		return e.what();
	}
	catch(std::exception& e){
		return e.what();
	}
	catch (...) {
	    return "Unknown expection.";
	}
}
/**
processViaJSON tries to  mimic a restful client-server interface.
It expects a JSON string like this:
{
	"inputImage" : "",
	"outputDirectory" : "",
	"alignedFormOutputPath" : outputDirectory + "aligned.jpg",
	"markedupFormOutputPath" : outputDirectory + "markedup.jpg",
	"jsonOutputPath" : outputDirectory + "output.json",
	"alignForm" : true,
	"processForm" : true,
	"templatePath" : "",
	"templatePaths" : [],
	"calibrationFilePath" : "",
	"trainingDataDirectory" : "training_examples/"
}
You only need to specify the inputImage, outputDirectory and templatePath(s).
The JSON above contains all the default values.

It returns a JSON string like this:
{
	"errorMessage" : "This is only here if there's an error",
	"templatePath" : "path/to/template/that/was/used/for/processing"
}

The benefits are as follows:
- Only a single call is required to use the whole processing pipeline
  (all the pipeline logic remains in here).
- Keyword arguments make it clearer what the args do,
  and allow for more flexible default behavior when they are not specified.
- Extra properties can be passed in without both ends supporting them in advance.
- The output JSON could be passed back this way without requiring use of the file system.
*/
const string Processor::processViaJSON(const char* jsonString) {
	Json::Value result(Json::objectValue);
	try {
		Json::Value config;// will contain the root value after parsing.
		Json::Reader reader;
		bool parsingSuccessful = reader.parse( jsonString, config );
		if(!parsingSuccessful){
			result["errorMessage"] = "Could not parse JSON configuration string.";
			return stringify(result);
		}
		if(!config.isMember("inputImage")){
			result["errorMessage"] = "Missing input image path.";
			return stringify(result);
		}
		if(!config.isMember("outputDirectory")){
			result["errorMessage"] = "Missing output image path.";
			return stringify(result);
		}
		string outputDirectory = config["outputDirectory"].asString();
		processorImpl->loadFormImage(config["inputImage"].asString().c_str(), config.get("calibrationFilePath", "").asString().c_str());

		int formIdx = 0;

		if(config.isMember("templatePath")){
			if(!processorImpl->loadFeatureData(config["templatePath"].asString().c_str())) {
				result["errorMessage"] = "Could not load feature data.";
				return stringify(result);
			}
			if(!processorImpl->setTemplate(config["templatePath"].asString().c_str())) {
				result["errorMessage"] = "Could not set template.";
				return stringify(result);
			}
			result["templatePath"] = config["templatePath"];
		} else if(config.isMember("templatePaths")) {
			Json::Value templatePaths = config["templatePaths"];
			for ( size_t j = 0; j < templatePaths.size(); j++ ) {
				const Json::Value templatePath = templatePaths[j];
				if(!processorImpl->loadFeatureData(templatePath.asString().c_str())) {
					result["errorMessage"] = "Could not load feature data.";
					return stringify(result);
				}
			}
			formIdx = processorImpl->detectForm();
			if(formIdx < 0) {
				result["errorMessage"] = "Could not detect form.";
				return stringify(result);
			}
			result["templatePath"] = templatePaths[formIdx];
			if(!processorImpl->setTemplate(templatePaths[formIdx].asString().c_str())) {
				result["errorMessage"] = "Could not set template.";
				return stringify(result);
			}
		} else {
			result["errorMessage"] = "One or more template paths are required.";
			return stringify(result);
		}

		if(config.get("alignForm", true).asBool()){
			string alignedFormOutputPath = config.get("alignedFormOutputPath",
					addSlashIfNeeded(outputDirectory) + "aligned.jpg").asString();
			if(!processorImpl->alignForm(alignedFormOutputPath.c_str(), (size_t)formIdx)){
				result["errorMessage"] = "Could not align form.";
				return stringify(result);
			}
		}
		if(config.get("processForm", true).asBool()){
			processorImpl->trainingDataPath = config.get("trainingDataDirectory", "training_examples/").asString();
			string normalizedOutDir = addSlashIfNeeded(outputDirectory);
			string jsonOutputPath = config.get("jsonOutputPath",
					normalizedOutDir + "output.json").asString();
			string markedupFormOutputPath = config.get("markedupFormOutputPath",
					normalizedOutDir + "markedup.jpg").asString();
			if(!processorImpl->processForm(normalizedOutDir, jsonOutputPath, markedupFormOutputPath, false)){
				result["errorMessage"] = "Could not process form.";
				return stringify(result);
			}
		}
	}
	catch(cv::Exception& e){
		result["errorMessage"] = e.what();
		return stringify(result);
	}
	catch(std::exception& e){
		result["errorMessage"] = e.what();
		return stringify(result);
	}
	catch (...) {
		result["errorMessage"] = "Unknown expection.";
		return stringify(result);
	}
	return stringify(result);
}
bool Processor::writeFormImage(const char* outputPath) const{
	return processorImpl->writeFormImage(outputPath);
}

const string Processor::jniEchoTest(const char* testStr) const{
	Json::Value result(Json::objectValue);
	result["stuff"] = testStr;
	return stringify(result);
}

