
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;
import org.opencv.core.*;
import org.opencv.utils;
// C++: class StarDetector
public class StarDetector {


    protected final long nativeObj;
    protected StarDetector(long addr) { nativeObj = addr; }

    //
    // C++:   StarDetector::StarDetector()
    //

    /**
     * The Star Detector constructor
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#stardetector-stardetector">org.opencv.features2d.StarDetector.StarDetector</a>
     */
    public   StarDetector()
    {

        nativeObj = n_StarDetector();

        return;
    }


    //
    // C++:   StarDetector::StarDetector(int _maxSize, int _responseThreshold, int _lineThresholdProjected, int _lineThresholdBinarized, int _suppressNonmaxSize)
    //

    /**
     * The Star Detector constructor
     *
     * @param _maxSize a _maxSize
     * @param _responseThreshold a _responseThreshold
     * @param _lineThresholdProjected a _lineThresholdProjected
     * @param _lineThresholdBinarized a _lineThresholdBinarized
     * @param _suppressNonmaxSize a _suppressNonmaxSize
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#stardetector-stardetector">org.opencv.features2d.StarDetector.StarDetector</a>
     */
    public   StarDetector(int _maxSize, int _responseThreshold, int _lineThresholdProjected, int _lineThresholdBinarized, int _suppressNonmaxSize)
    {

        nativeObj = n_StarDetector(_maxSize, _responseThreshold, _lineThresholdProjected, _lineThresholdBinarized, _suppressNonmaxSize);

        return;
    }


    //
    // C++:  void StarDetector::operator()(Mat image, vector_KeyPoint& keypoints)
    //

    /**
     * Finds keypoints in an image
     *
     * @param image The input 8-bit grayscale image
     * @param keypoints The output vector of keypoints
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#stardetector-operator">org.opencv.features2d.StarDetector.operator()</a>
     */
    public  void detect(Mat image, java.util.List<KeyPoint> keypoints)
    {
        Mat keypoints_mat = new Mat();
        n_detect(nativeObj, image.nativeObj, keypoints_mat.nativeObj);
        utils.Mat_to_vector_KeyPoint(keypoints_mat, keypoints);
        return;
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

    // C++:   StarDetector::StarDetector()
    private static native long n_StarDetector();

    // C++:   StarDetector::StarDetector(int _maxSize, int _responseThreshold, int _lineThresholdProjected, int _lineThresholdBinarized, int _suppressNonmaxSize)
    private static native long n_StarDetector(int _maxSize, int _responseThreshold, int _lineThresholdProjected, int _lineThresholdBinarized, int _suppressNonmaxSize);

    // C++:  void StarDetector::operator()(Mat image, vector_KeyPoint& keypoints)
    private static native void n_detect(long nativeObj, long image_nativeObj, long keypoints_mat_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
