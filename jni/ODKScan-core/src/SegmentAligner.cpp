#include "configuration.h"
#include "Addons.h"
#include "AlignmentUtils.h"
#include "SegmentAligner.h"

#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#include <iostream>

#ifdef OUTPUT_DEBUG_IMAGES
#include "NameGenerator.h"
NameGenerator alignmentNamer("debug_segment_images/", false);
#endif


using namespace std;
using namespace cv;

// Try to distil the maximum quad (4 point contour) from a convex contour of many points.
// if none is found maxQuad will not be altered.
// TODO: Find out what happens when you try to simplify a contour that already has just 4 points.
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
//Sum the pixels that lie on a line starting and ending in the specified rows.
int lineSum(const Mat& img, int start, int end, bool transpose) {

	int hSpan;
	if(transpose){
		hSpan = img.rows - 1;
	}
	else{
		hSpan = img.cols - 1;
	}
	
	int sum = 0;
	double slope = (double)(end - start)/hSpan;
	
	for(int i = 0; i<=hSpan; i++) {
		int j = start + slope*i;

		if(j < 0){
			sum+=127;
		}
		else{
			if(transpose){
				sum += img.at<uchar>(i, j);
			}
			else{
				sum += img.at<uchar>(j, i);
			}
		}
	}
	return sum;
}
void findLinesHelper(const Mat& img, int& start, int& end, const Rect& roi, bool flip, bool transpose) {
	int vSpan, hSpan;
	int range, midpoint;
	float maxSlope = .15;

	if(transpose){
		vSpan = img.cols - 1;
		hSpan = img.rows - 1;
		midpoint = roi.x;
		range = roi.y;
	}
	else{		
		vSpan = img.rows - 1;
		hSpan = img.cols - 1;
		midpoint = roi.y;
		range = roi.y;
	}
	
	//The param limits the weigting to a certain magnitude, in this case 10% of the max.
	int param = .15 * 255 * (hSpan + 1);
	float maxSsdFromMidpoint = 2*range*range;
	
	int minLs = INT_MAX;
	for(int i = midpoint - range; i < midpoint + range; i++) {
		for(int j = MAX(i-hSpan*maxSlope, midpoint - range); j < MIN(i+hSpan*maxSlope, midpoint + range); j++) {

			float ssdFromMidpoint = (i - midpoint)*(i - midpoint) + (j - midpoint)*(j - midpoint);
			int ls = param * ssdFromMidpoint / maxSsdFromMidpoint;
			if(flip){
				ls += lineSum(img, vSpan - i, vSpan - j, transpose);
			}
			else{
				ls += lineSum(img, i, j, transpose);
			}
			if( ls < minLs ){
				start = i;
				end = j;
				minLs = ls;
			}
		}
	}
}
/*
float findMultiLineEnergy(const Mat& img, int& start, int& end, vector<LineIterator>& iters) {
	int sum = 0;
	int totalPixels = 0;
	for(size_t itIdx; itIdx < iters.size(); itIdx++){
		totalPixels += iters[itIdx].count;
		for(int i = 0; i < iters[itIdx].count; i++, ++iters[itIdx])
			sum += (int)*iters[i];
	}
	return float(sum)/totalPixels;
}
*/

//#define LI_TYPE 8
//LineIterator(img, quad[0], quad[1], LI_TYPE);

//Find the minimum energy lines crossing the image.
//A and B are the start and end points of the line.
template <class T>
void findLines(const Mat& img, Point_<T>& A, Point_<T>& B, const Rect& roi, bool flip, bool transpose) {
	int start, end;
	
	findLinesHelper(img, start, end, roi, flip, transpose);
	
	if(flip && transpose){
		A = Point_<T>(img.cols - 1 - start, img.rows-1);
		B = Point_<T>(img.cols - 1 - end, 0);
	}
	else if(!flip && transpose){
		A = Point_<T>(start, 0);
		B = Point_<T>(end, img.rows -1);
	}
	else if(flip && !transpose){
		A = Point_<T>(0, img.rows - 1 - start);
		B = Point_<T>(img.cols-1, img.rows - 1 - end);
	}
	else{
		A = Point_<T>(0, start);
		B = Point_<T>(img.cols-1, end);
	}
}
//This version doesn't seem to work for some reason...
template <class T>
void findLinesLIt(const Mat& img, Point_<T>& A, Point_<T>& B, const Rect& roi, bool flip, bool transpose) {
	int vSpan, hSpan;
	int range, midpoint;
	float maxSlope = .15;

	if(transpose){
		vSpan = img.cols - 1;
		hSpan = img.rows - 1;
		midpoint = roi.x;
		range = roi.y;
	}
	else{		
		vSpan = img.rows - 1;
		hSpan = img.cols - 1;
		midpoint = roi.y;
		range = roi.y;
	}
	
	//The param limits the weigting to a certain magnitude, in this case 10% of the max.
	int param = .15 * 255 * (hSpan + 1);
	float maxSsdFromMidpoint = 2*range*range;
	
	int minLs = INT_MAX;
	for(int i = midpoint - range; i < midpoint + range; i++) {
		for(int j = MAX(i-hSpan*maxSlope, midpoint - range); j < MIN(i+hSpan*maxSlope, midpoint + range); j++) {

			float ssdFromMidpoint = (i - midpoint)*(i - midpoint) + (j - midpoint)*(j - midpoint);
			int ls = param * ssdFromMidpoint / maxSsdFromMidpoint;
			
			Point curA;
			Point curB;
			
			if(flip){
				curA = Point(0, vSpan - i);
				curB = Point(hSpan, vSpan - j);
			}
			else{
				curA = Point(0, i);
				curB = Point(hSpan, j);
			}
			if(transpose){
				curA = Point(curA.y, curA.x);
				curB = Point(curB.y, curB.x);
			}
			
			#define LI_TYPE 8
			LineIterator lit(img, curA, curB, LI_TYPE);
			if(lit.count == 0) continue;
			for(int litIdx = 0; litIdx < lit.count; litIdx++, ++lit)
				ls += (int)*lit;
			ls /= lit.count;
			
			if( ls < minLs ){
				A = curA;
				B = curB;
				minLs = ls;
			}
		}
	}
}
template <class T>
inline Point_<T> findIntersection(const Point_<T>& P1, const Point_<T>& P2,
						const Point_<T>& P3, const Point_<T>& P4){
	// From determinant formula here:
	// http://en.wikipedia.org/wiki/Line_intersection
	double denom = (P1.x - P2.x) * (P3.y - P4.y) - (P1.y - P2.y) * (P3.x - P4.x);
	return Point_<T>(
		( (P1.x * P2.y - P1.y * P2.x) * (P3.x - P4.x) -
		  (P1.x - P2.x) * (P3.x * P4.y - P3.y * P4.x) ) / denom,
		( (P1.x * P2.y - P1.y * P2.x) * (P3.y - P4.y) -
		  (P1.y - P2.y) * (P3.x * P4.y - P3.y * P4.x) ) / denom);
}
template <class T>
void refineCorners(const Mat& img, vector< Point_<T> >& quad){
	return;
}
//TODO: could possibly improve this using harris detector w/ hillclimbing instead.
void refineCorners(const Mat& img, vector< Point2f >& quad){
	TermCriteria termcrit(CV_TERMCRIT_ITER | CV_TERMCRIT_EPS, 20, 0.03);
	cornerSubPix(img, quad, Size(1,1), Size(-1, -1), termcrit);
}
template <class T, class U>
void convertQuad(const vector< Point_<T> >& quad, vector< Point_<U> >& outQuad){
	for(size_t i = 0; i<4; i++){
		outQuad.push_back( Point_<U>(quad[i].x, quad[i].y) );
	}
}
template <class T>
Point_<T> getCorner(const Mat& img, const Rect&roi, const Mat& templ, const Point& offset){
	#if 0
	vector<Point2f> cornerVec;
	/goodFeaturesToTrack(img(roi), cornerVec, 1, .01, 0, Mat(), 3);
	return Point_<T>(cornerVec[0].x + roi.x, cornerVec[0].y + roi.y);
	#endif
	Mat result(img.size(), CV_8U);
	int method = 3962;//CV_TM_SQDIFF;
	if(method == 3962){
		createLinearFilter(templ.type(), templ.type(), 255 - templ)->apply(img, result);
	}
	else{
		matchTemplate(img, templ, result, method);
	}
	
	Point minLoc, maxLoc;
	minMaxLoc(result, 0, 0, &minLoc, &maxLoc);
	Point corner;
	if(method == CV_TM_SQDIFF || method == CV_TM_SQDIFF_NORMED || method == 3962){
		corner = minLoc;
	}
	else{
		corner = maxLoc;
	}
	
	return Point_<T>(corner.x + offset.x, corner.y + offset.y);
}
/*
Pseudo-code description of findSegment:
1.	Find lines along the edges of the bounding box
	a.	Using difference of means and thresholding about 0 generate a binary images that should have highly pronounced black edges.
		There are many alternatives that might also work here, for example an inverted canny filter.
	b.	For every pair of rows in the top half of the image sum the white pixels in a line that starts at the left side of the image in the first row,
		and ends at the right side of the image in the second. The row pair with the minimum number of white pixels is the minimum energy line.
	c.	Repeat step 2 while flipping and/or transposing the image to find 4 such lines corresponding to the edges of the segment.
2.	Find their intersections
3.	Find the trasformation from the 4 intersection points to the rectangle defined in the template,
	then use it to transform the segment into the rectangle.
Note, this leaves out some tweaks and implementation details.
*/

//TODO: reimplement scaling to 128 px
template <class T>
void findSegmentImpl(const Mat& img, const Rect& roi, vector< Point_<T> >& outQuad){

	//quad finding modes:
	//There is a tradeoff with using intersections.
	//It seems to work better on messy segments, however,
	//on segments with multiple min energy lines we are more likely
	//to choose the wrong line than with the contour method.
	#define QUAD_FIND_INTERSECTION 0
	#define QUAD_FIND_CONTOURS 1
	#define QUAD_FIND_CORNERS 2 //Broken
	
	#define QUAD_FIND_MODE QUAD_FIND_INTERSECTION
	
	Mat imgThresh, temp_img, temp_img2;
	
	int blurSize = 40;
	
	#if 1
		blur(img, temp_img, Size(blurSize, blurSize));
	#else
		//Not sure if this had advantages or not...
		//My theory is that it is slower but more accurate,
		//but I don't know if either difference is significant enough to notice.
		//Will need to test.
		GaussianBlur(img, temp_img, Size(9, 9), 3, 3);
	#endif

	imgThresh = (img - temp_img) > 0;

	Rect contractedRoi = resizeRect(roi, .7);
	
	imgThresh(contractedRoi) = Scalar::all(255);
	
	Point_<T> A1, B1, A2, B2, A3, B3, A4, B4;
	findLines(imgThresh, A1, B1, roi, false, true);
	findLines(imgThresh, A2, B2, roi, false, false);
	findLines(imgThresh, A3, B3, roi, true, true);
	findLines(imgThresh, A4, B4, roi, true, false);

	#if QUAD_FIND_MODE == QUAD_FIND_INTERSECTION
		vector< Point_<T> > quad;
		quad.push_back(findIntersection(A1, B1, A2, B2));
		quad.push_back(findIntersection(A2, B2, A3, B3));
		quad.push_back(findIntersection(A3, B3, A4, B4));
		quad.push_back(findIntersection(A4, B4, A1, B1));
		outQuad = quad;
		//TODO: add some code that does this:
		//      lines can be used to mask off sections of the image
		//      if there is a height/width discrepancy move the weighting
		//      line to the averate of the expected lines.
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
	#elif QUAD_FIND_MODE == QUAD_FIND_CORNERS
		vector< Point_<T> > quad;

		Size quadrantSize = .5 * img.size();
		Rect quadrant;
		
		Mat templ(Size(13,13), CV_8U, Scalar::all(255));
		Size templSize = templ.size();
		line( templ, Point(0, 0), Point(templSize.width, 0), Scalar::all(0), 2, 4);
		line( templ, Point(0, 0), Point(0, templSize.height), Scalar::all(0), 2, 4);
		quadrant = Rect(Point(0,0), quadrantSize);
		quad.push_back(getCorner<T>(img(quadrant), roi, templ, quadrant.tl()));
		Mat temp;
		flip(templ, temp,0);
		transpose(temp, templ);
		quadrant = Rect(Point(img.cols/2,0), quadrantSize);
		quad.push_back(getCorner<T>(img(quadrant), roi, templ,
		                            quadrant.tl() + Point(templSize.width,0)));
		flip(templ, temp,0);
		transpose(temp, templ);
		quadrant = Rect(Point(img.cols/2,img.rows/2), quadrantSize);
		quad.push_back(getCorner<T>(img(quadrant), roi, templ,
		                            quadrant.tl() + Point(templSize.width,templSize.height)));
		flip(templ, temp,0);
		transpose(temp, templ);
		quadrant = Rect(Point(0,img.rows/2), quadrantSize);
		quad.push_back(getCorner<T>(img(quadrant), roi, templ, quadrant.tl() + Point(0,templSize.height)));
		outQuad = quad;
	#endif
	
	//refineCorners(img, quad);
	#ifdef OUTPUT_DEBUG_IMAGES
		Mat dbg_out, dbg_out2;
		imgThresh.copyTo(dbg_out);
		string segfilename = alignmentNamer.get_unique_name("alignment_debug_");
		segfilename.append(".jpg");
		imwrite(segfilename, dbg_out);
	#endif
}
void findSegment(const Mat& img, const Rect& roi, vector< Point >& outQuad){ findSegmentImpl(img, roi, outQuad); }
void findSegment(const Mat& img, const Rect& roi, vector< Point2f >& outQuad){ findSegmentImpl(img, roi, outQuad); }
