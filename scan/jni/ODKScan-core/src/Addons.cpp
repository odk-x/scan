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
#include "Addons.h"
#include <json/json.h>
#include <fstream>
#include <stdexcept>
#include <iostream>
#include <opencv2/highgui/highgui.hpp>

using namespace std;
using namespace cv;


Scalar getColor(bool filled) {
	if(filled){
		return Scalar(20, 20, 255);
	}
	else{
		return Scalar(255, 20, 20);
	}
}
Scalar getColor(int colorIdx) {
	Scalar colors[6] = {
	 Scalar(0,   0,   255),
	 Scalar(0,   255, 255),
	 Scalar(255, 0,   255),
	 Scalar(0,   255, 0),
	 Scalar(255, 255, 0),
	 Scalar(255, 0,   0)};
	return colors[colorIdx % 6];
}
Size operator * (float lhs, Size rhs) {
	return Size(lhs*rhs.width, lhs*rhs.height);
}
Rect operator * (float lhs, Rect rhs) {
	return Rect(lhs*rhs.tl(), lhs*rhs.size());
}
vector<Point> rectToQuad(const Rect& r){
	vector<Point> out;
	out.push_back(r.tl());
	out.push_back(Point(r.x+r.width, r.y));
	out.push_back(r.br());
	out.push_back(Point(r.x, r.y+r.height));
	return out;
}
Json::Value pointToJson(const Point p){
	Json::Value jPoint;
	jPoint.append(p.x);
	jPoint.append(p.y);
	return jPoint;
}
Point jsonToPoint(const Json::Value& jPoint){
	return Point(jPoint[0u].asInt(), jPoint[1u].asInt());
}
template <class T>
Json::Value quadToJsonArrayImpl(const vector< Point_<T> >& quad, const Point& offset){
	Json::Value out;
	for(size_t i = 0; i < quad.size(); i++){
		out.append(pointToJson(Point(quad[i].x + offset.x,
									 quad[i].y + offset.y)));
	}
	return out;
}
Json::Value quadToJsonArray(const vector< Point >& quad, const Point& offset){
	return quadToJsonArrayImpl(quad, offset);
}
Json::Value quadToJsonArray(const vector< Point2f >& quad, const Point& offset){
	return quadToJsonArrayImpl(quad, offset);
}
vector<Point> jsonArrayToQuad(const Json::Value& quadJson){
	vector<Point> out;
	for(size_t i = 0; i < quadJson.size(); i++){
		out.push_back(jsonToPoint(quadJson[i]));
	}
	return out;
}
void inferBubbles(Json::Value& field, inferenceMethod method){

	int cutIdx = minErrorCut(computedFilledIntegral(field));
	int bubbleNum = 0;
	
	Json::Value segments = field.get("segments", -1);
	for ( size_t j = 0; j < segments.size(); j++ ) {
		Json::Value segment = segments[j];
		Json::Value bubbles = segment.get("bubbles", -1);
		for ( size_t k = 0; k < bubbles.size(); k++ ) {
			bubbles[k]["value"] = Json::Value( bubbleNum < cutIdx );
			bubbleNum++;
		}
		segment["bubbles"] = bubbles;
		segments[j] = segment;
	}
	field["segments"] = segments;
}
//Indexing is a little bit complicated here.
//The first element of the filledIntegral is always 0.
//A min error cut at 0 means no bubbles are considered filled.
int minErrorCut(const vector<int>& filledIntegral){
	int minErrors = filledIntegral.back();
	int minErrorCutIdx = 0;
	for ( size_t i = 1; i < filledIntegral.size(); i++ ) {
		int errors = (int)i - 2 * filledIntegral[i] + filledIntegral.back();
		if(errors <= minErrors){ // saying < instead would weight things towards empty bubbles
			minErrors = errors;
			minErrorCutIdx = i;
		}
	}
	return minErrorCutIdx;
}
vector<int> computedFilledIntegral(const Json::Value& field){
	vector<int> filledIntegral(1,0);
	const Json::Value segments = field["segments"];
	for ( size_t i = 0; i < segments.size(); i++ ) {
		const Json::Value segment = segments[i];
		const Json::Value bubbles = segment["bubbles"];
		
		for ( size_t j = 0; j < bubbles.size(); j++ ) {
			const Json::Value bubble = bubbles[j];
			if(bubble["value"].asBool()){
				filledIntegral.push_back(filledIntegral.back()+1);
			}
			else{
				filledIntegral.push_back(filledIntegral.back());
			}
		}
	}
	return filledIntegral;
}
string replaceFilename(const string& filepath, const string& newName ){
	int nameIdx = filepath.find_last_of("/");
	return filepath.substr(0,nameIdx + 1) + newName;
}
void debugShow(const Mat& img){
	namedWindow("debug window", CV_WINDOW_NORMAL);
	imshow("debug window", img );

	for(;;)
	{
		char c = (char)waitKey(0);
		if( c == '\x1b' ) // esc
		{
			cvDestroyWindow("debug window");
			break;
		}
	}
}
int strToInt( const std::string& s ) {
	int result;
	std::istringstream ss( s );
	ss >> result;
	if (!ss) throw std::invalid_argument( "StrToInt" );
	return result;
}
string intToStr( int n )
{
	std::ostringstream result;
	result << n;
	return result.str();
}
Json::Value& extend(Json::Value& base, const Json::Value& extender) {
	Json::Value::Members members = extender.getMemberNames();
	for( Json::Value::Members::iterator itr = members.begin() ; itr != members.end() ; itr++ ) {
		base[*itr] = extender[*itr];
	}
	return base;
}
