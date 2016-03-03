#ifndef ALIGNER_H
#define ALIGNER_H

#include <opencv2/core/core.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/nonfree/features2d.hpp>

class Aligner
{
	private:
		#ifdef SHOW_MATCHES_WINDOW
			cv::Mat featureSource;
			std::vector<cv::Mat> templateImages;
		#endif
		std::vector<cv::KeyPoint> currentImgKeypoints;
		cv::Mat currentImgDescriptors;

		//The amount an image was actually rescaled factoring in rounding error.
		cv::Point3d trueEfficiencyScale;
	
		cv::Ptr<cv::FeatureDetector> detector;
		cv::Ptr<cv::DescriptorExtractor> descriptorExtractor;
	
		std::vector< std::vector<cv::KeyPoint> > templKeypointsVec;
		std::vector<cv::Mat> templDescriptorsVec;
		
	
	public:
		cv::Mat currentImg;
		std::vector<cv::Size> templImageSizeVec;
	
		Aligner();
		//Load feature data for the given template, caching it to the given featuresFile
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
