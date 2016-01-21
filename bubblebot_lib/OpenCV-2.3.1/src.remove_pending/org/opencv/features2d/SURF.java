
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;
import org.opencv.core.*;
import org.opencv.utils;
// C++: class SURF
public class SURF {


    protected final long nativeObj;
    protected SURF(long addr) { nativeObj = addr; }

    //
    // C++:   SURF::SURF()
    //

    /**
     * The SURF extractor constructors.
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF()
    {

        nativeObj = n_SURF();

        return;
    }


    //
    // C++:   SURF::SURF(double _hessianThreshold, int _nOctaves = 4, int _nOctaveLayers = 2, bool _extended = false, bool _upright = false)
    //

    /**
     * The SURF extractor constructors.
     *
     * @param _hessianThreshold a _hessianThreshold
     * @param _nOctaves a _nOctaves
     * @param _nOctaveLayers a _nOctaveLayers
     * @param _extended a _extended
     * @param _upright a _upright
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers, boolean _extended, boolean _upright)
    {

        nativeObj = n_SURF(_hessianThreshold, _nOctaves, _nOctaveLayers, _extended, _upright);

        return;
    }

    /**
     * The SURF extractor constructors.
     *
     * @param _hessianThreshold a _hessianThreshold
     * @param _nOctaves a _nOctaves
     * @param _nOctaveLayers a _nOctaveLayers
     * @param _extended a _extended
     * @param _upright a _upright
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers, boolean _extended)
    {

        nativeObj = n_SURF(_hessianThreshold, _nOctaves, _nOctaveLayers, _extended);

        return;
    }

    /**
     * The SURF extractor constructors.
     *
     * @param _hessianThreshold a _hessianThreshold
     * @param _nOctaves a _nOctaves
     * @param _nOctaveLayers a _nOctaveLayers
     * @param _extended a _extended
     * @param _upright a _upright
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers)
    {

        nativeObj = n_SURF(_hessianThreshold, _nOctaves, _nOctaveLayers);

        return;
    }

    /**
     * The SURF extractor constructors.
     *
     * @param _hessianThreshold a _hessianThreshold
     * @param _nOctaves a _nOctaves
     * @param _nOctaveLayers a _nOctaveLayers
     * @param _extended a _extended
     * @param _upright a _upright
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF(double _hessianThreshold, int _nOctaves)
    {

        nativeObj = n_SURF(_hessianThreshold, _nOctaves);

        return;
    }

    /**
     * The SURF extractor constructors.
     *
     * @param _hessianThreshold a _hessianThreshold
     * @param _nOctaves a _nOctaves
     * @param _nOctaveLayers a _nOctaveLayers
     * @param _extended a _extended
     * @param _upright a _upright
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-surf">org.opencv.features2d.SURF.SURF</a>
     */
    public   SURF(double _hessianThreshold)
    {

        nativeObj = n_SURF(_hessianThreshold);

        return;
    }


    //
    // C++:  int SURF::descriptorSize()
    //

    public  int descriptorSize()
    {

        int retVal = n_descriptorSize(nativeObj);

        return retVal;
    }


    //
    // C++:  void SURF::operator()(Mat img, Mat mask, vector_KeyPoint& keypoints)
    //

    /**
     * Detects keypoints and computes SURF descriptors for them.
     *
     * @param img a img
     * @param mask Optional input mask that marks the regions where we should detect
     * features.
     * @param keypoints The input/output vector of keypoints
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-operator">org.opencv.features2d.SURF.operator()</a>
     */
    public  void detect(Mat img, Mat mask, java.util.List<KeyPoint> keypoints)
    {
        Mat keypoints_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, mask.nativeObj, keypoints_mat.nativeObj);
        utils.Mat_to_vector_KeyPoint(keypoints_mat, keypoints);
        return;
    }


    //
    // C++:  void SURF::operator()(Mat img, Mat mask, vector_KeyPoint& keypoints, vector_float& descriptors, bool useProvidedKeypoints = false)
    //

    /**
     * Detects keypoints and computes SURF descriptors for them.
     *
     * @param img a img
     * @param mask Optional input mask that marks the regions where we should detect
     * features.
     * @param keypoints The input/output vector of keypoints
     * @param descriptors The output concatenated vectors of descriptors. Each
     * descriptor is 64- or 128-element vector, as returned by "SURF.descriptorSize()".
     * So the total size of "descriptors" will be "keypoints.size()*descriptorSize()".
     * @param useProvidedKeypoints Boolean flag. If it is true, the keypoint
     * detector is not run. Instead, the provided vector of keypoints is used and
     * the algorithm just computes their descriptors.
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-operator">org.opencv.features2d.SURF.operator()</a>
     */
    public  void detect(Mat img, Mat mask, java.util.List<KeyPoint> keypoints, java.util.List<Float> descriptors, boolean useProvidedKeypoints)
    {
        Mat keypoints_mat = new Mat();  Mat descriptors_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, mask.nativeObj, keypoints_mat.nativeObj, descriptors_mat.nativeObj, useProvidedKeypoints);
        utils.Mat_to_vector_KeyPoint(keypoints_mat, keypoints);  utils.Mat_to_vector_float(descriptors_mat, descriptors);
        return;
    }

    /**
     * Detects keypoints and computes SURF descriptors for them.
     *
     * @param img a img
     * @param mask Optional input mask that marks the regions where we should detect
     * features.
     * @param keypoints The input/output vector of keypoints
     * @param descriptors The output concatenated vectors of descriptors. Each
     * descriptor is 64- or 128-element vector, as returned by "SURF.descriptorSize()".
     * So the total size of "descriptors" will be "keypoints.size()*descriptorSize()".
     * @param useProvidedKeypoints Boolean flag. If it is true, the keypoint
     * detector is not run. Instead, the provided vector of keypoints is used and
     * the algorithm just computes their descriptors.
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/feature_detection_and_description.html#surf-operator">org.opencv.features2d.SURF.operator()</a>
     */
    public  void detect(Mat img, Mat mask, java.util.List<KeyPoint> keypoints, java.util.List<Float> descriptors)
    {
        Mat keypoints_mat = new Mat();  Mat descriptors_mat = new Mat();
        n_detect(nativeObj, img.nativeObj, mask.nativeObj, keypoints_mat.nativeObj, descriptors_mat.nativeObj);
        utils.Mat_to_vector_KeyPoint(keypoints_mat, keypoints);  utils.Mat_to_vector_float(descriptors_mat, descriptors);
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

    // C++:   SURF::SURF()
    private static native long n_SURF();

    // C++:   SURF::SURF(double _hessianThreshold, int _nOctaves = 4, int _nOctaveLayers = 2, bool _extended = false, bool _upright = false)
    private static native long n_SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers, boolean _extended, boolean _upright);
    private static native long n_SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers, boolean _extended);
    private static native long n_SURF(double _hessianThreshold, int _nOctaves, int _nOctaveLayers);
    private static native long n_SURF(double _hessianThreshold, int _nOctaves);
    private static native long n_SURF(double _hessianThreshold);

    // C++:  int SURF::descriptorSize()
    private static native int n_descriptorSize(long nativeObj);

    // C++:  void SURF::operator()(Mat img, Mat mask, vector_KeyPoint& keypoints)
    private static native void n_detect(long nativeObj, long img_nativeObj, long mask_nativeObj, long keypoints_mat_nativeObj);

    // C++:  void SURF::operator()(Mat img, Mat mask, vector_KeyPoint& keypoints, vector_float& descriptors, bool useProvidedKeypoints = false)
    private static native void n_detect(long nativeObj, long img_nativeObj, long mask_nativeObj, long keypoints_mat_nativeObj, long descriptors_mat_nativeObj, boolean useProvidedKeypoints);
    private static native void n_detect(long nativeObj, long img_nativeObj, long mask_nativeObj, long keypoints_mat_nativeObj, long descriptors_mat_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
