#ifndef _DIGIT_FEATURES_H_
#define _DIGIT_FEATURES_H_
#include <dirent.h>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"
#include <stdlib.h>
#include <stdio.h>
#include <iostream>
#include <vector>
#include <cmath>
#include <string>
#include <limits>
#include <time.h>

using namespace std;
using namespace cv;

/* --- BEGIN IMAGE PRE-PROCESSING --- */

static bool BINARY_REMOVE_DOTS = false;
static bool BINARY_AUTOCROP = true;
static bool BINARY_REMOVE_BORDERS = true;
static bool BINARY_THIN = false;

/* --- END IMAGE PRE-PROCESSING --- */

/* --- BEGIN FEATURE EXTRACTION -- */

static int STRUCTURAL_CHARS = 0, GRADIENT_DIR = 1;

static int EXTRACTION_ALG = STRUCTURAL_CHARS;

static int NUM_CLASSES = 11; //All digit classes.
//static int NUM_CLASSES = 3; //Month high digit classes (empty, 0, 1).
//static int NUM_CLASSES = 5; //Day high digit classes (empty, 0, 1, 2, 3).

static int TRAINING_SET_PROP = 75, VALIDATION_SET_PROP = 10, TEST_SET_PROP = 15;

/* --- END FEATURE EXTRACTION -- */

/* FEATURE EXTRACTION FUNCTIONS */
Rect bounding_box(Mat image);
void binary_processed_image(Mat& src);

void remove_dots(Mat& image, int dot_radius);
void remove_top_border(Mat& image);
void remove_bottom_border(Mat& image);
void remove_left_border(Mat& image);
void remove_right_border(Mat& image);
double fraction_remove_stopped(vector<bool> stopped);

void structural_characteristics(Mat binary_image, vector<double>& feature_vector);

void set_num_classes(int num_classes);
void set_extraction_alg(int extraction_alg);

void get_data(cv::Mat &src, vector<double>& features);

#endif // _DIGIT_FEATURES_H_
