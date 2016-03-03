/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
#include "configuration.h"
#include "Aligner.h"
#include "AlignmentUtils.h"
#include "Addons.h"
#include "FileUtils.h"

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/calib3d/calib3d.hpp>

#include <math.h>
#include <json/json.h>

#include <iostream>
#include <fstream>

#if 0
	#define ALWAYS_COMPUTE_TEMPLATE_FEATURES
	#define SHOW_MATCHES_WINDOW
#endif

//#define MASK_CENTER_AMOUNT .41

#ifdef OUTPUT_DEBUG_IMAGES
	#include "NameGenerator.h"
	NameGenerator dbgNamer("debug_form_images/", true);
#endif

using namespace std;
using namespace cv;

//Generate a mask that blocks out segments using the specified template.
Mat generateMask(const Json::Value& templateRoot) {
	Mat markupImage = Mat(templateRoot.get("height", 0).asInt(),
	                      templateRoot.get("width", 0).asInt(),
	                      CV_8UC1, Scalar::all(255));

	const Json::Value fields = templateRoot["fields"];

	//Note: segments etc. are not inherited like in processor.
	//I don't think that behavior is necessairy.

	for ( size_t i = 0; i < fields.size(); i++ ) {
		const Json::Value field = fields[i];
		const Json::Value segments = field["segments"];
		for ( size_t j = 0; j < segments.size(); j++ ) {
			const Json::Value segment = segments[j];
			Rect segRect( Point( segment.get("segment_x", INT_MIN ).asInt(),
			                     segment.get("segment_y", INT_MIN).asInt()),
			              Size( segment.get("segment_width", INT_MIN).asInt(),
			                    segment.get("segment_height", INT_MIN).asInt()));
			rectangle( markupImage, segRect.tl(), segRect.br(), Scalar::all(0), -1);
		}
	}
	return markupImage;
}

//This is from the OpenCV descriptor matcher example.
void crossCheckMatching( Ptr<DescriptorMatcher>& descriptorMatcher,
                         const Mat& descriptors1, const Mat& descriptors2,
                         vector<DMatch>& filteredMatches12, int knn=1 )
{
    filteredMatches12.clear();
    vector<vector<DMatch> > matches12, matches21;
    descriptorMatcher->knnMatch( descriptors1, descriptors2, matches12, knn );
    descriptorMatcher->knnMatch( descriptors2, descriptors1, matches21, knn );
    for( size_t m = 0; m < matches12.size(); m++ )
    {
        bool findCrossCheck = false;
        for( size_t fk = 0; fk < matches12[m].size(); fk++ )
        {
            DMatch forward = matches12[m][fk];

            for( size_t bk = 0; bk < matches21[forward.trainIdx].size(); bk++ )
            {
                DMatch backward = matches21[forward.trainIdx][bk];
                if( backward.trainIdx == forward.queryIdx )
                {
                    filteredMatches12.push_back(forward);
                    findCrossCheck = true;
                    break;
                }
            }
            if( findCrossCheck ) break;
        }
    }
}

void loadFeatures( const string& featuresFile, Size& templImageSize,
                   vector<KeyPoint>& templKeypoints, Mat& templDescriptors) throw(cv::Exception) {
							
	if( !fileExists(featuresFile) ) CV_Error(CV_StsError, "Specified feature file does not exist.");
	
	FileStorage fs(featuresFile, FileStorage::READ);
	
	if( !fs["templwidth"].empty() && !fs["templheight"].empty() &&
		!fs["templKeypoints"].empty() && !fs["templDescriptors"].empty()){

		fs["templwidth"] >> templImageSize.width;
		fs["templheight"] >> templImageSize.height; 

		read(fs["templKeypoints"], templKeypoints);
		fs["templDescriptors"] >> templDescriptors;
	}
	else{
		CV_Error(CV_StsError, "Required field is missing.");
	}
	
	if(templKeypoints.empty() || templDescriptors.empty()) CV_Error(CV_StsError, "Keypoints or descriptors not loaded.");
}
void saveFeatures(  const string& featuresFile, const Size& templImageSize,
					const vector<KeyPoint>& templKeypoints, const Mat& templDescriptors) throw(cv::Exception) {
					
	// write feature data to a file.
	FileStorage fs(featuresFile, FileStorage::WRITE);

	fs << "templwidth" << templImageSize.width;
	fs << "templheight" << templImageSize.height;

	write(fs, "templKeypoints", templKeypoints);
	fs << "templDescriptors" << templDescriptors;
}

Aligner::Aligner(){
	//With OpenCV form alignment can be broken apart into 3 components,
	//feature detection, feature extraction, and feature matching.
	//In each param set different combinations of components are configured:

	//A few notes:
	//- The grid adapted feature detector is good for limiting the number of key-points,
	//and ensuring they are reasonably well distributed around the image.
	//- BRIEF is fast but not scale/rotation invariant.
	//- SURF and SIFT have invariance but are slower and patented.
	//- STANDARD_AREA is a parameter because we don't know how big the image should be before we detect the template.
	//so it is resized to have the standard area.
	//This also has the implicit assumption that templates all are are approximately the same size.

	//IMPORTANT: Make sure you define ALWAYS_COMPUTE_TEMPLATE_FEATURES
	//when trying new parameterizations, otherwise your cached features will be used.

	#define PARAM_SET 1
	#if PARAM_SET == 0
		detector = Ptr<FeatureDetector>(
			new GridAdaptedFeatureDetector(
				new SurfFeatureDetector( 250., 1, 3),
				3500, 7, 7));
	
		//descriptorExtractor = DescriptorExtractor::create( "SURF" );
		descriptorExtractor = Ptr<DescriptorExtractor>(new SurfDescriptorExtractor( 1, 3 ));
		#define MATCHER_TYPE "FlannBased"
		#define FH_REPROJECT_THRESH 4.0
		#define STANDARD_AREA 1259712.f
	#elif PARAM_SET == 1 
		//This is the parameter set we used in the paper.
		//Optimal hessian level seems to vary by phone
		//Total success rate: 99.3063%
		//Average image processing time: 4.9735 seconds

		detector = Ptr<FeatureDetector>(
			new GridAdaptedFeatureDetector(
				new SurfFeatureDetector( 395., 1, 3),
				3500, 7, 7));
	
		//descriptorExtractor = DescriptorExtractor::create( "SURF" );
		descriptorExtractor = Ptr<DescriptorExtractor>(new SurfDescriptorExtractor( 1, 3 ));
		#define MATCHER_TYPE "FlannBased"
		#define FH_REPROJECT_THRESH 4.0
		//1259712 is the area of a 5 megapixel image scaled by .5.
		//The goal is to keep resizing consistent.
		#define STANDARD_AREA 1259712.f
	#elif PARAM_SET == 2
		//Reduced number of features:
		detector = Ptr<FeatureDetector>(
			new GridAdaptedFeatureDetector(
				new SurfFeatureDetector( 395., 1, 3),
				500, 5, 5));
	
		//descriptorExtractor = DescriptorExtractor::create( "SURF" );
		descriptorExtractor = Ptr<DescriptorExtractor>(new SurfDescriptorExtractor( 1, 3 ));
		#define MATCHER_TYPE "BruteForce"
		#define FH_REPROJECT_THRESH 4.0
	#elif PARAM_SET == 3
		//Trying to balance speed with reliablity.
		detector = Ptr<FeatureDetector>(
			new GridAdaptedFeatureDetector(
				new SurfFeatureDetector( 395., 1, 3),
				500, 5, 5));
		descriptorExtractor = Ptr<DescriptorExtractor>(new SurfDescriptorExtractor( 1, 3 ));
		#define MATCHER_TYPE "FlannBased"
		#define FH_REPROJECT_THRESH 4.0
		#define STANDARD_AREA 1259712.f
	#elif PARAM_SET == 4
		//ORB 98.1873%
		detector = FeatureDetector::create("GridORB");
		descriptorExtractor = DescriptorExtractor::create("ORB");
		#define MATCHER_TYPE "BruteForce-Hamming"
		#define FH_REPROJECT_THRESH 3.0
		#define STANDARD_AREA 1259712.f
	#elif PARAM_SET == 5
		//ORB nexus_bg/nice -- 98.3468%
		detector = FeatureDetector::create("GridORB");
		descriptorExtractor = DescriptorExtractor::create("ORB");
		#define MATCHER_TYPE "BruteForce-Hamming"
		#define FH_REPROJECT_THRESH 4.0
		//Standard area eyeballed so that the actual image is about the same size as the template.
		//I think this will cause some issues in terms of robustness. It could be tricky for people to make templates/take pictures of the right scale.
		#define STANDARD_AREA 1200000.f
	#elif PARAM_SET == 6
		//FAST nexus_bg/nice -- 99.8278%
		//Average image processing time: 3.5605 seconds
		//Downside is that it is very sensitive to scale and upsidedown pictures are no longer possible.
		detector = FeatureDetector::create("GridFAST");
		descriptorExtractor = DescriptorExtractor::create("BRIEF");
		#define MATCHER_TYPE "BruteForce-HammingLUT"
		#define FH_REPROJECT_THRESH 4.0
		#define STANDARD_AREA 1259712.f
	#elif PARAM_SET == 7
		detector = FeatureDetector::create("GridORB");
		descriptorExtractor = DescriptorExtractor::create("ORB");
		#define MATCHER_TYPE "BruteForce-HammingLUT"
		#define FH_REPROJECT_THRESH 4.0
		#define STANDARD_AREA 1259712.f
    #elif PARAM_SET == 8
        detector = Ptr<FeatureDetector>(
				new GridAdaptedFeatureDetector(
						new FastFeatureDetector(10, true), 3500, 7, 7));
		descriptorExtractor = Ptr<DescriptorExtractor>(
				new FREAK(true, true, 22.0f, 4, vector<int>()));
		#define MATCHER_TYPE "BruteForce-HammingLUT"
		#define FH_REPROJECT_THRESH 4.0
		#define STANDARD_AREA 1259712.f
	#endif

}
void Aligner::setImage( const cv::Mat& img ){

	currentImg = img;
	
	Mat currentImgResized;

	resize(currentImg, currentImgResized, sqrt(STANDARD_AREA / currentImg.size().area()) * currentImg.size(), 0, 0, INTER_AREA);
	
	trueEfficiencyScale = Point3d(  double(currentImgResized.cols) / img.cols,
	                                double(currentImgResized.rows) / img.rows,
	                                1.0);

	#if 0
		Mat temp;
		adaptiveThreshold(currentImgResized, temp, 50, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 9, 15);
		//equalizeHist(currentImgResized, temp);
		currentImgResized = currentImgResized + temp;
		currentImgResized.convertTo(currentImgResized, 0);
	#endif
	
	#ifdef SHOW_MATCHES_WINDOW
		featureSource = currentImgResized;
	#endif
	
	#if 0
		debugShow(currentImgResized);
	#endif
	
	Mat mask(currentImgResized.rows, currentImgResized.cols, CV_8UC1, Scalar::all(255));
	
	#ifdef MASK_CENTER_AMOUNT
		Rect roi = resizeRect(Rect(Point(0,0), currentImgResized.size()), MASK_CENTER_AMOUNT);
		rectangle(mask, roi.tl(), roi.br(), Scalar::all(0), -1);
	#endif
	
	#ifdef DEBUG_ALIGN_IMAGE
		cout << "Extracting keypoints from current image..." << endl;
	#endif
	
	detector->detect( currentImgResized, currentImgKeypoints, mask);
	
	#ifdef DEBUG_ALIGN_IMAGE
		cout << "\t" << currentImgKeypoints.size() << " points" << endl;
		cout << "Computing descriptors for keypoints from current image..." << endl;
	#endif
	
	descriptorExtractor->compute( currentImgResized, currentImgKeypoints, currentImgDescriptors );
}
void Aligner::loadFeatureData(const string& imagePath, const string& jsonPath, const string& featuresFile) throw(cv::Exception) {

	vector<KeyPoint> templKeypoints;
	Mat templDescriptors;
	Size templImageSize;

	try{
		#ifdef ALWAYS_COMPUTE_TEMPLATE_FEATURES
			CV_Error(CV_StsError, "Always compute template features is on.");
		#endif
		loadFeatures( featuresFile, templImageSize, templKeypoints, templDescriptors );
	}
	catch( cv::Exception& e ) {
	
		cout << e.what() << endl;
		LOGI("Creating new feature data:");
		
		if(detector.empty() || descriptorExtractor.empty())
			CV_Error(CV_StsError, "Counld not create detector/extractor.");
		
		Mat templImage, temp;
		templImage = imread( imagePath, 0 );
		if(templImage.empty()) {
			CV_Error(CV_StsError, "Template image not found: " + imagePath);
		}

		Json::Value templateRoot;
		if( !parseJsonFromFile(jsonPath, templateRoot) ){
			CV_Error(CV_StsError, "Could not read template.json: " + jsonPath);
		}
		//Resize the template image to the size specified in the template json.
		//TODO: Get standard image size from average template image size?
		templImageSize = Size( templateRoot.get("width", 0).asInt(),
		                       templateRoot.get("height", 0).asInt());
		
		//When a picture can be aligned without masking,
		//the mask seems to make the alignment slightly less precise.
		//However, some pictures are not correctly aligned when masking is not used.
		Mat mask = generateMask(templateRoot);
		//debugShow(mask);

		#ifdef MASK_CENTER_AMOUNT
			Rect roi = resizeRect(Rect(Point(0,0), templImageSize), MASK_CENTER_AMOUNT);
			rectangle(mask, roi.tl(), roi.br(), Scalar::all(0), -1);
		#endif
		
		resize(templImage, temp, templImageSize, 0, 0, INTER_AREA);
		templImage.release();
		templImage = temp;
		
		//debugShow(templImage & mask);
		
		#ifdef DEBUG_ALIGN_IMAGE
			cout << "Extracting keypoints from template image..." << endl;
		#endif

		detector->detect( templImage, templKeypoints, mask );

		#ifdef DEBUG_ALIGN_IMAGE
			cout << "\t" << templKeypoints.size() << " points" << endl;
			cout << "Computing descriptors for keypoints from template image..." << endl;
		#endif
		
		descriptorExtractor->compute( templImage, templKeypoints, templDescriptors );
		cout << featuresFile << endl;

		saveFeatures(featuresFile, templImageSize, templKeypoints, templDescriptors);
	}
	#ifdef SHOW_MATCHES_WINDOW
		{
		Mat templImage, temp;
		templImage = imread( imagePath, 0 );
		resize(templImage, temp, templImageSize, 0, 0, INTER_AREA);
		templateImages.push_back( temp );
		}
	#endif
	templKeypointsVec.push_back(templKeypoints);
	templDescriptorsVec.push_back(templDescriptors);
	templImageSizeVec.push_back(templImageSize);
}
size_t Aligner::detectForm() const throw(cv::Exception) {
	//TODO: This code was whipped up in a hurry,
	//and will probably not do a very good job telling the difference
	//between similar forms.
	//It could also be much faster if it used a cached classifier.
	LOGI("Detecting form...");
	Ptr<DescriptorMatcher> descriptorMatcher = DescriptorMatcher::create( MATCHER_TYPE );
	size_t formIdx = 0;
	size_t previousBest = 0;
	size_t numForms = templDescriptorsVec.size();
	if(numForms == 0) CV_Error(CV_StsError, "No templates loaded.");
	
	for(size_t i = 0; i < numForms; i++){
		vector<DMatch> filteredMatches;
		size_t inliers = 0;
		crossCheckMatching( descriptorMatcher, currentImgDescriptors, templDescriptorsVec[i], filteredMatches);
		
		vector<int> queryIdxs( filteredMatches.size() ), trainIdxs( filteredMatches.size() );
		for( size_t j = 0; j < filteredMatches.size(); j++ )
		{
			queryIdxs[j] = filteredMatches[j].queryIdx;
			trainIdxs[j] = filteredMatches[j].trainIdx;
		}

		vector<Point2f> points1; KeyPoint::convert(currentImgKeypoints, points1, queryIdxs);
		vector<Point2f> points2; KeyPoint::convert(templKeypointsVec[i], points2, trainIdxs);
		
		Mat H = findHomography( Mat(points1), Mat(points2), CV_RANSAC, FH_REPROJECT_THRESH );
		Mat points1t; perspectiveTransform(Mat(points1), points1t, H);
		
		for( size_t i1 = 0; i1 < points1.size(); i1++ )
		{
		    if( norm(points2[i1] - points1t.at<Point2f>((int)i1,0)) < FH_REPROJECT_THRESH ) // inlier
		       inliers++;
		}
		
		if(inliers > previousBest){
			previousBest = inliers;
			formIdx = i;
		}
	}
	return formIdx;
	/*
	Ptr<BOWImgDescriptorExtractor> bowExtractor =
			new BOWImgDescriptorExtractor( descriptorExtractor, DescriptorMatcher::create( MATCHER_TYPE ));
	
	Mat vocabulary = trainVocabulary( "BOWfile", vocData, vocabTrainParams,
	                                  detector, descriptorExtractor );
                                      
	CvSVM svm;
	trainSVMClassifier( svm, svmTrainParamsExt, objClasses[classIdx], vocData,
	                    bowExtractor, featureDetector, resPath );
                                      
	bowExtractor->setVocabulary( vocabulary );
	
	Mat bowDescriptor;
	bowExtractor->compute( currentImg, currentImgKeypoints, bowDescriptor );
	*/
}
void Aligner::alignFormImage(Mat& aligned_img, const Size& aligned_img_sz, size_t featureDataIdx ) throw(cv::Exception) {
	
	vector<KeyPoint> templKeypoints = templKeypointsVec[featureDataIdx];
	Mat templDescriptors = templDescriptorsVec[featureDataIdx];
	Size templImageSize = templImageSizeVec[featureDataIdx];

	#ifdef DEBUG_ALIGN_IMAGE
		cout << "Matching descriptors..." << endl;
	#endif
	Ptr<DescriptorMatcher> descriptorMatcher = DescriptorMatcher::create( MATCHER_TYPE );
	
	if( descriptorMatcher.empty()) CV_Error(CV_StsError, "Can not create descriptor matcher of given type");
	
	vector<DMatch> filteredMatches;
	crossCheckMatching( descriptorMatcher, currentImgDescriptors, templDescriptors, filteredMatches);
	//descriptorMatcher->match( currentImgDescriptors, templDescriptors, filteredMatches );

	vector<int> queryIdxs( filteredMatches.size() ), trainIdxs( filteredMatches.size() );
	for( size_t i = 0; i < filteredMatches.size(); i++ )
	{
		queryIdxs[i] = filteredMatches[i].queryIdx;
		trainIdxs[i] = filteredMatches[i].trainIdx;
	}

	vector<Point2f> points1; KeyPoint::convert(currentImgKeypoints, points1, queryIdxs);
	vector<Point2f> points2; KeyPoint::convert(templKeypoints, points2, trainIdxs);

	Point3d sc = Point3d( ((double)aligned_img_sz.width) / templImageSize.width,
	                      ((double)aligned_img_sz.height) / templImageSize.height,
	                      1.0);
	
	if( points1.size() < 4 || points2.size() < 4) CV_Error(CV_StsError, "Not enough matches.");
	
	Mat H = findHomography( Mat(points1), Mat(points2), CV_RANSAC, FH_REPROJECT_THRESH );//CV_LMEDS
	Mat Hscaled = Mat::diag(Mat(sc)) * H * Mat::diag(Mat(trueEfficiencyScale));

	aligned_img = Mat(0,0, CV_8U);
	warpPerspective( currentImg, aligned_img, Hscaled, aligned_img_sz);
	
	vector<Point> quad = transformationToQuad(Hscaled, aligned_img_sz);

	#ifdef SHOW_MATCHES_WINDOW
		//This code creates a window to show matches:
		vector<char> matchesMask( filteredMatches.size(), 0 );
		Mat points1t; perspectiveTransform(Mat(points1), points1t, H);
		for( size_t i1 = 0; i1 < points1.size(); i1++ )
		{
			if( norm(points2[i1] - points1t.at<Point2f>((int)i1,0)) < FH_REPROJECT_THRESH ) // inlier
				matchesMask[i1] = 1;
		}
	
		Mat drawImg;

		drawMatches( featureSource, currentImgKeypoints, templateImages[featureDataIdx], templKeypoints, filteredMatches, drawImg,
					CV_RGB(0, 255, 0), CV_RGB(255, 0, 255), matchesMask, DrawMatchesFlags::DRAW_RICH_KEYPOINTS);
	
		debugShow(drawImg);
		
		for( size_t i1 = 0; i1 < matchesMask.size(); i1++ ){
			matchesMask[i1] = !matchesMask[i1];
		}
		drawMatches( featureSource, currentImgKeypoints, templateImages[featureDataIdx], templKeypoints, filteredMatches, drawImg,
        	             CV_RGB(0, 0, 255), CV_RGB(255, 0, 0), matchesMask,
        	             DrawMatchesFlags::DRAW_OVER_OUTIMG | DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS );
		
		debugShow(drawImg);
	#endif

	#ifdef OUTPUT_DEBUG_IMAGES
		Mat dbg;
		currentImg.copyTo(dbg);
		string qiname = dbgNamer.get_unique_name("alignment_debug_") + ".jpg";
		const Point* p = &quad[0];
		int n = (int) quad.size();
		polylines(dbg, &p, &n, 1, true, 250, 3, CV_AA);

		imwrite(qiname, dbg);
	#endif

	//Disabling size checking
	//if( ! testQuad(quad, .85 * currentImg.size()) ) CV_Error(CV_StsError, "Invalid quad found.");
	if( ! testQuadValidity(quad) ) CV_Error(CV_StsError, "Invalid quad found.");
}
