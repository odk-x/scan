
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.video;
// C++: class BackgroundSubtractorMOG
public class BackgroundSubtractorMOG {


    protected final long nativeObj;
    protected BackgroundSubtractorMOG(long addr) { nativeObj = addr; }

    //
    // C++:   BackgroundSubtractorMOG::BackgroundSubtractorMOG()
    //

    /**
     * The contructors
     *
     * Default constructor sets all parameters to default values.
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#backgroundsubtractormog-backgroundsubtractormog">org.opencv.video.BackgroundSubtractorMOG.BackgroundSubtractorMOG</a>
     */
    public   BackgroundSubtractorMOG()
    {

        nativeObj = n_BackgroundSubtractorMOG();

        return;
    }


    //
    // C++:   BackgroundSubtractorMOG::BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio, double noiseSigma = 0)
    //

    /**
     * The contructors
     *
     * Default constructor sets all parameters to default values.
     *
     * @param history Length of the history.
     * @param nmixtures Number of Gaussian mixtures.
     * @param backgroundRatio Background ratio.
     * @param noiseSigma Noise strength.
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#backgroundsubtractormog-backgroundsubtractormog">org.opencv.video.BackgroundSubtractorMOG.BackgroundSubtractorMOG</a>
     */
    public   BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio, double noiseSigma)
    {

        nativeObj = n_BackgroundSubtractorMOG(history, nmixtures, backgroundRatio, noiseSigma);

        return;
    }

    /**
     * The contructors
     *
     * Default constructor sets all parameters to default values.
     *
     * @param history Length of the history.
     * @param nmixtures Number of Gaussian mixtures.
     * @param backgroundRatio Background ratio.
     * @param noiseSigma Noise strength.
     *
     * @see <a href="http://opencv.itseez.com/modules/video/doc/motion_analysis_and_object_tracking.html#backgroundsubtractormog-backgroundsubtractormog">org.opencv.video.BackgroundSubtractorMOG.BackgroundSubtractorMOG</a>
     */
    public   BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio)
    {

        nativeObj = n_BackgroundSubtractorMOG(history, nmixtures, backgroundRatio);

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

    // C++:   BackgroundSubtractorMOG::BackgroundSubtractorMOG()
    private static native long n_BackgroundSubtractorMOG();

    // C++:   BackgroundSubtractorMOG::BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio, double noiseSigma = 0)
    private static native long n_BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio, double noiseSigma);
    private static native long n_BackgroundSubtractorMOG(int history, int nmixtures, double backgroundRatio);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
