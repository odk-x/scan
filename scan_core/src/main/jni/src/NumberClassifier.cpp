#include <opencv2/imgproc/imgproc.hpp>
#include <opencv/highgui.h>
#include <opencv2/opencv.hpp>
#include <opencv/cv.h>

#include "NumberClassifier.h"

#include <iostream>
#include <fstream>

using namespace std;
using namespace cv;

/* This filters out non-image files when recursing through a directory */
int NumberClassifier::filter(const struct dirent *ent) {
    const char *file_name = ent->d_name;
    const char *jpeg = ".jpg";
    return !!strstr(file_name, jpeg);
}

int NumberClassifier::rect_mask(int x, int y, int w, int h, int dir) {
    return 1;
}

NumberClassifier::NumberClassifier() {}

/* Calculates the location and dimensions of each segment's rectangular
 * boundary based on the size of the image and the size of the segments.
 * Assumes that the image is aligned, and centers each segment on the midpoint
 * of the imaginary line between dots */
void NumberClassifier::find_roi(int segment, int iw, int ih, int mw, int mh) {
    int x, y,w,h;
    w = mh;
    h = mw;
    switch(segment) {
    case TOP:
        x = (iw / 2) - (mw / 2);
        y = (ih / 6) - (mh / 2);
        w = mw;
        h = mh;
        break;
    case MIDDLE:
        x = (iw / 2) - (mw / 2);
        y = (ih / 2) - (mh / 2);
        w = mw;
        h = mh;
        break;
    case BOTTOM:
        x = (iw / 2) - (mw / 2);
        y = 5 * (ih / 6) - (mh / 2);
        w = mw;
        h = mh;
        break;
    case TOP_LEFT:
        x = (iw / 4) - (mh / 2);
        y = (ih / 3) - (mw / 2);
        break;
    case TOP_RIGHT:
        x = 3 * (iw / 4) - (mh / 2);
        y = (ih / 3) - (mw / 2);
        break;
    case BOTTOM_LEFT:
        x = (iw / 4) - (mh / 2);
        y = 2 * (ih / 3) - (mw / 2);
        break;
    case BOTTOM_RIGHT:
        x = 3 * (iw / 4) - (mh / 2);
        y = 2 * (ih / 3) - (mw / 2);
        break;
    }
    rois.at(segment) = cv::Rect(x, y, w, h);
}

/* Used to map the output of c_process() to a number */
int NumberClassifier::predict_number(const char guess) {
    switch (guess) {
    case ZERO_SEGMENTS:
        return 0;
    case ONE_SEGMENTS_LEFT:
        return 1;
    case ONE_SEGMENTS_RIGHT:
        return 1;
    case TWO_SEGMENTS:
        return 2;
    case THREE_SEGMENTS:
        return 3;
    case FOUR_SEGMENTS:
        return 4;
    case FIVE_SEGMENTS:
        return 5;
    case SIX_SEGMENTS:
        return 6;
    case SIX_SEGMENTS_PARTIAL:
        return 6;
    case SEVEN_SEGMENTS:
        return 7;
    case EIGHT_SEGMENTS:
        return 8;
    case NINE_SEGMENTS:
        return 9;
    case NINE_SEGMENTS_PARTIAL:
        return 9;
    default:
        return -1;
    }
}

/* Prints out the segment locations and boundaries */
void NumberClassifier::print_rois(void) {
    for (int i = 0; i < NUM_SEGMENTS; i++) {
        std::cout << "ROI #" << i << std::endl;
        std::cout << "Top left: (" << rois.at(i).tl().x << "," << rois.at(i).tl().y << ")" << std::endl;
        std::cout << "Bottom right: (" << rois.at(i).br().x << "," << rois.at(i).br().y << ")" << std::endl;
    }
}

/* Searches for the outline of the box and uses it's location to crop and align the image */
void NumberClassifier::crop_img(cv::Mat& img, int box_height, int box_width) {
    
	imageNumber++;

	cv::Mat inverted;
    cv::bitwise_not ( img, inverted );

    int widthThreshold = box_width;
    int heightThreshold = box_height;

    std::vector<int> top_lines;
    std::vector<int> bottom_lines;
    std::vector<int> left_lines;
    std::vector<int> right_lines;

    //Find the top horizontal line of the box outline
    int total_black = 0;
    int currentMin = 100;
    for (int i=0; i<img.rows/2; i++)
    {
        cv::Rect r = cv::Rect(0, i, img.cols, 1);
        total_black = cv::countNonZero(inverted(r));
        int difference = abs (total_black - box_width);
        if ((difference <= currentMin) && (difference < widthThreshold))
        {
        	currentMin = difference;
            top_lines.push_back(i);
        }
    }

    //Find the bottom horizontal line of the box outline
    total_black = 0;
    currentMin = 100;
    for (int i=img.rows/2; i<img.rows; i++)
    {
        cv::Rect r = cv::Rect(0, i, img.cols, 1);
        total_black = cv::countNonZero(inverted(r));
        int difference = abs (total_black - box_width);

        if ((difference <= currentMin) && (difference < widthThreshold))
        {
        	currentMin = difference;
            bottom_lines.push_back(i);
        }
    }


    
    //Find the left vertical line of the box outline
    total_black = 0;
    currentMin = 100;
    for (int i=0; i<img.cols/2; i++)
    {
        cv::Rect r = cv::Rect(i, 0, 1, img.rows);
        total_black = cv::countNonZero(inverted(r));
        int difference = abs (total_black - box_height);
        if ((difference <= currentMin) && (difference < heightThreshold))
        {
        	currentMin = difference;
            left_lines.push_back(i);
        }
    }

    
    //Find the bottom vertical line of the box outline
    total_black = 0;
    currentMin = 100;
    for (int i=img.cols/2; i<img.cols; i++)
    {
        cv::Rect r = cv::Rect(i, 0, 1, img.rows);
        total_black = cv::countNonZero(inverted(r));
        
        int difference = abs (total_black - box_height);

        if ((difference <= currentMin) && (difference < heightThreshold))
        {
        	currentMin = difference;
        	right_lines.push_back(i);
        }
    }

    int top_line = 0;
    int bottom_line = img.rows;
    int left_line = 0;
    int right_line = img.cols;

    //Finding TOP LINE
    //if we didn't find any candidate top lines then use the top of the image
    if (top_lines.size() == 0)
    {
    	top_line = 0;
    }
	//if we only found one candidate top line then use that
    else if (top_lines.size() == 1)
	{
		top_line = top_lines[0];
	}
	//if there is more than one candidate line, use the bottom lines to choose the best
	else
	{
		int minDifference = 100;

		//Start with just the bottom of the image
		for (size_t i = 0; i < top_lines.size(); i++)
		{
			int candidateHeight = bottom_line - top_lines[i];

			int difference = abs (candidateHeight - box_height);
			if (difference < minDifference)
			{
				minDifference = difference;
				top_line = top_lines[i];
			}
		}

		if (bottom_lines.size() != 0)
		{
			for (size_t i = 0; i < top_lines.size(); i++)
			{
				for (size_t j = 0; j < bottom_lines.size(); j++)
				{
					int candidateHeight = bottom_lines[j] - top_lines[i];
					int difference = abs (candidateHeight - box_height);
					if (difference < minDifference)
					{
						minDifference = difference;
						top_line = top_lines[i];
					}
				}
			}
		}
	}

    //Finding BOTTOM LINE
    if (bottom_lines.size() == 0)
	{
		bottom_line = img.rows;
	}
	//if we only found one candidate bottom line then use that
	else if (bottom_lines.size() == 1)
	{
		bottom_line = bottom_lines[0];
	}
	//if there is more than one candidate line, use the top lines to choose the best
	else
	{
		int minDifference = 100;

		//Start with just the bottom of the image
		for (size_t i = 0; i < bottom_lines.size(); i++)
		{
			int candidateHeight = bottom_lines[i] - top_line;

			int difference = abs (candidateHeight - box_height);
			if (difference < minDifference)
			{
				minDifference = difference;
				bottom_line = bottom_lines[i];
			}
		}
	}

    //Finding LEFT LINE
    //if we didn't find any candidate left lines then use the edge of the image
    if (left_lines.size() == 0)
    {
    	left_line = 0;
    }
	//if we only found one candidate top line then use that
    else if (left_lines.size() == 1)
	{
    	left_line = left_lines[0];
	}
	//if there is more than one candidate line, use the right lines to choose the best
	else
	{
		int minDifference = 100;

		//Start with just the bottom of the image
		for (size_t i = 0; i < left_lines.size(); i++)
		{
			int candidateHeight = right_line - left_lines[i];

			int difference = abs (candidateHeight - box_width);
			if (difference < minDifference)
			{
				minDifference = difference;
				left_line = left_lines[i];
			}
		}

		if (right_lines.size() != 0)
		{
			for (size_t i = 0; i < left_lines.size(); i++)
			{
				for (size_t j = 0; j < right_lines.size(); j++)
				{
					int candidateHeight = right_lines[j] - left_lines[i];
					int difference = abs (candidateHeight - box_width);
					if (difference < minDifference)
					{
						minDifference = difference;
						left_line = left_lines[i];
					}
				}
			}
		}
	}

    //Finding RIGHT LINE
    //if we didn't find any candidate right lines then use the edge of the image
    if (right_lines.size() == 0)
    {
    	right_line = img.cols;
    }
	//if we only found one candidate top line then use that
    else if (right_lines.size() == 1)
	{
    	right_line = right_lines[0];
	}
	//if there is more than one candidate line, use the right lines to choose the best
    else
	{
		int minDifference = 100;

		//Start with just the bottom of the image
		for (size_t i = 0; i < right_lines.size(); i++)
		{
			int candidateHeight = right_lines[i] - left_line;

			int difference = abs (candidateHeight - box_width);
			if (difference < minDifference)
			{
				minDifference = difference;
				right_line = right_lines[i];
			}
		}
	}

    //debugging
    /*
    cv::line(img, cv::Point(0, top_line), cv::Point(img.cols, top_line), cv::Scalar(125), 1, 1);
    cv::line(img, cv::Point(0, bottom_line), cv::Point(img.cols, bottom_line), cv::Scalar(125), 1, 1);
    cv::line(img, cv::Point(left_line, 0), cv::Point(left_line, img.rows), cv::Scalar(125), 1, 1);
    cv::line(img, cv::Point(right_line, 0), cv::Point(right_line, img.rows), cv::Scalar(125), 1, 1);

	stringstream ss;
	ss << c_dir << imageNumber << "_binary.jpg";
	std::string imgName = ss.str();
	stringstream ss2;
	ss2 << c_dir << imageNumber << "_cropped.jpg";
	std::string queryPixelsName = ss2.str();
	*/

	//cv::imwrite(imgName, img);

    int horizontal_top = top_line + 3;
    int horizontal_bottom = bottom_line -1 ;
    int vertical_top = left_line + 3;
    int vertical_bottom = right_line -1 ;
    
    int new_width = vertical_bottom - vertical_top;
    int new_height = horizontal_bottom - horizontal_top;
    img = img(cv::Rect(vertical_top, horizontal_top, new_width, new_height));

	//Trying to eliminate dots to see if that helps with 1, 4, 7, 9
	cv::bitwise_not ( img, inverted );
	vector<vector<Point> > contours;
	vector<Vec4i> hierarchy;

	findContours(inverted, contours, hierarchy, CV_RETR_LIST, CV_CHAIN_APPROX_SIMPLE, Point(0, 0) );

	vector<vector<Point> > contours_poly( contours.size() );
	for( int i = 0; i< contours.size(); i++ )
	{
		approxPolyDP( Mat(contours[i]), contours_poly[i], 3, true );
		Rect boundRect = boundingRect( Mat(contours_poly[i]) );

		if (boundRect.width * boundRect.height < 25)
		{
			Scalar color = Scalar(255,255,255);
			drawContours(img, contours, i, color, 1, 8, hierarchy, 0, Point() );
		}
	}

    //cv::imwrite(queryPixelsName, img);
}

/* Converts the image to black/white, crops it, resizes it */
void NumberClassifier::pre_process(cv::Mat& img, int classifier_height, int classifier_width)
{
	cv::resize(img, img, cv::Size(25,35), 0, 0, cv::INTER_AREA);
	cv::Mat img_gray = img;
    if (img.channels() > 1)
        cv::cvtColor(img, img_gray, CV_BGR2GRAY);
    std::vector<cv::Mat> channels;
    split(img, channels);
    int threshold = (int)cv::mean(channels[0])[0];
    cv::Mat img_bin;
    cv::threshold(img_gray, img_bin, threshold, 255, cv::THRESH_BINARY);
    img = img_bin;

    crop_img(img, classifier_height, classifier_width);
    cv::resize(img, img, cv::Size(img_w,img_h), 0, 0, cv::INTER_AREA);
}

/* Prints the results of the classification */
void NumberClassifier::print_results(void) {
    int total_correct = 0;
    for (int i = 0; i < 10; i++) {
        std::cout << "#" << i << ": " << correct.at(i) << "/" << guesses.at(i) << std::endl;
        total_correct += correct.at(i);
    }
    float percent_correct = total_correct/c_numbers;
    std::cout << "Total: " << total_correct << "/" << c_numbers << std::endl;
}

Json::Value NumberClassifier::classify_segment(const cv::Mat& img, const cv::Point& item_location, const string weight_file_path, int classifier_height, int classifier_width) {

	Json::Value output;
	cv::Mat query_pixels;
  vector<double> features;

  ifstream infile(weight_file_path.c_str());
  int num_hidden_units;

  infile >> NUM_CLASSES;
  infile >> num_hidden_units;
  infile >> EXTRACTION_ALG;
  set_num_classes(NUM_CLASSES);
  set_extraction_alg(EXTRACTION_ALG);

	//add some pixels to give it a little buffer
	int expandedHeight = classifier_height + (0.25*classifier_height);
	int expandedWidth = classifier_width + (0.25*classifier_width);

	getRectSubPix(img, cv::Size(expandedWidth, expandedHeight), item_location, query_pixels);

	//debugging
	//stringstream ss;
	//ss << c_dir << item_location.x << "_" << item_location.y << "_binary.jpg";
	//std::string imgName = ss.str();
	//stringstream ss2;
	//ss2 << c_dir << item_location.x << "_" << item_location.y << "_query_pixels.jpg";
	//std::string queryPixelsName = ss2.str();
	//cv::imwrite(queryPixelsName, query_pixels);

	cv::resize(query_pixels, query_pixels, cv::Size(50,70), 0, 0, cv::INTER_AREA);

  get_data(query_pixels, features);

	vector<vector<double> > W(features.size() + 1, vector<double>(num_hidden_units, 0));
	vector<vector<double> > V(num_hidden_units + 1, vector<double>(NUM_CLASSES, 0));

	for(int i = 0; i < W.size(); i++)
	{
		for(int j = 0; j < W[i].size(); j++)
		{
			infile >> W[i][j];
		}
	}
	for(int i = 0; i < V.size(); i++)
	{
		for(int j = 0; j < V[i].size(); j++)
		{
			infile >> V[i][j];
		}
	}

	int guess = mlp_two_layer_predict_class(features, W, V);

  // Blank numbers are classified as "10". Pass that out as
  // an empty string instead.
  if (guess == 10) {
	  output["classification"] = " ";
	  output["value"] = " ";
	  output["type"] = "string";
  } else {
	  output["classification"] = guess;
	  output["value"] = guess;
	  output["type"] = "number";
  }
	output["confidence"] = 1;

	return output;
}
