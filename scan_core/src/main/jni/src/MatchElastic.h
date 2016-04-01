/** 
 * @file MatchElastic.h 
 * This file contains the function definitions of elastic matching to find
 * regions of interest based on an initial rigid alignment of the camera-
 * captured form with the template. 
 * 
 * @author Faisal Shafait (faisalshafait@gmail.com)  
 * 
 * @version 0.1  
 * 
 */ 
 

#ifndef SCAN_IMAGEPROCESSOR_H
#define SCAN_IMAGEPROCESSOR_H

#include "stdio.h" 
#include <string> 
#include <vector> 
#include <fstream> 
#include <opencv2/core/core.hpp>
#include "opencv2/highgui/highgui.hpp" 
#include "opencv2/imgproc/imgproc.hpp" 

#define DEBUG_OUTPUT 0

void binarizeShafait(cv::Mat &gray, cv::Mat &binary, int w, double k); 
void conCompFast(cv::Mat &img, std::vector<cv::Rect> &rboxes,
             float max_x=1.0, float max_y=1.0, float min_area=0, int type=8);

bool isAlmostSquare(cv::Rect &r);
void smearHorizontal(cv::Mat &imgIn, int thresh);
void smearVertical(cv::Mat &imgIn, int thresh);
bool detectBubbles(std::vector<cv::Rect>& bboxes, std::vector<cv::Rect>& bubbles);
bool detectHoles(std::vector<cv::Rect>& bboxes, std::vector<cv::Rect>& holes);
bool detectNumBoxes(std::vector<cv::Rect>& bboxes, std::vector<cv::Rect>& numBoxes);
bool detectQRCodes(cv::Mat& imgMATbinInv, std::vector<cv::Rect>& qrBoxes);
void despeckle(cv::Mat& imgMATInv, std::vector<cv::Rect>& bboxes);

#endif //SCAN_IMAGEPROCESSOR_H
