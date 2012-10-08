#include <json/value.h>
#include <iostream>
#include <fstream>

//TODO: Add exceptions

/**
	This class allows you to specify functions that will be applied to the form/field/segment objects 
	as they are recursed over in a pre-order traversal.
	We assume templates that are nested like this:
		{fields : [ segments : [] ]}
	You need to call the base class functions for the recursion to happen.
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

bool parseJsonFromFile(const std::string& filePath, Json::Value& myRoot);
