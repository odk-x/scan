#ifndef PROCESSOR_H
#define PROCESSOR_H
#include "configuration.h"
#include <tr1/memory>

class Processor{
	public:
		//The constructor takes as an argument a root path that it will look for training data and calibration data on.
		//The default constructor sets the root path to ""
		Processor();
		Processor(const char* appRootDir);
		bool loadFormImage(const char* imagePath, const char* calibrationFilePath = NULL);
		bool loadFeatureData(const char* templatePath);
		int detectForm();
		bool setTemplate(const char* templatePath);
		bool alignForm(const char* alignedImageOutputPath, int templateIdx = 0);
		bool processForm(const char* outputPath, bool minifyJson = false);
		//scanAndMarkup is a version of processForm that returns better errors.
		const char* scanAndMarkup(const char* outputPath);
		//Run the full processing pipeline via a JSON configuration string.
		const char* processViaJSON(const char* jsonString);
		bool writeFormImage(const char* outputPath) const;

	private:
		class ProcessorImpl;
    		std::tr1::shared_ptr<ProcessorImpl> processorImpl;
};


#endif
