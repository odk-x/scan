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


#include "boost/filesystem.hpp"   // includes all needed Boost.Filesystem declarations
namespace fs = boost::filesystem;

using namespace std;

string getLabel(const string& filepath){
	int start = filepath.find_last_of("/") + 1;
	return	filepath.substr(start, filepath.find_last_of("_") - start);
}
string removeLastComponent(const string& filepath){
	if(filepath.find_last_of("/") == filepath.length() - 1){
		return removeLastComponent(filepath.substr(0, filepath.length() - 1));
	}
	return filepath.substr(0, filepath.find_last_of("/"));
}
//Create all the missing directorys on the specified path.
void mkdirs(const string& path){
	int start = path.find_last_of("/");
	struct stat buf;
	//If the current path doesn't exist create its parent
	if(stat(path.c_str(), &buf) != 0){
		mkdirs(removeLastComponent(path));
	}
	mkdir(path.c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);
	return;
}
int main(int argc, char *argv[]) {

	clock_t init, final;	
	
	map<string, StatCollector> collectors;

	fs::path baseTemplatePath(argv[1]);
	fs::path inputDir(argv[2]);
	string outputDir = addSlashIfNeeded(argv[3]);

	if (!fs::exists(inputDir)) {
		cout << "Bad Path: " << inputDir << endl;
		return 0;
	}
	fs::directory_iterator outer_end_itr; // default construction yields past-the-end
	for (fs::directory_iterator outer_itr(inputDir); outer_itr != outer_end_itr; ++outer_itr) {
		if (!fs::is_directory(outer_itr->status())) {
			continue;
		}
		string label = outer_itr->path().filename().string();
		
		//if(label != "manhica") continue;
		
		fs::directory_iterator end_itr; // default construction yields past-the-end
		for (fs::directory_iterator itr(outer_itr->path()); itr != end_itr; ++itr) {
			if (!fs::is_directory(itr->status())) {
				continue;
			}
		
			//omit single page images:
			if(itr->path().filename().string() == "taken_2013-04-03_10-52-11(page2)") continue;
			if(itr->path().filename().string() == "taken_2013-04-02_12-46-49") continue;
			if(itr->path().filename().string() == "taken_2013-04-03_11-22-41") continue;
			
			/*
			fs::path retakePath()
			if (fs::exists(retakePath)) {
				cout << "Skipping retake..." << inputImage << endl;
				continue;
			}
			*/
			string expectedJsonFile((itr->path() / "expected.json").string());
			string inputImage((itr->path() / "photo.jpg").string());
			string outputPath( (fs::path(outputDir) / itr->path().filename()).string() + "/");
			string templatePath = baseTemplatePath.string();
			if(itr->path().filename().string().find("(page2)") != std::string::npos){
				templatePath = (baseTemplatePath / "nextPage").string();
			}
			string jsonOutfile(outputPath + "output.json");
			
			collectors[label].incrImages();
	
			init = clock();
	
			cout << "Creating output directory: " << outputPath << endl;
			mkdirs(outputPath);
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
			
			if(fileExists(jsonOutfile) && fileExists(expectedJsonFile)){
				collectors[label].compareFiles(jsonOutfile, expectedJsonFile, COMP_BUBBLE_VALS);
				//collectors[label].compareFiles(jsonOutfile, templatePath + ".json", COMP_BUBBLE_OFFSETS);
			}
			else {
				cout << "\E[31m" <<  "Could not compare files." << expectedJsonFile << " does not exist." << "\e[0m" << endl;
				continue;
			}
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
	
	ofstream myfile;
	myfile.open("histogram.csv", std::ios_base::app);
	overall.printHistRows(baseTemplatePath.filename().string(), myfile);
	myfile.close();
	
}
