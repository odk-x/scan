
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;
import org.opencv.core.*;
// C++: class KalmanFilter
public class KalmanFilter {


    protected final long nativeObj;
    protected KalmanFilter(long addr) { nativeObj = addr; }

    //
    // C++:   KalmanFilter::KalmanFilter()
    //

    /**
     * The constructors.
     *
     * The full constructor.
     *
     * Note: In C API when "CvKalman* kalmanFilter" structure is not needed anymore,
     * it should be released with "cvReleaseKalman(&kalmanFilter)"
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-kalmanfilter">org.opencv.video.KalmanFilter.KalmanFilter</a>
     */
    public   KalmanFilter()
    {

        nativeObj = n_KalmanFilter();

        return;
    }


    //
    // C++:   KalmanFilter::KalmanFilter(int dynamParams, int measureParams, int controlParams = 0, int type = CV_32F)
    //

    /**
     * The constructors.
     *
     * The full constructor.
     *
     * Note: In C API when "CvKalman* kalmanFilter" structure is not needed anymore,
     * it should be released with "cvReleaseKalman(&kalmanFilter)"
     *
     * @param dynamParams Dimensionality of the state.
     * @param measureParams Dimensionality of the measurement.
     * @param controlParams Dimensionality of the control vector.
     * @param type Type of the created matrices that should be "CV_32F" or "CV_64F".
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-kalmanfilter">org.opencv.video.KalmanFilter.KalmanFilter</a>
     */
    public   KalmanFilter(int dynamParams, int measureParams, int controlParams, int type)
    {

        nativeObj = n_KalmanFilter(dynamParams, measureParams, controlParams, type);

        return;
    }

    /**
     * The constructors.
     *
     * The full constructor.
     *
     * Note: In C API when "CvKalman* kalmanFilter" structure is not needed anymore,
     * it should be released with "cvReleaseKalman(&kalmanFilter)"
     *
     * @param dynamParams Dimensionality of the state.
     * @param measureParams Dimensionality of the measurement.
     * @param controlParams Dimensionality of the control vector.
     * @param type Type of the created matrices that should be "CV_32F" or "CV_64F".
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-kalmanfilter">org.opencv.video.KalmanFilter.KalmanFilter</a>
     */
    public   KalmanFilter(int dynamParams, int measureParams, int controlParams)
    {

        nativeObj = n_KalmanFilter(dynamParams, measureParams, controlParams);

        return;
    }

    /**
     * The constructors.
     *
     * The full constructor.
     *
     * Note: In C API when "CvKalman* kalmanFilter" structure is not needed anymore,
     * it should be released with "cvReleaseKalman(&kalmanFilter)"
     *
     * @param dynamParams Dimensionality of the state.
     * @param measureParams Dimensionality of the measurement.
     * @param controlParams Dimensionality of the control vector.
     * @param type Type of the created matrices that should be "CV_32F" or "CV_64F".
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-kalmanfilter">org.opencv.video.KalmanFilter.KalmanFilter</a>
     */
    public   KalmanFilter(int dynamParams, int measureParams)
    {

        nativeObj = n_KalmanFilter(dynamParams, measureParams);

        return;
    }


    //
    // C++:  Mat KalmanFilter::correct(Mat measurement)
    //

    /**
     * Updates the predicted state from the measurement.
     *
     * @param measurement a measurement
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-correct">org.opencv.video.KalmanFilter.correct</a>
     */
    public  Mat correct(Mat measurement)
    {

        Mat retVal = new Mat(n_correct(nativeObj, measurement.nativeObj));

        return retVal;
    }


    //
    // C++:  Mat KalmanFilter::predict(Mat control = Mat())
    //

    /**
     * Computes a predicted state.
     *
     * @param control The optional input control
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-predict">org.opencv.video.KalmanFilter.predict</a>
     */
    public  Mat predict(Mat control)
    {

        Mat retVal = new Mat(n_predict(nativeObj, control.nativeObj));

        return retVal;
    }

    /**
     * Computes a predicted state.
     *
     * @param control The optional input control
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#kalmanfilter-predict">org.opencv.video.KalmanFilter.predict</a>
     */
    public  Mat predict()
    {

        Mat retVal = new Mat(n_predict(nativeObj));

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

    // C++:   KalmanFilter::KalmanFilter()
    private static native long n_KalmanFilter();

    // C++:   KalmanFilter::KalmanFilter(int dynamParams, int measureParams, int controlParams = 0, int type = CV_32F)
    private static native long n_KalmanFilter(int dynamParams, int measureParams, int controlParams, int type);
    private static native long n_KalmanFilter(int dynamParams, int measureParams, int controlParams);
    private static native long n_KalmanFilter(int dynamParams, int measureParams);

    // C++:  Mat KalmanFilter::correct(Mat measurement)
    private static native long n_correct(long nativeObj, long measurement_nativeObj);

    // C++:  Mat KalmanFilter::predict(Mat control = Mat())
    private static native long n_predict(long nativeObj, long control_nativeObj);
    private static native long n_predict(long nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
