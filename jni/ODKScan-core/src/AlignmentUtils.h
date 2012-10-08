#ifndef ALIGNMENT_UTILS_H
#define ALIGNMENT_UTILS_H

#include <opencv2/core/core.hpp>
cv::Rect resizeRect(const cv::Rect& r, float amount);
std::vector<cv::Point> orderCorners(const std::vector<cv::Point>& corners);
std::vector<cv::Point2f> orderCorners(const std::vector<cv::Point2f>& corners);
std::vector<cv::Point> expandCorners(const std::vector<cv::Point>& corners, double expansionPercent);
cv::Mat quadToTransformation(const std::vector<cv::Point>& foundCorners, const cv::Size& out_image_sz);
cv::Mat quadToTransformation(const std::vector<cv::Point2f>& foundCorners, const cv::Size& out_image_sz);
std::vector<cv::Point> transformationToQuad(const cv::Mat& H, const cv::Size& out_image_sz);
bool testQuad(const std::vector<cv::Point2f>& quad, const cv::Size& sz, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point2f>& quad, const cv::Rect& r, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point>& quad, const cv::Size& sz, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point>& quad, const cv::Rect& r, float sizeThresh = .3);
#endif
