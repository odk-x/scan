/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

#include "configuration.h"
#include "Addons.h"
#include "AlignmentUtils.h"
#include "SegmentAligner.h"

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <iostream>

//#define OUTPUT_DEBUG_IMAGES
#ifdef OUTPUT_DEBUG_IMAGES
#include "NameGenerator.h"
NameGenerator alignmentNamer("debug_segment_images/", false);
#endif


using namespace std;
using namespace cv;

// Try to distil the maximum quad (4 point contour) from a convex contour of many points.
// if none is found maxQuad will not be altered.
float maxQuadSimplify(vector <Point>& contour, vector<Point>& maxQuad, float current_approx_p){
	
	float area = 0;
	
	float arc_len = arcLength(Mat(contour), true);
	while (current_approx_p < 1) {
		vector <Point> approx;
		approxPolyDP(Mat(contour), approx, arc_len * current_approx_p, true);
		if (approx.size() == 4){
			maxQuad = approx;
			area = fabs(contourArea(Mat(approx)));
			break;
		}
		current_approx_p += .01;
	}
	return area;
}
//Find the largest quad contour in img
//Warning: destroys img
vector<Point> findMaxQuad(Mat& img, float approx_p_seed = 0){
	vector<Point> maxRect;
	vector < vector<Point> > contours;
	// Find all external contours of the image
	findContours(img, contours, CV_RETR_EXTERNAL, CV_CHAIN_APPROX_SIMPLE);

	float maxContourArea = 0;
	// Iterate through all detected contours
	for (size_t i = 0; i < contours.size(); ++i) {
		vector<Point> contour, quad;
		convexHull(Mat(contours[i]), contour);
		float area = maxQuadSimplify(contour, quad, approx_p_seed);

		if (area > maxContourArea) {
			maxRect = quad;
			maxContourArea = area;
		}
	}
	return maxRect;
}
//Sum the pixels along the line defined by the start and end points.
template <class T>
int lineSum(const Mat& img, Point_<T>& start, Point_<T>& end) {
	
	int sum = 0;
	
	Point_<T> vector = (end - start);
	int length = norm(vector);
	float portion = 1.0 / length;

	for(int i = 0; i < length; i++) {
		Point_<T> point = start + vector * (portion * i);
		sum += img.at<uchar>(point);
	}

	return sum;
}
//Finds a minimum energy line that crosses the image
//weighted near the region of interest.
//A and B are the start and end points of the line.
//The flip variable sets which side of the roi to look for a line on.
//The transpose flag virtually transposes the image so you can look for vertical lines.
template <class T>
void findLines(const Mat& img, Point_<T>& start, Point_<T>& end, const Rect& roi, bool flip, bool transpose) {
	int hSpan;
	int range, midpoint;
	float maxSlope = 0.10;

	if(transpose){
		hSpan = img.rows;
		midpoint = roi.x;
		if(flip) midpoint += roi.width;
		range = roi.x;
	}
	else{
		hSpan = img.cols;
		midpoint = roi.y;
		if(flip) midpoint += roi.height;
		range = roi.y;
	}
	//This is the maximum sum of a line crossing the image.
	int maxSum = 255 * hSpan;
	//The greater param is the more results are weighted towards the orginal position.
	//When decreasing this watch out for prompts with lines close to the borders (e.g. bub_* prompts).
	//There is a trade-off because lowering the param yield better alignments when it doesn't pick up the wrong line.
	double param = 0.4;

	float maxSsdFromMidpoint = 2*range*range;
	
	//Contracting the roi can make it less likely we choose the wrong line when the segment is narrow.
	int roiInset = 1;

	int minLs = INT_MAX;
	for(int i = midpoint - range; i <= midpoint + range; i++) {
		for(int j = MAX(i-hSpan*maxSlope, midpoint - range); j <= MIN(i+hSpan*maxSlope, midpoint + range); j++) {
			//The initial line sum is the weighting applied
			float ssdFromMidpoint = (i - midpoint)*(i - midpoint) + (j - midpoint)*(j - midpoint);
			int ls = param * maxSum * ssdFromMidpoint / maxSsdFromMidpoint;
			Point_<T> startPt, endPt;
			if(transpose){
				startPt = Point_<T>(i, roi.y + roiInset);
				endPt = Point_<T>(j, roi.y + roi.height - roiInset);
			}
			else{
				startPt = Point_<T>(roi.x  + roiInset, i);
				endPt = Point_<T>(roi.x + roi.width - roiInset, j);
			}

			ls += lineSum(img, startPt, endPt);
			if( ls < minLs ){
				start = startPt;
				end = endPt;
				minLs = ls;
			}
		}
	}
}
//Finds the intersection of the lines defined by (P1,P2) and (P3,P4)
template <class T>
inline Point_<T> findIntersection(const Point_<T>& P1, const Point_<T>& P2,
						const Point_<T>& P3, const Point_<T>& P4){
	// From determinant formula here:
	// http://en.wikipedia.org/wiki/Line_intersection
	// "Note that the intersection point is for the infinitely long lines defined by the points,
	// rather than the line segments between the points, 
	// and can produce an intersection point beyond the lengths of the line segments."
	double denom = (P1.x - P2.x) * (P3.y - P4.y) - (P1.y - P2.y) * (P3.x - P4.x);
	return Point_<T>(
		( (P1.x * P2.y - P1.y * P2.x) * (P3.x - P4.x) -
		  (P1.x - P2.x) * (P3.x * P4.y - P3.y * P4.x) ) / denom,
		( (P1.x * P2.y - P1.y * P2.x) * (P3.y - P4.y) -
		  (P1.y - P2.y) * (P3.x * P4.y - P3.y * P4.x) ) / denom);
}
//Deprecated
void refineCorners(const Mat& img, vector< Point2f >& quad){
	TermCriteria termcrit(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.03);
	cornerSubPix(img, quad, Size(1,1), Size(-1, -1), termcrit);
}
//Converts the types of points in the quad.
template <class T, class U>
void convertQuad(const vector< Point_<T> >& quad, vector< Point_<U> >& outQuad){
	for(size_t i = 0; i<4; i++){
		outQuad.push_back( Point_<U>(quad[i].x, quad[i].y) );
	}
}
/*
Pseudo-code description of findSegment:
1.	Find lines along the edges of the bounding box
2.	Find their intersections
3.	Find the transformation from the 4 intersection points to the rectangle defined in the template,
	then use it to transform the segment into the rectangle.
*/
template <class T>
void findSegmentImpl(const Mat& img, const Rect& roi, vector< Point_<T> >& outQuad){

	//quad finding modes:
	//There is a tradeoff with using intersections.
	//It seems to work better on messy segments, however,
	//on segments with multiple min energy lines we are more likely
	//to choose the wrong line than with the contour method.
	#define QUAD_FIND_INTERSECTION 0
	#define QUAD_FIND_CONTOURS 1
	
	#define QUAD_FIND_MODE QUAD_FIND_INTERSECTION
	
	Mat imgThresh, temp_img, temp_img2;
	
	//A binary image gradient is applied here.
	//You can use findLines without it, but it
	//generally leads to better performance.
	//However, it may lead to a loss in percision.
	//i.e. you're more likely to find the right line, but the coords will be slightly off.
	int blurSize = 40;
	blur(img, temp_img, Size(blurSize, blurSize));
	imgThresh = (img - temp_img) > 0;
	//White out the middle of the segment to prevent it from interfering.
	Rect contractedRoi = resizeRect(roi, .7);
	imgThresh(contractedRoi) = Scalar::all(255);

	Point_<T> A1, B1, A2, B2, A3, B3, A4, B4;
	findLines(imgThresh, A1, B1, roi, false, true);
	findLines(imgThresh, A2, B2, roi, false, false);
	findLines(imgThresh, A3, B3, roi, true, true);

	//This line is making it crash
	findLines(imgThresh, A4, B4, roi, true, false);

	#if QUAD_FIND_MODE == QUAD_FIND_INTERSECTION
		vector< Point_<T> > quad;
		quad.push_back(findIntersection(A1, B1, A2, B2));
		quad.push_back(findIntersection(A2, B2, A3, B3));
		quad.push_back(findIntersection(A3, B3, A4, B4));
		quad.push_back(findIntersection(A4, B4, A1, B1));
		outQuad = quad;
	#elif QUAD_FIND_MODE == QUAD_FIND_CONTOURS
		line( imgThresh, A1, B1, Scalar::all(0), 1, 4);
		line( imgThresh, A2, B2, Scalar::all(0), 1, 4);
		line( imgThresh, A3, B3, Scalar::all(0), 1, 4);
		line( imgThresh, A4, B4, Scalar::all(0), 1, 4);
		Mat imgThresh2;
		imgThresh.copyTo(imgThresh2);
		vector< Point > quad = findMaxQuad(imgThresh2, 0);
		//This works poorly on large rectangles
		//#define EXPANSION_PERCENTAGE .01
		//quad = expandCorners(quad, EXPANSION_PERCENTAGE);
		quad = orderCorners(quad);
		convertQuad(quad, outQuad);
	#endif
	
	//refineCorners(img, quad);
	#ifdef OUTPUT_DEBUG_IMAGES

		Mat dbg_out, dbg_out2;
		imgThresh.copyTo(dbg_out);

		vector< Point > roundQuad;

		roundQuad.push_back(quad[0]);
		roundQuad.push_back(quad[1]);
		roundQuad.push_back(quad[2]);
		roundQuad.push_back(quad[3]);

		const Point* p = &roundQuad[0];
		int n = (int) quad.size();
		polylines(dbg_out, &p, &n, 1, true, Scalar::all(100), 1, CV_AA);
		//debugShow(dbg_out);

		string segfilename = alignmentNamer.get_unique_name("alignment_debug_");
		segfilename.append(".jpg");
		imwrite(segfilename, dbg_out);
	#endif
}

void findSegment(const Mat& img, const Rect& roi, vector< Point >& outQuad){ findSegmentImpl(img, roi, outQuad); }
void findSegment(const Mat& img, const Rect& roi, vector< Point2f >& outQuad){ findSegmentImpl(img, roi, outQuad); }
