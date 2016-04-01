/** 
 * @file MatchElastic.cpp 
 * This file contains the implementation of elastic matching to find
 * regions of interest based on an initial rigid alignment of the camera-
 * captured form with the template. 
 * 
 * @author Faisal Shafait (faisalshafait@gmail.com)  
 * 
 * @version 0.1  
 * 
 */ 
 

#include "MatchElastic.h"
#include "UnionFind.h"

using namespace std;
using namespace cv;

/** 
 * This function implements local adaptive thresholding algorithms from: 
 * Faisal Shafait, Daniel Keysers, Thomas M. Breuel. "Efficient Implementation  
 * of Local Adaptive Thresholding Techniques Using Integral Images",  
 * SPIE Document Recognition and Retrieval XV, DRR'08, San Jose, CA, USA. Jan. 2008 
 *  
 * @param[in]     gray Gray scale input image as an OpenCV Mat object. 
 * @param[out]    binary Output image binarized using Shafait's method. 
 * @param[in]     w local square window side length to compute adaptive threshold. 
 * @param[in]     k Gray level sensititivity parameter. Lower values of k result in whiter images (fewer black pixels) and vice versa. 
 */ 
void binarizeShafait(Mat &gray, Mat &binary, int w, double k){ 
    Mat sum, sumsq; 
    gray.copyTo(binary); 
    int half_width = w >> 1; 
    integral(gray, sum, sumsq,  CV_64F); 
    for(int i=0; i<gray.rows; i ++){ 
        for(int j=0; j<gray.cols; j++){ 
            int x_0 = (i > half_width) ? i - half_width : 0; 
            int y_0 = (j > half_width) ? j - half_width : 0; 
            int x_1 = (i + half_width >= gray.rows) ? gray.rows - 1 : i + half_width; 
            int y_1 = (j + half_width >= gray.cols) ? gray.cols - 1 : j + half_width; 
            double area = (x_1-x_0) * (y_1-y_0); 
            double mean = (sum.at<double>(x_0,y_0) + sum.at<double>(x_1,y_1) - sum.at<double>(x_0,y_1) - sum.at<double>(x_1,y_0)) / area; 
            double sq_mean = (sumsq.at<double>(x_0,y_0) + sumsq.at<double>(x_1,y_1) - sumsq.at<double>(x_0,y_1) - sumsq.at<double>(x_1,y_0)) / area; 
            double stdev = sqrt(sq_mean - (mean * mean)); 
            double threshold = mean * (1 + k * ((stdev / 128) -1) ); 
            if (gray.at<uchar>(i,j) > threshold) 
                binary.at<uchar>(i,j) = 255; 
            else 
                binary.at<uchar>(i,j) = 0; 
        } 
    } 
} 

/** 
 * This function uses union find structures to build connected components
 * in a horizontal and a vertical sweep through the image
 *  
 * @param[in]     img Input CV_8U Mat data structure representing a binary image
 *                Foreground is assumed 0 and background is 255.
 * @param[in]     max_x Maximum allowed width of a connected component as 
 *                a fraction of image width
 * @param[in]     max_y Maximum allowed height of a connected component as 
 *                a fraction of image height
 * @param[in]     min_area Minimum allowed area (width x height) of a valid 
 *                connected component.
 * @param[in]     type Use 4 or 8 connected neighborhood.
 * @param[out]    rboxes An array of bounding boxes of valid connected components.
 * @return        Pointer to the newly created Pix data structure.
 * 
 */ 
void conCompFast(cv::Mat &img, std::vector<cv::Rect> &rboxes,
                 float max_x, float max_y, float min_area, int type){	
    cv::Mat labelImg = cv::Mat::zeros(img.rows, img.cols, CV_64F);
    int label = 0;
    CUnionFind *uf = new CUnionFind(img.rows*img.cols);
    int l1, l2;
    for (int y=0, Y=img.rows; y<Y; y++){	
        for (int x=0, X=img.cols ; x<X; x++){
            if (img.at<uchar>(y,x)==0){
				// adapt from left neighbor
                if (x>0 && img.at<uchar>(y,x-1)==0){
					labelImg.at<double>(y,x)=label;
                } else {
                    labelImg.at<double>(y,x)=++label;
                } // top neighbor
                if (y>0 && labelImg.at<double>(y-1,x)!=0){
                    l1 = uf->find(label);
                    l2 = uf->find(labelImg.at<double>(y-1,x));
                    if (l1 != l2) uf->set(l1,l2);
                }
                else if (y>0 && x>0 && labelImg.at<double>(y-1,x-1)!=0 && type==8){
                    l1 = uf->find(label);
                    l2 = uf->find(labelImg.at<double>(y-1,x-1));
                    if (l1 != l2) uf->set(l1,l2);
                }
                else if (y>0 && x<X-1 && labelImg.at<double>(y-1,x+1)!=0 && type==8){
                    l1 = uf->find(label);
                    l2 = uf->find(labelImg.at<double>(y-1,x+1));
                    if (l1 != l2) uf->set(l1,l2);
                }
            }
        }
    }
    //fprintf(stderr, "%d labels found!\n", label);
    for (int y=0; y<img.rows; y++) 
        for (int x=0; x<img.cols; x++) 
            labelImg.at<double>(y,x) = uf->find(labelImg.at<double>(y,x));
	
    // Init Bboxes and Seeds
    std::vector<cv::Rect> bb(label+1, cv::Rect());
	std::vector<bool> empty(label+1, true);
    int l;
    cv::Rect b;
    int l_old = 0 ;
    for (int y=0, Y=img.rows; y<Y; y++){
        for (int x=0, X=img.cols; x<X; x++){ 
            if (img.at<uchar>(y,x) == 0){
                l = uf->find(labelImg.at<double>(y,x));
                cv::Rect pt(x,y,1,1);
                if(empty[l]){
                    bb[l] = pt;
                    empty[l] = false;
                } else {
                    bb[l] |= pt;
                }
            }
        }
    }
    // Exclude boxes that are too big or too small
    float max_x_box = img.cols*max_x ;
    float max_y_box = img.rows*max_y ;
    cv::Rect imgDim(0,0,img.cols-1,img.rows-1);
    for (int i=1; i<=label; i++){
        if (uf->isRoot(i)){
            cv::Rect rc = bb[i];
            rc &= imgDim;
            if ( (rc.height > max_y_box) || (rc.width > max_x_box) ||
               (rc.height*rc.width < min_area)) continue;
            rboxes.push_back(rc);
//	        fprintf(stderr,"Root: %d %d %d %d\n", bb[i].x, bb[i].y, bb[i].width, bb[i].height);
        }
    }
    delete uf;
}


/**
 * This function decides whether the input Rect has a square shape
 * with a reasonable margin for distortions
 *
 * @param[in]     r Input rectangle
 * @return    True if the input rectangle appears like a square, False otherwise.
 */
bool isAlmostSquare(Rect &r){
    float factor = 0.25;
    float hmin = r.height * (1.0 - factor);
    float hmax = r.height * (1.0 + factor);

    return (r.width > hmin) && (r.width < hmax);
}

/**
 * This function implements the run length smearing algorithm (RLSA) in the horizontal direction
 *
 * @param[in,out]     imgIn Input binary image that is smeared (output is stored in the same image).
 * @param[in]    thresh smearing threshold.
 */
void smearHorizontal(Mat &imgIn, int thresh){
    for(int r=1; r<imgIn.rows; r++){
        bool bgRun = false;
        int runStart = -1;
        for(int c=1; c<imgIn.cols; c++){
            //fprintf(stderr,"(%d,%d) ", c, r);
            if(!imgIn.at<uchar>(r,c) && imgIn.at<uchar>(r,c-1)){
                bgRun = true;
                runStart = c;
            }
            if(imgIn.at<uchar>(r,c) && !imgIn.at<uchar>(r,c-1)){
                if(bgRun){
                    int runLength = c - runStart;
                    if(runLength < thresh){
                        for(int i=runStart; i<c; i++)
                            imgIn.at<uchar>(r,i) = 1;
                    }
                    bgRun = false;
                    runStart = -1;
                }
            }
        }
    }
}

/**
 * This function implements the run length smearing algorithm (RLSA) in the vertical direction
 *
 * @param[in,out]     imgIn Input binary image that is smeared (output is stored in the same image).
 * @param[in]    thresh smearing threshold.
 */
void smearVertical(Mat &imgIn, int thresh){
    for(int c=1; c<imgIn.cols; c++){
        bool bgRun = false;
        int runStart = -1;
        for(int r=1; r<imgIn.rows; r++){
            //fprintf(stderr,"(%d,%d) ", c, r);
            if(!imgIn.at<uchar>(r,c) && imgIn.at<uchar>(r-1,c)){
                bgRun = true;
                runStart = r;
            }
            if(imgIn.at<uchar>(r,c) && !imgIn.at<uchar>(r-1,c)){
                if(bgRun){
                    int runLength = r - runStart;
                    if(runLength < thresh){
                        for(int i=runStart; i<r; i++)
                            imgIn.at<uchar>(i,c) = 1;
                    }
                    bgRun = false;
                    runStart = -1;
                }
            }
        }
    }
}

bool detectBubbles(vector<Rect>& bboxes, vector<Rect>& bubbles){
    double minBubbleHeight = 7;
    double maxBubbleHeight = 13;
    vector<Rect> candidates;
    for(int i=0; i<bboxes.size(); i++){
        Rect r = bboxes[i];
        if(r.height > maxBubbleHeight) continue;
        if(r.height < minBubbleHeight) continue;
        if( !isAlmostSquare(r) ) continue;
        bubbles.push_back(r);
    }
    return true;
}

bool detectHoles(vector<Rect>& bboxes, vector<Rect>& holes){
    double minHoleHeight = 5;
    double maxHoleHeight = 15;
    for(int i=0; i<bboxes.size(); i++){
        Rect r = bboxes[i];
        if(r.height > maxHoleHeight) continue;
        if(r.height < minHoleHeight) continue;
        if( !isAlmostSquare(r) ) continue;
        holes.push_back(r);
    }
    return true;
}

bool detectNumBoxes(vector<Rect>& bboxes, vector<Rect>& numBoxes){
    double minNboxHeight = 25;
    double maxNboxHeight = 40;
    for(int i=0; i<bboxes.size(); i++){
        Rect r = bboxes[i];
        if(r.height > maxNboxHeight) continue;
        if(r.height < minNboxHeight) continue;
        if( r.height > 2*r.width ) continue;
        if( r.height < 1.2*r.width ) continue;
        numBoxes.push_back(r);
    }
}

bool detectQRCodes(Mat& imgMATbinInv, vector<Rect>& qrBoxes){
    Mat element3(3,3, CV_8U, cv::Scalar(1));
    Mat element(11, 11, CV_8U, cv::Scalar(1));
    Mat temp1;
    erode(imgMATbinInv, temp1, element3);

    Mat imgDilated, imgClosed, imgEroded, imgOpened;
    dilate(temp1, imgDilated, element);
    erode(imgDilated, imgClosed, element);
    erode(imgClosed, imgEroded, element);
    dilate(imgEroded, imgOpened, element);
    if(DEBUG_OUTPUT){
        imwrite("/storage/emulated/0/opendatakit/tables/data/scan_data/debug/qrcode.jpg", imgOpened);
    }
    Mat imgMorphInv = 255 - imgOpened;
    conCompFast(imgMorphInv, qrBoxes);
}

void despeckle(Mat& imgMATInv, vector<Rect>& bboxes){
    for(int i=0; i<bboxes.size(); i++){
        if(bboxes[i].area() < 10){
            rectangle(imgMATInv, bboxes[i], Scalar(0), -1);
        }
    }
}


