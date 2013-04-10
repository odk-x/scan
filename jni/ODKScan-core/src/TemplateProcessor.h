#include <json/value.h>
#include <iostream>
#include <fstream>

/*
 * This is a class for traversing form definition JSON.
 * The idea is that you extend the class with the behaviors your want
 * for the form/field/segment objects, and make calls to the parent class
 * to descend into their children. In addition the form/field/segment
 * functions are called with JSON objects that inherit from their parent.
 *
 * In hindsight I think this class was a bad idea because of the complication in adds.
 * It would have been better to stick with nested for loops.
 */
class TemplateProcessor
{
	public:
	//inheritMembers makes the child value inherit the members that it does not override from the specified parent json value.
	//The parent is copied so it can be written over, while the child is passed in and returned with added members by refrence.
	Json::Value& inheritMembers(Json::Value& child, Json::Value parent) const;
	//XXX: If you override these, you should call the base class functions after your code to keep descending.
	virtual Json::Value segmentFunction(const Json::Value& segment);
	virtual Json::Value fieldFunction(const Json::Value& field);
	virtual Json::Value formFunction(const Json::Value& templateRoot);
	virtual bool start(const char* templatePath);


	virtual ~TemplateProcessor(){}
};

