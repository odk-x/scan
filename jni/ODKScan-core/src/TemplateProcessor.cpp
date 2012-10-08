#include "TemplateProcessor.h"
#include <json/json.h>
#include <iostream>
#include <fstream>

using namespace std;

bool parseJsonFromFile(const string& filePath, Json::Value& myRoot) {
	ifstream JSONin;
	Json::Reader reader;
	
	JSONin.open(filePath.c_str(), ifstream::in);
	bool parse_successful = reader.parse( JSONin, myRoot );
	
	JSONin.close();
	if(parse_successful){
		return true;
	}
	else{
		cout << reader.getFormatedErrorMessages() << endl;
	}
	return false;
}
//inheritMembers makes the child value inherit the members that it does not override from the specified parent json value.
//The parent is copied so it can be written over, while the child is passed in and returned with added members by refrence.
Json::Value& TemplateProcessor::inheritMembers(Json::Value& child, Json::Value parent) const {
	Json::Value::Members members = child.getMemberNames();
	for( Json::Value::Members::iterator itr = members.begin() ; itr != members.end() ; itr++ ) {
		//cout << *itr << flush;
		//TODO: If inheriting a Json Object, perhaps I could recusively inherit:
		//parent[*itr] = inheritMembers(child[*itr], parent[*itr]);
		parent[*itr] = child[*itr];
	}
	//I'm worried that we end up with refrences to elements of the stack allocated parent copy.
	//However, I think operator= is overloaded so that this doesn't happen.
	return child = parent;
}

//XXX: If you override these, you should call the base class functions after your code to keep descending.
Json::Value TemplateProcessor::segmentFunction(const Json::Value& segment){
	//This will usually be the function to override
	//std::cout << "test" << std::endl;
	return Json::Value();
}
Json::Value TemplateProcessor::fieldFunction(const Json::Value& field){
	const Json::Value segments = field["segments"];
	Json::Value outfield;
	Json::Value outSegments;
	
	for ( size_t j = 0; j < segments.size(); j++ ) {
		Json::Value segment = segments[j];
		inheritMembers(segment, field);
		Json::Value outSegment = segmentFunction(segment);
		if(!outSegment.isNull()){
			outSegments.append(outSegment);
		}
	}

	outfield["segments"] = outSegments;
	return outfield;
}
Json::Value TemplateProcessor::formFunction(const Json::Value& templateRoot){
	const Json::Value fields = templateRoot["fields"];
	Json::Value outForm;
	Json::Value outFields;

	for ( size_t i = 0; i < fields.size(); i++ ) {
		Json::Value field = fields[i];
		inheritMembers(field, templateRoot);
		Json::Value outField = fieldFunction(field);
		if(!outField.isNull()){
			outFields.append(outField);
		}
	}

	outForm["fields"] = outFields;
	return outForm;
}
bool TemplateProcessor::start(const char* templatePath){
	Json::Value templateRoot;
	if( parseJsonFromFile(templatePath, templateRoot) ){
		formFunction(templateRoot);
		return true;		
	}
	return false;
}

