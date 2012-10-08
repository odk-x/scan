#ifndef PROCESSOR_H
#define PROCESSOR_H
#include "configuration.h"
#include <tr1/memory>

/*
This class handles most of the JSON parsing and provides an interface to the image processing pipeline.
TODO:
I think this could be split into an alignment class and a classifier class.
The tricky part is setTemplate, which sets the form width/height used in alignForm.
I don't think this is necessairy, alignForm could get the width and height from loadFeatureData.
*/
class Processor{
	public:
		//The constructor takes as an arguement a root path that it will look for training data and calibration data on.
		//The default constructor sets the root path to ""
		Processor();
		Processor(const char* appRootDir);
		bool loadFormImage(const char* imagePath, const char* calibrationFilePath = NULL);
		bool loadFeatureData(const char* templatePath);
		int detectForm();
		bool setTemplate(const char* templatePath);
		bool alignForm(const char* alignedImageOutputPath, int templateIdx = 0);
		bool processForm(const char* outputPath, bool minifyJson = false);
		bool writeFormImage(const char* outputPath) const;

	private:
		class ProcessorImpl;
    		std::tr1::shared_ptr<ProcessorImpl> processorImpl;
};


#endif
