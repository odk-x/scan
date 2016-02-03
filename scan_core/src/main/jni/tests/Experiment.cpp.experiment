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

#include <json/json.h>

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

		cout << "Creating output directory: " << outputPath << endl;
		mkdirs(outputPath);
		mkdir((outputPath + "segments").c_str(), S_IRWXU | S_IRWXG | S_IROTH | S_IXOTH);

		cout << "Processing image: " << inputImage << endl;

		Processor myProcessor("");
		Json::Value config;
		config["trainingDataDirectory"] = "assets/training_examples/";
		config["inputImage"] = inputImage;
		config["outputDirectory"] = outputPath;
		config["templatePath"] = templatePath;
		ostringstream ss;
		ss << config;
		Json::Reader reader;
		Json::Value result;
		reader.parse(myProcessor.processViaJSON(ss.str().c_str()), result);
		cout << result << endl;
		if( result.isMember("errorMessage") ) {
			cout << "\E[31m" <<  result["errorMessage"] << "\e[0m" << endl;
			cout << "Configuration used:" <<  config << endl;
			continue;
		}

		final=clock()-init;
		cout << "Time taken: " << (double)final / ((double)CLOCKS_PER_SEC) << " seconds" << endl;
		cout << "\E[32m" << "Apparent success!" << "\e[0m" << endl;
		
		collectors[label].addTime( (double)final / ((double)CLOCKS_PER_SEC) );
		
		string jsonOutfile(outputPath + "output.json");
		if(fileExists(jsonOutfile) && fileExists(expectedJsonFile)) {
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
