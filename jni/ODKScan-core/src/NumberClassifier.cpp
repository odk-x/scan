#include <opencv2/imgproc/imgproc.hpp>
#include <opencv/highgui.h>
#include <opencv2/opencv.hpp>
#include <opencv/cv.h>

#include "NumberClassifier.h"

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
void NumberClassifier::crop_img(cv::Mat& img, int target_w, int target_h) {
    
    cv::Mat inverted;
    cv::bitwise_not ( img, inverted );
    
    //Find the top horizontal line of the box outline
    int total_black = 0;
    int currentMax = 0;
    int black_line = 0;
    for (int i=0; i<img.rows/2; i++)
    {
        cv::Rect r = cv::Rect(0, i, img.cols, 1);
        total_black = cv::countNonZero(inverted(r));
        
        if (total_black > currentMax)
        {
            currentMax = total_black;
            black_line = i;
        }
    }
    int horizontal_top = black_line;
    //cv::line(img, cv::Point(0, black_line), cv::Point(img.cols, black_line), cv::Scalar(0,0,125), 1, 1);

    //Find the bottom horizontal line of the box outline
    total_black = 0;
    currentMax = 0;
    black_line = 0;
    for (int i=img.rows/2; i<img.rows-1; i++)
    {
        cv::Rect r = cv::Rect(0, i, img.cols, 1);
        total_black = cv::countNonZero(inverted(r));
        
        if (total_black > currentMax)
        {
            currentMax = total_black;
            black_line = i;
        }
    }
    int horizontal_bottom = black_line;
    //cv::line(img, cv::Point(0, black_line), cv::Point(img.cols, black_line), cv::Scalar(0,0,125), 1, 1);
    
    //Find the top vertical line of the box outline
    total_black = 0;
    currentMax = 0;
    black_line = 0;
    for (int i=0; i<img.cols/2; i++)
    {
        cv::Rect r = cv::Rect(i, 0, 1, img.rows);
        total_black = cv::countNonZero(inverted(r));
        
        if (total_black > currentMax)
        {
            currentMax = total_black;
            black_line = i;
        }
    }
    int vertical_top = black_line;
    //cv::line(img, cv::Point(black_line, 0), cv::Point(black_line, img.rows), cv::Scalar(0,0,125), 1, 1);
    
    //Find the bottom vertical line of the box outline
    total_black = 0;
    currentMax = 0;
    black_line = 0;
    for (int i=img.cols/2; i<img.cols-1; i++)
    {
        cv::Rect r = cv::Rect(i, 0, 1, img.rows);
        total_black = cv::countNonZero(inverted(r));
        
        if (total_black > currentMax)
        {
            currentMax = total_black;
            black_line = i;
        }
    }
    int vertical_bottom = black_line;
    //cv::line(img, cv::Point(black_line, 0), cv::Point(black_line, img.rows), cv::Scalar(0,0,125), 1, 1);
    
    horizontal_top += 2;
    horizontal_bottom -= 2;
    vertical_top += 2;
    vertical_bottom -= 2;
    
    int new_width = vertical_bottom - vertical_top;
    int new_height = horizontal_bottom - horizontal_top;
    img = img(cv::Rect(vertical_top, horizontal_top, new_width, new_height));
}

/* Converts the image to black/white, crops it, resizes it */
void NumberClassifier::pre_process(cv::Mat& img)
{
    cv::Mat img_gray = img;
    if (img.channels() > 1)
        cv::cvtColor(img, img_gray, CV_BGR2GRAY);
    std::vector<cv::Mat> channels;
    split(img, channels);
    int threshold = (int)cv::mean(channels[0])[0];
    threshold = threshold-20;
    cv::Mat img_bin;
    cv::threshold(img_gray, img_bin, threshold, 255, cv::THRESH_BINARY);
    img = img_bin;
    //cv::imwrite(c_dir + "W_PROCESSED.jpg", img);
    crop_img(img, img_w, img_h);
    //cv::imwrite(c_dir + "X_PROCESSED.jpg", img);
    cv::resize(img, img, cv::Size(img_w,img_h), 0, 0, cv::INTER_AREA);
    //cv::imwrite(c_dir + "Y_PROCESSED.jpg", img);
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

/* Iterates through the classification directory, passing each image to the
 * c_process() function which performs the actual classification */
//Currently not used in ODK Scan
void NumberClassifier::classify(void) {
    int n = c_numbers;
    if (n < 0) {
        LOGI("Error opening directory");
    }
    while (n--) {
        std::string img_name = std::string(c_list[n]->d_name);
        int img_num = img_name.at(0) - 0x30;
        cv::Mat img = cv::imread(c_dir + img_name, CV_LOAD_IMAGE_COLOR);
        if (img.empty())
        	LOGI("Image not loaded");
        else {
            pre_process(img);
            int guess = c_process(img);
            if (img_num == guess)
                correct.at(img_num)++;
            else
                std::cerr << "Guessed " << img_name << " as " << guess << std::endl;
            guesses.at(img_num)++;
        }
        delete c_list[n];
    }
    delete c_list;
}

int NumberClassifier::classify_segment(const cv::Mat& img, const cv::Point& item_location) {
	cv::Mat query_pixels;
	getRectSubPix(img, cv::Size(img_w, img_h), item_location, query_pixels);
	pre_process(query_pixels);
	int guess = c_process(query_pixels);
	return guess;
}

/* Counts the number of black pixels in the given segment of the image */
int NumberClassifier::get_black_pixels(const cv::Mat& img, int segment)
{
    cv::Mat i_segment = img(rois.at(segment));
    if ((1 << segment) & (TOP_BIT | MIDDLE_BIT | BOTTOM_BIT))
    {
        bitwise_and(i_segment, h_mask.get_mask(segment), i_segment);
    }
    else if ((1 << segment) & (TOP_LEFT_BIT | TOP_RIGHT_BIT | BOTTOM_LEFT_BIT | BOTTOM_RIGHT_BIT))
    {
        bitwise_and(i_segment, v_mask.get_mask(segment), i_segment);
    }

    //temporarily hardcoding these values. Need to change them later.
    total_pixels = 15*15;

    int returnNumber = total_pixels - cv::countNonZero(i_segment);

    //debugging
    //std::stringstream ss;
    //ss << "segment " << segment << " total_pixels " << total_pixels << " number black pixels " << returnNumber;
    //std::string str = ss.str();
    //const char * c = str.c_str();
    //LOGI(c);
    //end debugging

    return returnNumber;
}

/* This function iterates through a training directory and calls
 * t_process() on each image in the directory. Can be used by any
 * ML classifier as a generic train function */
void NumberClassifier::train(void) {
	int n = t_numbers;
    if (n < 0) {
    	LOGI("Error opening number training directory");
    }
    while (n--) {
        std::string img_name = std::string(t_list[n]->d_name);
        int img_num = img_name.at(0) - 0x30;

        //debugging
        //std::stringstream ss;
        //ss << t_dir << img_name;
        //std::string str = ss.str();
        //const char * c = str.c_str();
        //LOGI(c);
        //end debugging

        cv::Mat img = cv::imread(t_dir + img_name, CV_LOAD_IMAGE_COLOR);
        if (img.empty())
            LOGI("Number training image not loaded");

        else {
            pre_process(img);
            t_process(img, img_num);
        }
        delete t_list[n];
    }
    delete t_list;
}

/* This adds the number of pixels in each segment to the stats
 * object which keeps track of the mean and variance of each
 * segment for each number during training */
void NumberClassifier::t_process(const cv::Mat& img, int img_num)
{
    for (int i = 0; i < NUM_SEGMENTS; i++) {
        int black_pixels = get_black_pixels(img, i);
        stats.add_seg(img_num, i, black_pixels);

        //debugging
        //std::stringstream ss;
        //ss << "img num " << img_num << " i " << i << " black pixels " << black_pixels;
        //std::string str = ss.str();
        //const char * c = str.c_str();
        //LOGI(c);
        //end debugging
    }
}

/* This calculates the probability that the current image is 0-9
 * (thus it calculates 10 different probabilities). Assumes that the
 * pixel counts form a normal distribution */
int NumberClassifier::c_process(const cv::Mat& img) {

	//stats.print_stats();

	float max_prod = 0;
    int max_num = -1;
    float prod = 0;
    for (int num = 0; num < 10; num++) {
        prod = 1.0;
        for (int seg = 0; seg < NUM_SEGMENTS; seg++) {
            int black_pixels = get_black_pixels(img, seg);
            float newProd = stats.get_prob(num, seg, black_pixels);
            prod *= newProd;

            //debugging
            //std::stringstream ss;
            //ss << "seg " << seg << " get_prob " << newProd << " black pixels " << black_pixels;
            //std::string str = ss.str();
            //const char * c = str.c_str();
            //LOGI(c);
            //end debugging

        }
        if (prod > max_prod) {
            max_prod = prod;
			max_num = num;
        }
    }
    return max_num;
}
