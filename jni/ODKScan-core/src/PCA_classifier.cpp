#include "configuration.h"
#include "PCA_classifier.h"
//#include "FileUtils.h"
#include <opencv2/highgui/highgui.hpp>

#include <iostream>

#ifdef OUTPUT_BUBBLE_IMAGES
	#include "NameGenerator.h"
	extern NameGenerator namer;
#endif

#ifdef OUTPUT_EXAMPLES
	#include "NameGenerator.h"
	NameGenerator exampleNamer;
#endif

#define NORMALIZE

//getRectSubPix might slow things down, but provides 2 advanges over the roi method
//1. If the example images have an odd dimension it will linearly interpolate the
//   half pixel error.
//2. If the rectangle crosses the image boundary (because of a large search window)
//   it won't give an error.
#define USE_GET_RECT_SUB_PIX

//#define USE_MASK
//Using a mask seems to slightly help bubble alignment in the average case but cause some additional misses.

//#define DISABLE_PCA
//PCA definately does help but only by a few percent.

using namespace std;
using namespace cv;

template <class Tp>
int vectorFind(const vector<Tp>& vec, const Tp& element) {
	for(size_t i = 0; i < vec.size(); i++) {
		if( vec[i] == element ) {
			return i;
		}
	}
	return -1;
}
/*
//This functions sets the values of the gaussian_wights Mat,
//which can be used to weight bubble ratings when doing bubble alignment.
//TODO: The gaussian intensity isn't correctly scaling...
void PCA_classifier::update_gaussian_weights() {
	float sigma = .5; //increasing decreases spread.

	//This precomputes the gaussian for subbbubble alignment.
	int height = search_window.height;
	int width = search_window.width;
	if(height == 0 || width == 0){
		gaussian_weights.release();
		return;
	}
	Mat v_gauss = getGaussianKernel(height, float(height) * sigma, CV_32F);
	Mat h_gauss;
	transpose(getGaussianKernel(width, float(width) * sigma, CV_32F), h_gauss);
	v_gauss = repeat(v_gauss, 1, width);
	h_gauss = repeat(h_gauss, height, 1);
	Mat temp = v_gauss.mul(h_gauss);
	double temp_max;
	minMaxLoc(temp, NULL, &temp_max);
	gaussian_weights = temp_max - temp + .001;//.001 is to avoid roundoff problems, it might not be necessiary.
}
void PCA_classifier::set_alignment_radius(int radius) {
	search_window = Size(2*radius, 2*radius);
	update_gaussian_weights();
}
*/
void PCA_classifier::set_classifier_params(const Json::Value& classifier_params_arg){
	//TODO: Push more of the param setting into this function.
	classifier_params = classifier_params_arg;
}
int PCA_classifier::getClassificationIdx(const string& filepath) {
	int nameIdx = filepath.find_last_of("/");
	string filename = filepath.substr(nameIdx + 1, filepath.size() - nameIdx);
	string classification = filename.substr(0, filename.find_first_of("_"));
	int classificationIdx = vectorFind(classifications, classification);
	
	if(vectorFind(classifications, classification) < 0) {
		classificationIdx = classifications.size();
		classifications.push_back(classification);
		
		#ifdef DEBUG_CLASSIFIER
			cout << "Adding classification: " << classification << endl;
		#endif
	}
	
	return classificationIdx;
}
//Add an image Mat to a PCA_set performing the 
//necessairy reshaping and type conversion.
void PCA_classifier::PCA_set_push_back(Mat& PCA_set, const Mat& img) {
	Mat PCA_set_row;
    
	img.convertTo(PCA_set_row, CV_32F);
	#ifdef NORMALIZE
	normalize(PCA_set_row, PCA_set_row);
	#endif
	if(PCA_set.data == NULL){
		PCA_set_row.reshape(0,1).copyTo(PCA_set);
	}
	else{
		PCA_set.push_back(PCA_set_row.reshape(0,1));
	}
}
template <class Tp>
void writeVector(FileStorage& fs, const string& label, const vector<Tp>& vec) {
	fs << label << "[";
	for(size_t i = 0; i < vec.size(); i++) {
		fs << vec[i];
	}
	fs << "]";
}
//Note this only reads string vectors
vector<string> readVector(FileStorage& fs, const string& label) {
	FileNode fn = fs[label];
	vector<string> vec(fn.size());
	if(fn[0].isString()){
		for(size_t i = 0; i < fn.size(); i++) {
			fn[i] >> vec[i];
		}
	}
	return vec;
}
void PCA_classifier::save(const string& outputPath) const throw(cv::Exception){
	FileStorage fs(outputPath, FileStorage::WRITE);
	fs << "exampleSizeWidth" << exampleSize.width;
	fs << "exampleSizeHeight" << exampleSize.height;
	fs << "PCAmean" << my_PCA.mean;
	fs << "PCAeigenvectors" << my_PCA.eigenvectors;
	fs << "PCAeigenvalues" << my_PCA.eigenvalues;
	writeVector(fs, "classifications", classifications);
	statClassifier->write(*fs, "classifierData" );
}
void PCA_classifier::load(const string& inputPath) throw(cv::Exception){
	FileStorage fs(inputPath, FileStorage::READ);
	fs["exampleSizeWidth"] >> exampleSize.width;
	fs["exampleSizeHeight"] >> exampleSize.height;
	fs["PCAmean"] >> my_PCA.mean;
	fs["PCAeigenvectors"] >> my_PCA.eigenvectors;
	fs["PCAeigenvalues"] >> my_PCA.eigenvalues;
	
	classifications = readVector(fs, "classifications");
	//search_window = exampleSize;
	//update_gaussian_weights();
	statClassifier->clear();
	statClassifier->read(*fs, cvGetFileNodeByName(*fs, cvGetRootFileNode(*fs), "classifierData") );
}
//Loads a image with the specified filename and adds it to the PCA set.
//Classifications are inferred from the filename and added to training_bubble_values.
void PCA_classifier::PCA_set_add(Mat& PCA_set, vector<int>& trainingBubbleValues, const string& filename, bool flipExamples) {

	//if( !isImage(filename) ) return;
	Mat example = imread(filename, 0);
	if (example.data == NULL) {
		cout << "could not read " << filename << endl;
		return;
	}
	Mat aptly_sized_example;
	resize(example, aptly_sized_example, Size(exampleSize.width, exampleSize.height), 0, 0, INTER_AREA);

	#ifdef USE_MASK
		aptly_sized_example = aptly_sized_example & cMask;
	#endif

	#ifdef OUTPUT_EXAMPLES
		string outfilename = filename.substr(filename.find_last_of("/"), filename.size() - filename.find_last_of("/"));
		imwrite("example_images_used" + outfilename, aptly_sized_example);
	#endif
	
	int classificationIdx = getClassificationIdx(filename);
	
	PCA_set_push_back(PCA_set, aptly_sized_example);
	trainingBubbleValues.push_back(classificationIdx);
	
	if(flipExamples){
		for(int i = -1; i < 2; i++){
			Mat temp;
			flip(aptly_sized_example, temp, i);
			PCA_set_push_back(PCA_set, temp);
			trainingBubbleValues.push_back(classificationIdx);
		}
	}
}
//TODO: Add some code to print out the training set error.
bool PCA_classifier::train_PCA_classifier(const vector<string>& examplePaths, Size myExampleSize,
                                          int eigenvalues, bool flipExamples) {
	statClassifier->clear();
	weights = (Mat_<float>(3,1) << 1, 1, 1);//TODO: fix the weighting stuff 
	exampleSize = myExampleSize;
	//search_window = myExampleSize;
	//update_gaussian_weights();

	#ifdef USE_MASK
		cMask = gaussian_weights < .002;
		/*
		namedWindow("outliers", CV_WINDOW_NORMAL);
		imshow( "outliers", cMask );
		
		for(;;)
		{
		    char c = (char)waitKey(0);
		    if( c == '\x1b' ) // esc
		    {
		    	cvDestroyWindow("inliers");
		    	cvDestroyWindow("outliers");
		        break;
		    }
		}
		*/
	#endif

	Mat PCA_set;
	vector<int> trainingBubbleValues;
	for(size_t i = 0; i < examplePaths.size(); i++) {
		PCA_set_add(PCA_set, trainingBubbleValues, examplePaths[i], flipExamples);
	}

	if(PCA_set.rows < eigenvalues) return false;//I'm not completely sure about this line.

	my_PCA = PCA(PCA_set, Mat(), CV_PCA_DATA_AS_ROW, eigenvalues);
	Mat comparisonVectors = my_PCA.project(PCA_set);
	
	Mat trainingBubbleValuesMat(1,1,CV_32SC1);
	trainingBubbleValuesMat.at<int>(0) = trainingBubbleValues[0];
	for(size_t i = 1; i < trainingBubbleValues.size(); i++){
		trainingBubbleValuesMat.push_back( trainingBubbleValues[i] );
	}

	#ifdef DISABLE_PCA
		statClassifier->train_auto(PCA_set, trainingBubbleValuesMat, Mat(), Mat(), CvSVMParams());
	#else
		statClassifier->train_auto(comparisonVectors, trainingBubbleValuesMat, Mat(), Mat(), CvSVMParams());
	#endif

	return true;
}
//Rates a region of pixels in det_img_gray on how similar it is to the classifier's training examples.
//A lower rating means it is more similar.
//The rating is the  sum of squard difference of the queried pixels and their PCA back projection.
//Back projection tries to reconstruct a image/vector just using components of the PCA set (generated from the training data).
//The theory is that if there is little difference between the reconstructed image and the original image
//(as measured by the SSD) then the image is probably similar to some of the images used to generate the PCA set.
inline double PCA_classifier::rate_item(const Mat& det_img_gray, const Point& item_location) const {
	Mat query_pixels, pca_components;

	#ifdef USE_GET_RECT_SUB_PIX
		getRectSubPix(det_img_gray, exampleSize, item_location, query_pixels);
	#else
		Rect window = Rect(item_location - Point(exampleSize.width/2,
		                                           exampleSize.height/2), exampleSize);
		//Constrain the window to the image size:
		window = window & Rect(Point(0,0), det_img_gray.size());

		query_pixels = Mat::zeros(exampleSize, det_img_gray.type());
		query_pixels(Rect(Point(0,0), window.size())) += det_img_gray(window);
	#endif

	#ifdef USE_MASK
		query_pixels = query_pixels & cMask;
	#endif
	
	query_pixels.reshape(0,1).convertTo(query_pixels, CV_32F);

	#ifdef NORMALIZE
		normalize(query_pixels, query_pixels);
	#endif
	
	pca_components = my_PCA.project(query_pixels);

	#if 0
		Mat out;
		matchTemplate( pca_components, my_PCA.backProject(pca_components),
		               out, CV_TM_SQDIFF_NORMED);
		return out.at<float>(0,0);
	#endif

	Mat out = my_PCA.backProject(pca_components) - query_pixels;

	return sum(out.mul(out)).val[0];
}
//This using a hill descending algorithm to find the location that minimizes the value of the rate bubble function.
//It might only find a local instead of global minimum but it is much faster than a global search.
Point PCA_classifier::align_item(const Mat& det_img_gray, const Point& seed_location, double alignment_radius) const {
	int iterations = 10;
	#define CHECKED 123

	Mat sofar = Mat::zeros(Size(2*iterations+1, 2*iterations+1), CV_8UC1);
	Point sofarCenter = Point(iterations, iterations);
	//This is the offset of the element in deg_img_gray corresponding to the top-left element of sofar.
	Point offset = seed_location - sofarCenter;

	Point loc = Point(sofarCenter);
	
	double minDirVal = 100.;
	while( iterations > 0 ){
		Point minDir(0,0);
		for(int i = loc.x-1; i <= loc.x+1; i++) {
			for(int j = loc.y-1; j <= loc.y+1; j++) {
				if(sofar.at<uchar>(j,i) != CHECKED) {
					sofar.at<uchar>(j,i) = CHECKED;
					double initLocDistance = norm(loc - sofarCenter);
					double rating = rate_item(det_img_gray, Point(i,j) + offset);
					//This weights ratings to be higher the further they get from the seed location.
					rating *= MAX(1, initLocDistance);
					
					if(rating <= minDirVal){
						minDirVal = rating;
						minDir = Point(i,j) - loc;
					}
					
				}
			}
		}
		if(minDir.x == 0 && minDir.y == 0){
			break;
		}
		loc += minDir;
		if(norm(loc - sofarCenter) > alignment_radius){
			break;
		}
		iterations--;
	}
	#if 0
	//This shows the examined pixels if it is on.
	namedWindow("outliers", CV_WINDOW_NORMAL);
	imshow( "outliers", sofar );
	
	for(;;)
	{
	    char c = (char)waitKey(0);
	    if( c == '\x1b' ) // esc
	    {
	    	cvDestroyWindow("inliers");
	    	cvDestroyWindow("outliers");
	        break;
	    }
	}
	#endif
	return loc + offset;
}
/*
//This bit of code finds the location in the search_window most likely to be a bubble
//then it checks that rather than the exact specified location.
//This section probably slows things down by quite a bit and it might not provide significant
//improvement to accuracy. We will need to run some tests to find out if it's worth using.
Point PCA_classifier::align_item(const Mat& det_img_gray, const Point& seed_location) const {
	if(search_window.width == 0 || search_window.height == 0){
		return seed_location;
	}

	Mat out(search_window, CV_32F);
	Point offset = Point(seed_location.x - search_window.width/2,
	                     seed_location.y - search_window.height/2);
	
	for(int i = 0; i < search_window.width; i++) {
		for(int j = 0; j < search_window.height; j++) {
			out.at<float>(j,i) = rate_item(det_img_gray, Point(i,j) + offset);
		}
	}
	out = out.mul(gaussian_weights);
	
	Point min_location;
	minMaxLoc(out, NULL,NULL, &min_location);
	
	return min_location + offset;
}
*/

//Compare the specified bubble with all the training bubbles via PCA.
Json::Value PCA_classifier::classify_item(const Mat& det_img_gray, const Point& item_location) const {

	int classificationIndex;	
	Mat query_pixels;
	//cout << item_location << endl;
	#ifdef USE_GET_RECT_SUB_PIX
		getRectSubPix(det_img_gray, Size(exampleSize.width, exampleSize.height),
		              item_location, query_pixels);
	#else									 
		Rect window = Rect(item_location - Point(exampleSize.width/2,
		                                           exampleSize.height/2), exampleSize);
		//Constrain the window to the image size:
		window = window & Rect(Point(0,0), det_img_gray.size());

		query_pixels = Mat::zeros(exampleSize, det_img_gray.type());
		query_pixels(Rect(Point(0,0), window.size())) += det_img_gray(window);
	#endif
	
	#ifdef USE_MASK
		query_pixels = query_pixels & cMask;
	#endif
	
	#ifdef OUTPUT_BUBBLE_IMAGES
		string bubbleName = namer.get_unique_name("bubble_");
		imwrite("bubble_images/" + bubbleName + ".jpg", query_pixels);
	#endif
	
	query_pixels.convertTo(query_pixels, CV_32F);
	query_pixels = query_pixels.reshape(0,1);
	
	#ifdef NORMALIZE
		normalize(query_pixels, query_pixels);
	#endif
	//cout << "svc: " << *statClassifier->get_support_vector(25) << endl;
	//return 0;
	//I'm segfaulting when statClassifier is called. The reason might have little to do with statClassifier->..
	#ifdef DISABLE_PCA
		classificationIndex = statClassifier->predict( query_pixels );
	#else
		classificationIndex = statClassifier->predict( my_PCA.project(query_pixels) );
	#endif
	string classification_label = classifications[classificationIndex];
	Json::Value default_classification = classifier_params.get("default_classification", 0);
	return classifier_params["classification_map"].get(classification_label, default_classification);
}
