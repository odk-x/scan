
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;
import org.opencv.core.*;
// C++: class BackgroundSubtractor
public class BackgroundSubtractor {


    protected final long nativeObj;
    protected BackgroundSubtractor(long addr) { nativeObj = addr; }

    //
    // C++:  void BackgroundSubtractor::operator()(Mat image, Mat& fgmask, double learningRate = 0)
    //

    /**
     * Computes a foreground mask.
     *
     * @param image Next video frame.
     * @param fgmask The output foreground mask as an 8-bit binary image.
     * @param learningRate a learningRate
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#backgroundsubtractor-operator">org.opencv.video.BackgroundSubtractor.operator()</a>
     */
    public  void apply(Mat image, Mat fgmask, double learningRate)
    {

        n_apply(nativeObj, image.nativeObj, fgmask.nativeObj, learningRate);

        return;
    }

    /**
     * Computes a foreground mask.
     *
     * @param image Next video frame.
     * @param fgmask The output foreground mask as an 8-bit binary image.
     * @param learningRate a learningRate
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#backgroundsubtractor-operator">org.opencv.video.BackgroundSubtractor.operator()</a>
     */
    public  void apply(Mat image, Mat fgmask)
    {

        n_apply(nativeObj, image.nativeObj, fgmask.nativeObj);

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

    // C++:  void BackgroundSubtractor::operator()(Mat image, Mat& fgmask, double learningRate = 0)
    private static native void n_apply(long nativeObj, long image_nativeObj, long fgmask_nativeObj, double learningRate);
    private static native void n_apply(long nativeObj, long image_nativeObj, long fgmask_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
