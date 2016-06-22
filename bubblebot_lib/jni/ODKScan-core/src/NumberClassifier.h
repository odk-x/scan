#ifndef _NUMBER_CLASSIFIER_H_
#define _NUMBER_CLASSIFIER_H_

#include <dirent.h>
#include <vector>
#include <string>

#include "configuration.h"
#include "SegmentMask.h"
#include "PixelStats.h"
#include "two_layer_mlp.h"
#include <json/json.h>

// Number of segments per image
#define NUM_SEGMENTS 7

// ID number for each segment
#define TOP 0x0
#define MIDDLE 0x1
#define BOTTOM 0x2
#define TOP_LEFT 0x3
#define TOP_RIGHT 0x4
#define BOTTOM_LEFT 0x5
#define BOTTOM_RIGHT 0x6

// Bitfield representation of each segment
#define TOP_BIT (1 << TOP)
#define MIDDLE_BIT (1 << MIDDLE)
#define BOTTOM_BIT (1 << BOTTOM)
#define TOP_LEFT_BIT (1 << TOP_LEFT)
#define TOP_RIGHT_BIT (1 << TOP_RIGHT)
#define BOTTOM_LEFT_BIT (1 << BOTTOM_LEFT)
#define BOTTOM_RIGHT_BIT (1 << BOTTOM_RIGHT)

// Segments that make up each number
#define ZERO_SEGMENTS ( TOP_BIT | BOTTOM_BIT | TOP_LEFT_BIT | TOP_RIGHT_BIT | BOTTOM_LEFT_BIT | BOTTOM_RIGHT_BIT )
#define ONE_SEGMENTS_LEFT ( TOP_LEFT_BIT | BOTTOM_LEFT_BIT )
#define ONE_SEGMENTS_RIGHT ( TOP_RIGHT_BIT | BOTTOM_RIGHT_BIT )
#define TWO_SEGMENTS ( TOP_BIT | TOP_RIGHT_BIT | MIDDLE_BIT | BOTTOM_LEFT_BIT | BOTTOM_BIT )
#define THREE_SEGMENTS ( TOP_BIT | MIDDLE_BIT | BOTTOM_BIT | TOP_RIGHT_BIT | BOTTOM_RIGHT_BIT )
#define FOUR_SEGMENTS ( TOP_LEFT_BIT | TOP_RIGHT_BIT | MIDDLE_BIT | BOTTOM_RIGHT_BIT )
#define FIVE_SEGMENTS ( TOP_BIT | TOP_LEFT_BIT | MIDDLE_BIT | BOTTOM_RIGHT_BIT | BOTTOM_BIT )
#define SIX_SEGMENTS_PARTIAL ( TOP_LEFT_BIT | MIDDLE_BIT | BOTTOM_LEFT_BIT | BOTTOM_RIGHT_BIT | BOTTOM_BIT )
#define SIX_SEGMENTS ( TOP_BIT | SIX_SEGMENTS_PARTIAL )
#define SEVEN_SEGMENTS ( TOP_BIT | TOP_RIGHT_BIT | BOTTOM_RIGHT_BIT )
#define EIGHT_SEGMENTS ( TOP_BIT | MIDDLE_BIT | BOTTOM_BIT | TOP_LEFT_BIT | TOP_RIGHT_BIT | BOTTOM_LEFT_BIT | BOTTOM_RIGHT_BIT )
#define NINE_SEGMENTS_PARTIAL ( TOP_BIT | TOP_LEFT_BIT | TOP_RIGHT_BIT | MIDDLE_BIT | BOTTOM_RIGHT_BIT )
#define NINE_SEGMENTS ( NINE_SEGMENTS_PARTIAL | BOTTOM_BIT )

typedef int (*filter_func)(const struct dirent *ent);

class NumberClassifier
{
protected:
    
    PixelStats stats;           // Classifier object
    
    struct dirent **t_list;     // list of image names
    struct dirent **c_list;
    
    void t_process(const cv::Mat& img, int img_num);
    int c_process(const cv::Mat &img);
    
    std::string t_dir;
    std::string c_dir;    // name of classify directory
    
    int c_numbers;              // number of images to classify
    int t_numbers;
    
    int img_w;                  // images resized to this width
    int img_h;                  // images resized to this height
    
    int imageNumber;

    SegmentMask h_mask;         // defines a mask shape for the vertical segments
    SegmentMask v_mask;         // defines a mask shape for the horiz. segments
    
    std::vector<cv::Rect> rois; // rectangles for the segment locations
    std::vector<int> guesses;   // keeps track of guesses
    std::vector<int> correct;   // keeps track of correct guesses
    /*const*/ int total_pixels;     // total pixels in each segment

    void find_roi(int segment, int iw, int ih, int mw, int mh);
    int predict_number(const char guess);
    void pre_process(cv::Mat& img, int classifier_height, int classifier_width);
    int get_black_pixels(const cv::Mat& img, int segment); 
    void crop_img(cv::Mat& img, int box_height, int box_width);
    
    static int filter(const struct dirent *d);  // Filters out files that are not images
    static int rect_mask(int x, int y, int w, int h, int dir); // Defines a rectangular mask for segments
    
    
public:
    NumberClassifier();
    NumberClassifier ( std::string& classify_dir,  std::string& train_dir, int iw, int ih, int mw, int mh):
    	c_dir(classify_dir), img_w(iw), img_h(ih),
        t_dir(train_dir),
        imageNumber(0),
        guesses(10, 0),
        correct(10, 0),
        h_mask(&NumberClassifier::rect_mask, mw, mh, HORIZONTAL_MASK),
        v_mask(&NumberClassifier::rect_mask, mh, mw, VERTICAL_MASK),
        rois(NUM_SEGMENTS),
        total_pixels(h_mask.get_mask_area())
        {
            c_numbers = scandir(classify_dir.c_str(), &c_list, &NumberClassifier::filter, alphasort);
            t_numbers = scandir(train_dir.c_str(), &t_list, &NumberClassifier::filter, alphasort);
            for (int i = 0; i < NUM_SEGMENTS; i++)
            {
                find_roi(i, iw, ih, mw, mh);
            }
        };
    
    Json::Value classify_segment(const cv::Mat& img, const cv::Point& item_location, const string weight_file_path, int classifier_height, int classifier_width);
    void print_results(void);
    void print_rois(void);
};

#endif //_NUMBER_CLASSIFIER_H_
