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
//testQuad checks that the quad is valid and that it's size falls within the given threshold.
//The size checking isn't that helpful, it can detect a few errors when things are way too large/small
//but it can rule out images unnecessarily.
bool testQuad(const std::vector<cv::Point2f>& quad, const cv::Size& sz, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point2f>& quad, const cv::Rect& r, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point>& quad, const cv::Size& sz, float sizeThresh = .3);
bool testQuad(const std::vector<cv::Point>& quad, const cv::Rect& r, float sizeThresh = .3);
//Checks if the quad is valid
//i.e. if the contour has four points, does not self-intersect and is convex.
bool testQuadValidity(const std::vector<cv::Point2f>& quad);
bool testQuadValidity(const std::vector<cv::Point>& quad);
#endif
