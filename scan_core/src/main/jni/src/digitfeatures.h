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

static bool GRAY_REMOVE_DOTS = false;
static bool GRAY_AUTOCROP = true;
static bool GRAY_REMOVE_BORDERS = true;
static bool GRAY_THIN = false;

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
Mat binary_processed_image(Mat& src);
Mat gray_processed_image(Mat src);

void remove_dots(Mat& image, int dot_radius);
void remove_top_border(Mat& image);
void remove_bottom_border(Mat& image);
void remove_left_border(Mat& image);
void remove_right_border(Mat& image);
double fraction_remove_stopped(vector<bool> stopped);

void structural_characteristics(Mat binary_image, vector<double>& feature_vector);
void gradient_directional(Mat gray_unscaled, vector<double>& feature_vector);

void get_structural_characteristics_data_set(vector<Mat>& binary_images, vector<vector<double> >& features);
void get_gradient_directional_data_set(vector<Mat>& gray_images, vector<vector<double> >& features);

void get_processed_images(vector<Mat>& binary_images, vector<Mat>& gray_images, vector<int>& targets, string directory, vector<string>& files);

void set_num_classes(int num_classes);
void set_extraction_alg(int extraction_alg);

void split_data_set(vector<vector<double> >& features, vector<int>& targets, vector<vector<double> >& encoded_targets, vector<Mat>& images, vector<string>& image_names,
			vector<vector<double> >& features_training, vector<int>& targets_training, vector<vector<double> >& encoded_targets_training,
			vector<vector<double> >& features_validation, vector<int>& targets_validation, vector<vector<double> >& encoded_targets_validation,
			vector<vector<double> >& features_testing, vector<int>& targets_testing, vector<vector<double> >& encoded_targets_testing,
			vector<Mat>& images_training, vector<Mat>& images_validation, vector<Mat>& images_testing,
			vector<string>& image_names_training, vector<string>& image_names_validation, vector<string>& image_names_testing);

void get_data_set(string directory, vector<vector<double> >& features_training, vector<int>& targets_training, vector<vector<double> >& encoded_targets_training,
			vector<vector<double> >& features_validation, vector<int>& targets_validation, vector<vector<double> >& encoded_targets_validation,
			vector<vector<double> >& features_testing, vector<int>& targets_testing, vector<vector<double> >& encoded_targets_testing,
			vector<Mat>& images_training, vector<Mat>& images_validation, vector<Mat>& images_testing,
			vector<string>& image_names_training, vector<string>& image_names_validation, vector<string>& image_names_testing);

void get_data(cv::Mat src, vector<double>& features);

void prune_error_samples(vector<vector<double> >& features, vector<vector<double> >& encoded_targets, vector<int>& targets, vector<vector<double> >& pruned_features, vector<vector<double> >& pruned_encoded_targets, vector<int>& pruned_targets, vector<Mat>& images, vector<Mat>& pruned_images, vector<string>& image_names, vector<string>& pruned_image_names);

vector<vector<double> > encode_targets(vector<int>& targets, int num_classes);

#endif // _DIGIT_FEATURES_H_
