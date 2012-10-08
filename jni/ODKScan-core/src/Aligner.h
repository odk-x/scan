#ifndef ALIGNER_H
#define ALIGNER_H

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>

/*
Form alignment can be broken apart into 3 parts, feature detection, feature extraction, and feature matching. The best option I've found so far for detection/extraction is the grid adapted SURF feature detector, with the SURF feature extractor. I've parameterized SURF to only use one octave since order of magnitude scale differences shouldn't occur in form images. If they do, the form is probably too large/small in the picture to capture all the information on it. More octaves might help with larger features, I'll have to research this more...
The grid adapted feature detector is good for limiting the number of key-points found so that the algorithm executes in a reasonable amount of time.
Feature matching can be pretty easily switched between the brute force matcher (which is a bit more reliable) and the flann matcher (which is a bit faster). Additionally, I'm using cross check matching code from one of the OpenCV example programs.
*/
class Aligner
{
	private:
		std::vector<cv::KeyPoint> currentImgKeypoints;
		cv::Mat currentImgDescriptors;
		cv::Point3d trueEfficiencyScale;
	
		cv::Ptr<cv::FeatureDetector> detector;
		cv::Ptr<cv::DescriptorExtractor> descriptorExtractor;
	
		std::vector< std::vector<cv::KeyPoint> > templKeypointsVec;
		std::vector<cv::Mat> templDescriptorsVec;
		
	
	public:
		cv::Mat currentImg;
		std::vector<cv::Size> templImageSizeVec;
	
		Aligner();
		
		void loadFeatureData(const std::string& imagePath,
		                     const std::string& jsonPath, const std::string& featuresFile) throw(cv::Exception);
		
		//This will be a bit slow because it resizes the image and
		//computes its features and descriptors.
		//Calling this is a prerequisite for DetectForm and alignFormImage
		void setImage( const cv::Mat& img );
		
		//Returns the index of the feature data to use
		//alignFormImage will default to using the last index detected
		size_t detectForm() const throw(cv::Exception);
		
		void alignFormImage( cv::Mat& aligned_image,
		                     const cv::Size& aligned_image_sz,
		                     size_t formIdxArg ) throw(cv::Exception);
};

#endif
