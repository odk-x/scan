
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.objdetect;
import org.opencv.core.*;
import org.opencv.utils;
// C++: class HOGDescriptor
public class HOGDescriptor {


    protected final long nativeObj;
    protected HOGDescriptor(long addr) { nativeObj = addr; }

    public static final int
            L2Hys = 0,
            DEFAULT_NLEVELS = 64;


    //
    // C++:   HOGDescriptor::HOGDescriptor()
    //

    public   HOGDescriptor()
    {

        nativeObj = n_HOGDescriptor();

        return;
    }


    //
    // C++:   HOGDescriptor::HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture = 1, double _winSigma = -1, int _histogramNormType = HOGDescriptor::L2Hys, double _L2HysThreshold = 0.2, bool _gammaCorrection = false, int _nlevels = HOGDescriptor::DEFAULT_NLEVELS)
    //

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold, boolean _gammaCorrection, int _nlevels)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture, _winSigma, _histogramNormType, _L2HysThreshold, _gammaCorrection, _nlevels);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold, boolean _gammaCorrection)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture, _winSigma, _histogramNormType, _L2HysThreshold, _gammaCorrection);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture, _winSigma, _histogramNormType, _L2HysThreshold);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture, _winSigma, _histogramNormType);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture, double _winSigma)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture, _winSigma);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins, _derivAperture);

        return;
    }

    public   HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins)
    {

        nativeObj = n_HOGDescriptor(_winSize.width, _winSize.height, _blockSize.width, _blockSize.height, _blockStride.width, _blockStride.height, _cellSize.width, _cellSize.height, _nbins);

        return;
    }


    //
    // C++:   HOGDescriptor::HOGDescriptor(String filename)
    //

    public   HOGDescriptor(java.lang.String filename)
    {

        nativeObj = n_HOGDescriptor(filename);

        return;
    }


    //
    // C++:  bool HOGDescriptor::checkDetectorSize()
    //

    public  boolean checkDetectorSize()
    {

        boolean retVal = n_checkDetectorSize(nativeObj);

        return retVal;
    }


    //
    // C++:  void HOGDescriptor::compute(Mat img, vector_float& descriptors, Size winStride = Size(), Size padding = Size(), vector_Point locations = vector<Point>())
    //

    public  void compute(Mat img, java.util.List<Float> descriptors, Size winStride, Size padding, java.util.List<Point> locations)
    {
        Mat descriptors_mat = new Mat();  Mat locations_mat = utils.vector_Point_to_Mat(locations);
        n_compute(nativeObj, img.nativeObj, descriptors_mat.nativeObj, winStride.width, winStride.height, padding.width, padding.height, locations_mat.nativeObj);
        utils.Mat_to_vector_float(descriptors_mat, descriptors);
        return;
    }

    public  void compute(Mat img, java.util.List<Float> descriptors, Size winStride, Size padding)
    {
        Mat descriptors_mat = new Mat();
        n_compute(nativeObj, img.nativeObj, descriptors_mat.nativeObj, winStride.width, winStride.height, padding.width, padding.height);
        utils.Mat_to_vector_float(descriptors_mat, descriptors);
        return;
    }

    public  void compute(Mat img, java.util.List<Float> descriptors, Size winStride)
    {
        Mat descriptors_mat = new Mat();
        n_compute(nativeObj, img.nativeObj, descriptors_mat.nativeObj, winStride.width, winStride.height);
        utils.Mat_to_vector_float(descriptors_mat, descriptors);
        return;
    }

    public  void compute(Mat img, java.util.List<Float> descriptors)
    {
        Mat descriptors_mat = new Mat();
        n_compute(nativeObj, img.nativeObj, descriptors_mat.nativeObj);
        utils.Mat_to_vector_float(descriptors_mat, descriptors);
        return;
    }


    //
    // C++:  void HOGDescriptor::computeGradient(Mat img, Mat& grad, Mat& angleOfs, Size paddingTL = Size(), Size paddingBR = Size())
    //

    public  void computeGradient(Mat img, Mat grad, Mat angleOfs, Size paddingTL, Size paddingBR)
    {

        n_computeGradient(nativeObj, img.nativeObj, grad.nativeObj, angleOfs.nativeObj, paddingTL.width, paddingTL.height, paddingBR.width, paddingBR.height);

        return;
    }

    public  void computeGradient(Mat img, Mat grad, Mat angleOfs, Size paddingTL)
    {

        n_computeGradient(nativeObj, img.nativeObj, grad.nativeObj, angleOfs.nativeObj, paddingTL.width, paddingTL.height);

        return;
    }

    public  void computeGradient(Mat img, Mat grad, Mat angleOfs)
    {

        n_computeGradient(nativeObj, img.nativeObj, grad.nativeObj, angleOfs.nativeObj);

        return;
    }


    //
    // C++:  void HOGDescriptor::detect(Mat img, vector_Point& foundLocations, vector_double weights, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), vector_Point searchLocations = vector<Point>())
    //

    public  void detect(Mat img, java.util.List<Point> foundLocations, java.util.List<Double> weights, double hitThreshold, Size winStride, Size padding, java.util.List<Point> searchLocations)
    {
        Mat foundLocations_mat = new Mat();  Mat weights_mat = utils.vector_double_to_Mat(weights);  Mat searchLocations_mat = utils.vector_Point_to_Mat(searchLocations);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, weights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, searchLocations_mat.nativeObj);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, java.util.List<Double> weights, double hitThreshold, Size winStride, Size padding)
    {
        Mat foundLocations_mat = new Mat();  Mat weights_mat = utils.vector_double_to_Mat(weights);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, weights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, java.util.List<Double> weights, double hitThreshold, Size winStride)
    {
        Mat foundLocations_mat = new Mat();  Mat weights_mat = utils.vector_double_to_Mat(weights);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, weights_mat.nativeObj, hitThreshold, winStride.width, winStride.height);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, java.util.List<Double> weights, double hitThreshold)
    {
        Mat foundLocations_mat = new Mat();  Mat weights_mat = utils.vector_double_to_Mat(weights);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, weights_mat.nativeObj, hitThreshold);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, java.util.List<Double> weights)
    {
        Mat foundLocations_mat = new Mat();  Mat weights_mat = utils.vector_double_to_Mat(weights);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, weights_mat.nativeObj);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }


    //
    // C++:  void HOGDescriptor::detect(Mat img, vector_Point& foundLocations, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), vector_Point searchLocations = vector<Point>())
    //

    public  void detect(Mat img, java.util.List<Point> foundLocations, double hitThreshold, Size winStride, Size padding, java.util.List<Point> searchLocations)
    {
        Mat foundLocations_mat = new Mat();  Mat searchLocations_mat = utils.vector_Point_to_Mat(searchLocations);
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, searchLocations_mat.nativeObj);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, double hitThreshold, Size winStride, Size padding)
    {
        Mat foundLocations_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, double hitThreshold, Size winStride)
    {
        Mat foundLocations_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations, double hitThreshold)
    {
        Mat foundLocations_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }

    public  void detect(Mat img, java.util.List<Point> foundLocations)
    {
        Mat foundLocations_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, foundLocations_mat.nativeObj);
        utils.Mat_to_vector_Point(foundLocations_mat, foundLocations);
        return;
    }


    //
    // C++:  void HOGDescriptor::detectMultiScale(Mat img, vector_Rect& foundLocations, vector_double foundWeights, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), double scale = 1.05, double finalThreshold = 2.0, bool useMeanshiftGrouping = false)
    //

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold, Size winStride, Size padding, double scale, double finalThreshold, boolean useMeanshiftGrouping)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale, finalThreshold, useMeanshiftGrouping);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold, Size winStride, Size padding, double scale, double finalThreshold)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale, finalThreshold);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold, Size winStride, Size padding, double scale)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold, Size winStride, Size padding)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold, Size winStride)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold, winStride.width, winStride.height);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights, double hitThreshold)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj, hitThreshold);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, java.util.List<Double> foundWeights)
    {
        Mat foundLocations_mat = new Mat();  Mat foundWeights_mat = utils.vector_double_to_Mat(foundWeights);
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, foundWeights_mat.nativeObj);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }


    //
    // C++:  void HOGDescriptor::detectMultiScale(Mat img, vector_Rect& foundLocations, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), double scale = 1.05, double finalThreshold = 2.0, bool useMeanshiftGrouping = false)
    //

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold, Size winStride, Size padding, double scale, double finalThreshold, boolean useMeanshiftGrouping)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale, finalThreshold, useMeanshiftGrouping);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold, Size winStride, Size padding, double scale, double finalThreshold)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale, finalThreshold);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold, Size winStride, Size padding, double scale)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height, scale);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold, Size winStride, Size padding)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height, padding.width, padding.height);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold, Size winStride)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold, winStride.width, winStride.height);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations, double hitThreshold)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj, hitThreshold);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }

    public  void detectMultiScale(Mat img, java.util.List<Rect> foundLocations)
    {
        Mat foundLocations_mat = new Mat();
        n_detectMultiScale(nativeObj, img.nativeObj, foundLocations_mat.nativeObj);
        utils.Mat_to_vector_Rect(foundLocations_mat, foundLocations);
        return;
    }


    //
    // C++:  size_t HOGDescriptor::getDescriptorSize()
    //

    public  long getDescriptorSize()
    {

        long retVal = n_getDescriptorSize(nativeObj);

        return retVal;
    }


    //
    // C++:  double HOGDescriptor::getWinSigma()
    //

    public  double getWinSigma()
    {

        double retVal = n_getWinSigma(nativeObj);

        return retVal;
    }


    //
    // C++:  bool HOGDescriptor::load(String filename, String objname = String())
    //

    public  boolean load(java.lang.String filename, java.lang.String objname)
    {

        boolean retVal = n_load(nativeObj, filename, objname);

        return retVal;
    }

    public  boolean load(java.lang.String filename)
    {

        boolean retVal = n_load(nativeObj, filename);

        return retVal;
    }


    //
    // C++:  void HOGDescriptor::save(String filename, String objname = String())
    //

    public  void save(java.lang.String filename, java.lang.String objname)
    {

        n_save(nativeObj, filename, objname);

        return;
    }

    public  void save(java.lang.String filename)
    {

        n_save(nativeObj, filename);

        return;
    }


    //
    // C++:  void HOGDescriptor::setSVMDetector(vector_float _svmdetector)
    //

    public  void setSVMDetector(java.util.List<Float> _svmdetector)
    {
        Mat _svmdetector_mat = utils.vector_float_to_Mat(_svmdetector);
        n_setSVMDetector(nativeObj, _svmdetector_mat.nativeObj);

        return;
    }


    //
    // C++: // Size winSize
    //

    public  Size get_winSize()
    {

        Size retVal = new Size(n_get_winSize(nativeObj));

        return retVal;
    }


    //
    // C++: // Size blockSize
    //

    public  Size get_blockSize()
    {

        Size retVal = new Size(n_get_blockSize(nativeObj));

        return retVal;
    }


    //
    // C++: // Size blockStride
    //

    public  Size get_blockStride()
    {

        Size retVal = new Size(n_get_blockStride(nativeObj));

        return retVal;
    }


    //
    // C++: // Size cellSize
    //

    public  Size get_cellSize()
    {

        Size retVal = new Size(n_get_cellSize(nativeObj));

        return retVal;
    }


    //
    // C++: // int nbins
    //

    public  int get_nbins()
    {

        int retVal = n_get_nbins(nativeObj);

        return retVal;
    }


    //
    // C++: // int derivAperture
    //

    public  int get_derivAperture()
    {

        int retVal = n_get_derivAperture(nativeObj);

        return retVal;
    }


    //
    // C++: // double winSigma
    //

    public  double get_winSigma()
    {

        double retVal = n_get_winSigma(nativeObj);

        return retVal;
    }


    //
    // C++: // int histogramNormType
    //

    public  int get_histogramNormType()
    {

        int retVal = n_get_histogramNormType(nativeObj);

        return retVal;
    }


    //
    // C++: // double L2HysThreshold
    //

    public  double get_L2HysThreshold()
    {

        double retVal = n_get_L2HysThreshold(nativeObj);

        return retVal;
    }


    //
    // C++: // bool gammaCorrection
    //

    public  boolean get_gammaCorrection()
    {

        boolean retVal = n_get_gammaCorrection(nativeObj);

        return retVal;
    }


    //
    // C++: // int nlevels
    //

    public  int get_nlevels()
    {

        int retVal = n_get_nlevels(nativeObj);

        return retVal;
    }


    @Override
    protected void finalize() throws Throwable {
        n_delete(nativeObj);
        super.finalize();
    }



    //
    // native stuff
    //
    static { System.loadLibrary("opencv_java"); }

    // C++:   HOGDescriptor::HOGDescriptor()
    private static native long n_HOGDescriptor();

    // C++:   HOGDescriptor::HOGDescriptor(Size _winSize, Size _blockSize, Size _blockStride, Size _cellSize, int _nbins, int _derivAperture = 1, double _winSigma = -1, int _histogramNormType = HOGDescriptor::L2Hys, double _L2HysThreshold = 0.2, bool _gammaCorrection = false, int _nlevels = HOGDescriptor::DEFAULT_NLEVELS)
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold, boolean _gammaCorrection, int _nlevels);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold, boolean _gammaCorrection);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType, double _L2HysThreshold);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture, double _winSigma, int _histogramNormType);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture, double _winSigma);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins, int _derivAperture);
    private static native long n_HOGDescriptor(double _winSize_width, double _winSize_height, double _blockSize_width, double _blockSize_height, double _blockStride_width, double _blockStride_height, double _cellSize_width, double _cellSize_height, int _nbins);

    // C++:   HOGDescriptor::HOGDescriptor(String filename)
    private static native long n_HOGDescriptor(java.lang.String filename);

    // C++:  bool HOGDescriptor::checkDetectorSize()
    private static native boolean n_checkDetectorSize(long nativeObj);

    // C++:  void HOGDescriptor::compute(Mat img, vector_float& descriptors, Size winStride = Size(), Size padding = Size(), vector_Point locations = vector<Point>())
    private static native void n_compute(long nativeObj, long img_nativeObj, long descriptors_mat_nativeObj, double winStride_width, double winStride_height, double padding_width, double padding_height, long locations_mat_nativeObj);
    private static native void n_compute(long nativeObj, long img_nativeObj, long descriptors_mat_nativeObj, double winStride_width, double winStride_height, double padding_width, double padding_height);
    private static native void n_compute(long nativeObj, long img_nativeObj, long descriptors_mat_nativeObj, double winStride_width, double winStride_height);
    private static native void n_compute(long nativeObj, long img_nativeObj, long descriptors_mat_nativeObj);

    // C++:  void HOGDescriptor::computeGradient(Mat img, Mat& grad, Mat& angleOfs, Size paddingTL = Size(), Size paddingBR = Size())
    private static native void n_computeGradient(long nativeObj, long img_nativeObj, long grad_nativeObj, long angleOfs_nativeObj, double paddingTL_width, double paddingTL_height, double paddingBR_width, double paddingBR_height);
    private static native void n_computeGradient(long nativeObj, long img_nativeObj, long grad_nativeObj, long angleOfs_nativeObj, double paddingTL_width, double paddingTL_height);
    private static native void n_computeGradient(long nativeObj, long img_nativeObj, long grad_nativeObj, long angleOfs_nativeObj);

    // C++:  void HOGDescriptor::detect(Mat img, vector_Point& foundLocations, vector_double weights, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), vector_Point searchLocations = vector<Point>())
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long weights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, long searchLocations_mat_nativeObj);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long weights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long weights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long weights_mat_nativeObj, double hitThreshold);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long weights_mat_nativeObj);

    // C++:  void HOGDescriptor::detect(Mat img, vector_Point& foundLocations, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), vector_Point searchLocations = vector<Point>())
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, long searchLocations_mat_nativeObj);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold);
    private static native void n_detect(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj);

    // C++:  void HOGDescriptor::detectMultiScale(Mat img, vector_Rect& foundLocations, vector_double foundWeights, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), double scale = 1.05, double finalThreshold = 2.0, bool useMeanshiftGrouping = false)
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale, double finalThreshold, boolean useMeanshiftGrouping);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale, double finalThreshold);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj, double hitThreshold);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, long foundWeights_mat_nativeObj);

    // C++:  void HOGDescriptor::detectMultiScale(Mat img, vector_Rect& foundLocations, double hitThreshold = 0, Size winStride = Size(), Size padding = Size(), double scale = 1.05, double finalThreshold = 2.0, bool useMeanshiftGrouping = false)
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale, double finalThreshold, boolean useMeanshiftGrouping);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale, double finalThreshold);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height, double scale);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height, double padding_width, double padding_height);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold, double winStride_width, double winStride_height);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj, double hitThreshold);
    private static native void n_detectMultiScale(long nativeObj, long img_nativeObj, long foundLocations_mat_nativeObj);

    // C++:  size_t HOGDescriptor::getDescriptorSize()
    private static native long n_getDescriptorSize(long nativeObj);

    // C++:  double HOGDescriptor::getWinSigma()
    private static native double n_getWinSigma(long nativeObj);

    // C++:  bool HOGDescriptor::load(String filename, String objname = String())
    private static native boolean n_load(long nativeObj, java.lang.String filename, java.lang.String objname);
    private static native boolean n_load(long nativeObj, java.lang.String filename);

    // C++:  void HOGDescriptor::save(String filename, String objname = String())
    private static native void n_save(long nativeObj, java.lang.String filename, java.lang.String objname);
    private static native void n_save(long nativeObj, java.lang.String filename);

    // C++:  void HOGDescriptor::setSVMDetector(vector_float _svmdetector)
    private static native void n_setSVMDetector(long nativeObj, long _svmdetector_mat_nativeObj);

    // C++: // Size winSize
    private static native double[] n_get_winSize(long nativeObj);

    // C++: // Size blockSize
    private static native double[] n_get_blockSize(long nativeObj);

    // C++: // Size blockStride
    private static native double[] n_get_blockStride(long nativeObj);

    // C++: // Size cellSize
    private static native double[] n_get_cellSize(long nativeObj);

    // C++: // int nbins
    private static native int n_get_nbins(long nativeObj);

    // C++: // int derivAperture
    private static native int n_get_derivAperture(long nativeObj);

    // C++: // double winSigma
    private static native double n_get_winSigma(long nativeObj);

    // C++: // int histogramNormType
    private static native int n_get_histogramNormType(long nativeObj);

    // C++: // double L2HysThreshold
    private static native double n_get_L2HysThreshold(long nativeObj);

    // C++: // bool gammaCorrection
    private static native boolean n_get_gammaCorrection(long nativeObj);

    // C++: // int nlevels
    private static native int n_get_nlevels(long nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
