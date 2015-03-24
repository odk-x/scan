/*
This is an executable wrapper for ODKScan.
*/
#include "Processor.h"
#include "FileUtils.h"
#include "StatCollector.h"

#include <iostream>
#include <string>

#include <fstream>
#include <sys/stat.h>

using namespace std;

int main(int argc, char *argv[]) {

	//TODO: Put this is config json instead
	Processor myProcessor("assets/");
	if(argc == 2) {
		string result = myProcessor.processViaJSON(argv[1]);
		cout << endl << "<======= RESULT =======>" << endl << result << endl;
		return 0;
	}

	//Old API Below here:

	if(argc < 4) {
		cout << "Usage:" << endl;
		cout << string(argv[0]) << " templatePath inputImage outputDirectory" << endl;
		return 0;
	}

	string templatePath = addSlashIfNeeded(argv[1]);
	string inputImage(argv[2]);
	string outputPath = addSlashIfNeeded(argv[3]);

	//Make a directory with the name of the form
	//TODO: Move?
	mkdir(outputPath.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
	mkdir((outputPath + "segments").c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);

	string alignedFormOutfile(outputPath  + "aligned.jpg");
	//string markedupFormOutfile(outputPath + "markedup.jpg");
	//string jsonOutfile(outputPath + "output.json");
	
	cout << "Processing image: " << inputImage << endl;

	
	//TODO: Specify camera calibration somewhere else?
	//template doesn't make sense because it a property of the input image.
	#define CAMERA_CALIBRATION_FILE NULL
	if( !myProcessor.loadFormImage(inputImage.c_str(), CAMERA_CALIBRATION_FILE)) {
		cout << "\E[31m" <<  "Could not load. Arg: " << "\e[0m" << inputImage << endl;
		return 1;
	}
	
	if( !myProcessor.loadFeatureData(templatePath.c_str()) ) {
		cout << "\E[31m" <<  "Could not set load feature data. Arg: " << "\e[0m" << templatePath << endl;
		return 1;
	}
	
	if( !myProcessor.setTemplate(templatePath.c_str()) ) {
		cout << "\E[31m" <<  "Could not set template. Arg: " << "\e[0m" << templatePath << endl;
		return 1;
	}
	
	cout << "Outputting aligned image to: " << outputPath << endl;
	
	if( !myProcessor.alignForm(alignedFormOutfile.c_str()) ) {
		cout << "\E[31m" <<  "Could not align. Arg: " << "\e[0m" << alignedFormOutfile  << endl;
		return 1;
	}
	#define MINIFY_OUTPUT false
	if( !myProcessor.processForm(outputPath.c_str(), MINIFY_OUTPUT) ) {
		cout << "\E[31m" << "Could not process. Arg: " << "\e[0m" << outputPath << endl;
		return 1;
	}

	return 0;
}
