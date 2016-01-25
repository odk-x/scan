#ifndef _SEGMENT_MASK_H_
#define _SEGMENT_MASK_H_

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <iostream>

#define HORIZONTAL_MASK 0
#define VERTICAL_MASK 1

typedef int (*mask_func)(int x, int y, int w, int h, int dir);

/* This class computes a mask given a mask function */
class SegmentMask {
  protected:
    mask_func func;
    cv::Mat mask;
    int mask_area;
  public:
    SegmentMask() {}
    SegmentMask(const mask_func mf, int w, int h, int dir) : func(mf), mask(h, w, CV_8U, cv::Scalar::all(0)) {
      int px;
      mask_area = 0;
      // iterates through the pixels in the mask, setting each to the output value of
      // the mask function at that coordinate
      for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
          mask.at<uchar>(i, j, 0) = (*func)(j,i,w,h,dir);
          if (px)
            mask_area++;
        }
      }
    }
    const cv::Mat& get_mask(const char segment) {
      return mask;
    }
    int get_mask_area(void) { return mask_area; }
    void print_mask(void) { std::cout << mask << std::endl; }
};

#endif //_SEGMENT_MASK_H_
