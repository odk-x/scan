/*
This program runs the image processing pipeline on every image in the specified folder (recusively)
then prints out stats breaking down the results by image label and pipeline stage.
*/
#include "Processor.h"
#include "FileUtils.h"
#include "StatCollector.h"

#include <iostream>
#include <string>
#include <map>
#include <time.h>

#include <fstream>
#include <sys/stat.h>

using namespace std;

string getLabel(const string& filepath){
	int start = filepath.find_last_of("/") + 1;
	return	filepath.substr(start, filepath.find_last_of("_") - start);
}
int main(int argc, char *argv[]) {

	clock_t init, final;	
	
	map<string, StatCollector> collectors;

	string templatePath = addSlashIfNeeded(argv[1]);
	string inputDir = addSlashIfNeeded(argv[2]);
	string outputDir = addSlashIfNeeded(argv[3]);
	string expectedJsonFile(argv[4]);

	vector<string> filenames;
	CrawlFileTree(inputDir, filenames);

	vector<string>::iterator it;
	for(it = filenames.begin(); it != filenames.end(); it++) {
		string inputImage((*it));
		if( !isImage(inputImage) ) continue;
		string relativePathMinusExt((*it).substr((*it).find_first_of(inputDir) + inputDir.length(),
                            (*it).length()-inputDir.length()-4) + "/");
		string outputPath(outputDir + relativePathMinusExt);
		string label = getLabel(inputImage);
		
		//Limits the number of images to five for any given condition
		//if(collectors[label].numImages >= 5) continue;

		collectors[label].incrImages();

		init = clock();

		//Make a directory with the name of the form
		//TODO: Move?
		mkdir(outputPath.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
		mkdir((outputPath + "segments").c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);

		cout << "Processing image: " << inputImage << endl;

		Processor myProcessor("assets/");
		//TODO: Specify camera calibration somewhere else?
		//template doesn't make sense because it a property of the input image.
		#define CAMERA_CALIBRATION_FILE NULL
		if( !myProcessor.loadFormImage(inputImage.c_str(), CAMERA_CALIBRATION_FILE)) {
			cout << "\E[31m" <<  "Could not load. Arg: " << "\e[0m" << inputImage << endl;
			continue;
		}
	
		if( !myProcessor.loadFeatureData(templatePath.c_str()) ) {
			cout << "\E[31m" <<  "Could not set load feature data. Arg: " << "\e[0m" << templatePath << endl;
			continue;
		}
	
		if( !myProcessor.setTemplate(templatePath.c_str()) ) {
			cout << "\E[31m" <<  "Could not set template. Arg: " << "\e[0m" << templatePath << endl;
			continue;
		}
	
		cout << "Outputting aligned image to: " << outputPath << endl;
		string alignedFormOutfile(outputPath  + "aligned.jpg");
		if( !myProcessor.alignForm(alignedFormOutfile.c_str()) ) {
			cout << "\E[31m" <<  "Could not align. Arg: " << "\e[0m" << alignedFormOutfile  << endl;
			continue;
		}
		#define MINIFY_OUTPUT false
		if( !myProcessor.processForm(outputPath.c_str(), MINIFY_OUTPUT) ) {
			cout << "\E[31m" << "Could not process. Arg: " << "\e[0m" << outputPath << endl;
			continue;
		}

		final=clock()-init;
		cout << "Time taken: " << (double)final / ((double)CLOCKS_PER_SEC) << " seconds" << endl;
		cout << "\E[32m" << "Apparent success!" << "\e[0m" << endl;
		
		collectors[label].addTime( (double)final / ((double)CLOCKS_PER_SEC) );
		
		string jsonOutfile(outputPath + "output.json");
		if(fileExists(jsonOutfile) && fileExists(expectedJsonFile)){
			collectors[label].compareFiles(jsonOutfile, expectedJsonFile, COMP_BUBBLE_VALS);
			//collectors[label].compareFiles(jsonOutfile, templatePath + ".json", COMP_BUBBLE_OFFSETS);
		}
		else{
			cout << "\E[31m" <<  "Could not compare files." << expectedJsonFile << " does not exist." << "\e[0m" << endl;
			continue;
		}
	}

	cout << linebreak << endl;
	StatCollector overall;
	map<string, StatCollector>::iterator mapit;
	for ( mapit=collectors.begin() ; mapit != collectors.end(); mapit++ ){
		cout << (*mapit).first << endl << (*mapit).second;
		overall += (*mapit).second;
	}
	cout << "overall" << endl << overall;
}
