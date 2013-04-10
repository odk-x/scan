/*
Addons is a bunch of code that isn't really class specific and get's reused in multiple places.
It's a pretty random mix of stuff and it if grows much larger it should probably be split up.
*/
#ifndef ADDONS_H
#define ADDONS_H
#include "configuration.h"

#include <opencv2/core/core.hpp>

#include <json/value.h>


//OpenCV oriented functions:
cv::Scalar getColor(bool filled);
cv::Scalar getColor(int colorIdx);
cv::Size operator * (float lhs, cv::Size rhs);
cv::Rect operator * (float lhs, cv::Rect rhs);
std::vector <cv::Point> rectToQuad(const cv::Rect& r);
//JSON/OpenCV oriented functions:
Json::Value pointToJson(const cv::Point p);
cv::Point jsonToPoint(const Json::Value& jPoint);
Json::Value quadToJsonArray(const std::vector<cv::Point>& quad, const cv::Point& offset = cv::Point(0,0));
Json::Value quadToJsonArray(const std::vector<cv::Point2f>& quad, const cv::Point& offset = cv::Point(0,0));
std::vector<cv::Point> jsonArrayToQuad(const Json::Value& quadJson);
//Indexing is a little bit complicated here.
//The first element of the filledIntegral is always 0.
//A min error cut at 0 means no bubbles are considered filled.
enum inferenceMethod{INFER_LTR_TTB, INFER_NEIGHBORS};
//TODO: one idea is to infer that bubbles labeled barely are filled if their neighbors are.
void inferBubbles(Json::Value& field, inferenceMethod method);
int minErrorCut(const std::vector<int>& filledIntegral);
std::vector<int> computedFilledIntegral(const Json::Value& field);
//Misc:
std::string replaceFilename(const std::string& filepath, const std::string& newName );
template <class Tp>
bool returnTrue(Tp& anything){
	return true;
}
void debugShow(const cv::Mat & image);

int strToInt( const std::string& s );
std::string intToStr( int n );

//JSON FUnctions
Json::Value& extend(Json::Value& base, const Json::Value& extender);
#endif
