
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.calib3d;
import org.opencv.core.*;
// C++: class StereoSGBM
public class StereoSGBM {


    protected final long nativeObj;
    protected StereoSGBM(long addr) { nativeObj = addr; }

    public static final int
            DISP_SHIFT = 4,
            DISP_SCALE = (1<<DISP_SHIFT);


    //
    // C++:   StereoSGBM::StereoSGBM()
    //

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM()
    {

        nativeObj = n_StereoSGBM();

        return;
    }


    //
    // C++:   StereoSGBM::StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1 = 0, int P2 = 0, int disp12MaxDiff = 0, int preFilterCap = 0, int uniquenessRatio = 0, int speckleWindowSize = 0, int speckleRange = 0, bool fullDP = false)
    //

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange, boolean fullDP)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio, speckleWindowSize, speckleRange, fullDP);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio, speckleWindowSize, speckleRange);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio, speckleWindowSize);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff, preFilterCap);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2, disp12MaxDiff);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1, P2);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize, P1);

        return;
    }

    /**
     * Initializes "StereoSGBM" and sets parameters to custom values.??
     *
     * The first constructor initializes "StereoSGBM" with all the default
     * parameters. So, you only have to set "StereoSGBM.numberOfDisparities" at
     * minimum. The second constructor enables you to set each parameter to a custom
     * value.
     *
     * @param minDisparity Minimum possible disparity value. Normally, it is zero
     * but sometimes rectification algorithms can shift images, so this parameter
     * needs to be adjusted accordingly.
     * @param numDisparities Maximum disparity minus minimum disparity. The value is
     * always greater than zero. In the current implementation, this parameter must
     * be divisible by 16.
     * @param SADWindowSize Matched block size. It must be an odd number ">=1".
     * Normally, it should be somewhere in the "3..11" range.
     * @param P1 The first parameter controlling the disparity smoothness. See
     * below.
     * @param P2 The second parameter controlling the disparity smoothness. The
     * larger the values are, the smoother the disparity is. "P1" is the penalty on
     * the disparity change by plus or minus 1 between neighbor pixels. "P2" is the
     * penalty on the disparity change by more than 1 between neighbor pixels. The
     * algorithm requires "P2 > P1". See "stereo_match.cpp" sample where some
     * reasonably good "P1" and "P2" values are shown (like "8*number_of_image_channels*SADWindowSize*SADWindowSize"
     * and "32*number_of_image_channels*SADWindowSize*SADWindowSize", respectively).
     * @param disp12MaxDiff Maximum allowed difference (in integer pixel units) in
     * the left-right disparity check. Set it to a non-positive value to disable the
     * check.
     * @param preFilterCap Truncation value for the prefiltered image pixels. The
     * algorithm first computes x-derivative at each pixel and clips its value by
     * "[-preFilterCap, preFilterCap]" interval. The result values are passed to the
     * Birchfield-Tomasi pixel cost function.
     * @param uniquenessRatio Margin in percentage by which the best (minimum)
     * computed cost function value should "win" the second best value to consider
     * the found match correct. Normally, a value within the 5-15 range is good
     * enough.
     * @param speckleWindowSize Maximum size of smooth disparity regions to consider
     * their noise speckles and invalidate. Set it to 0 to disable speckle
     * filtering. Otherwise, set it somewhere in the 50-200 range.
     * @param speckleRange Maximum disparity variation within each connected
     * component. If you do speckle filtering, set the parameter to a positive
     * value, multiple of 16. Normally, 16 or 32 is good enough.
     * @param fullDP Set it to "true" to run the full-scale two-pass dynamic
     * programming algorithm. It will consume O(W*H*numDisparities) bytes, which is
     * large for 640x480 stereo and huge for HD-size pictures. By default, it is set
     * to "false".
     *
     * @see <a href="http://opencv.itseez.com/modules/calib3d/doc/camera_calibration_and_3d_reconstruction.html#stereosgbm-stereosgbm">org.opencv.calib3d.StereoSGBM.StereoSGBM</a>
     */
    public   StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize)
    {

        nativeObj = n_StereoSGBM(minDisparity, numDisparities, SADWindowSize);

        return;
    }


    //
    // C++:  void StereoSGBM::operator()(Mat left, Mat right, Mat& disp)
    //

    public  void compute(Mat left, Mat right, Mat disp)
    {

        n_compute(nativeObj, left.nativeObj, right.nativeObj, disp.nativeObj);

        return;
    }


    //
    // C++: // int minDisparity
    //

    public  int get_minDisparity()
    {

        int retVal = n_get_minDisparity(nativeObj);

        return retVal;
    }


    //
    // C++: // int minDisparity
    //

    public  void set_minDisparity(int minDisparity)
    {

        n_set_minDisparity(nativeObj, minDisparity);

        return;
    }


    //
    // C++: // int numberOfDisparities
    //

    public  int get_numberOfDisparities()
    {

        int retVal = n_get_numberOfDisparities(nativeObj);

        return retVal;
    }


    //
    // C++: // int numberOfDisparities
    //

    public  void set_numberOfDisparities(int numberOfDisparities)
    {

        n_set_numberOfDisparities(nativeObj, numberOfDisparities);

        return;
    }


    //
    // C++: // int SADWindowSize
    //

    public  int get_SADWindowSize()
    {

        int retVal = n_get_SADWindowSize(nativeObj);

        return retVal;
    }


    //
    // C++: // int SADWindowSize
    //

    public  void set_SADWindowSize(int SADWindowSize)
    {

        n_set_SADWindowSize(nativeObj, SADWindowSize);

        return;
    }


    //
    // C++: // int preFilterCap
    //

    public  int get_preFilterCap()
    {

        int retVal = n_get_preFilterCap(nativeObj);

        return retVal;
    }


    //
    // C++: // int preFilterCap
    //

    public  void set_preFilterCap(int preFilterCap)
    {

        n_set_preFilterCap(nativeObj, preFilterCap);

        return;
    }


    //
    // C++: // int uniquenessRatio
    //

    public  int get_uniquenessRatio()
    {

        int retVal = n_get_uniquenessRatio(nativeObj);

        return retVal;
    }


    //
    // C++: // int uniquenessRatio
    //

    public  void set_uniquenessRatio(int uniquenessRatio)
    {

        n_set_uniquenessRatio(nativeObj, uniquenessRatio);

        return;
    }


    //
    // C++: // int P1
    //

    public  int get_P1()
    {

        int retVal = n_get_P1(nativeObj);

        return retVal;
    }


    //
    // C++: // int P1
    //

    public  void set_P1(int P1)
    {

        n_set_P1(nativeObj, P1);

        return;
    }


    //
    // C++: // int P2
    //

    public  int get_P2()
    {

        int retVal = n_get_P2(nativeObj);

        return retVal;
    }


    //
    // C++: // int P2
    //

    public  void set_P2(int P2)
    {

        n_set_P2(nativeObj, P2);

        return;
    }


    //
    // C++: // int speckleWindowSize
    //

    public  int get_speckleWindowSize()
    {

        int retVal = n_get_speckleWindowSize(nativeObj);

        return retVal;
    }


    //
    // C++: // int speckleWindowSize
    //

    public  void set_speckleWindowSize(int speckleWindowSize)
    {

        n_set_speckleWindowSize(nativeObj, speckleWindowSize);

        return;
    }


    //
    // C++: // int speckleRange
    //

    public  int get_speckleRange()
    {

        int retVal = n_get_speckleRange(nativeObj);

        return retVal;
    }


    //
    // C++: // int speckleRange
    //

    public  void set_speckleRange(int speckleRange)
    {

        n_set_speckleRange(nativeObj, speckleRange);

        return;
    }


    //
    // C++: // int disp12MaxDiff
    //

    public  int get_disp12MaxDiff()
    {

        int retVal = n_get_disp12MaxDiff(nativeObj);

        return retVal;
    }


    //
    // C++: // int disp12MaxDiff
    //

    public  void set_disp12MaxDiff(int disp12MaxDiff)
    {

        n_set_disp12MaxDiff(nativeObj, disp12MaxDiff);

        return;
    }


    //
    // C++: // bool fullDP
    //

    public  boolean get_fullDP()
    {

        boolean retVal = n_get_fullDP(nativeObj);

        return retVal;
    }


    //
    // C++: // bool fullDP
    //

    public  void set_fullDP(boolean fullDP)
    {

        n_set_fullDP(nativeObj, fullDP);

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

    // C++:   StereoSGBM::StereoSGBM()
    private static native long n_StereoSGBM();

    // C++:   StereoSGBM::StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1 = 0, int P2 = 0, int disp12MaxDiff = 0, int preFilterCap = 0, int uniquenessRatio = 0, int speckleWindowSize = 0, int speckleRange = 0, bool fullDP = false)
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange, boolean fullDP);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize, int speckleRange);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio, int speckleWindowSize);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap, int uniquenessRatio);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff, int preFilterCap);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2, int disp12MaxDiff);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1, int P2);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize, int P1);
    private static native long n_StereoSGBM(int minDisparity, int numDisparities, int SADWindowSize);

    // C++:  void StereoSGBM::operator()(Mat left, Mat right, Mat& disp)
    private static native void n_compute(long nativeObj, long left_nativeObj, long right_nativeObj, long disp_nativeObj);

    // C++: // int minDisparity
    private static native int n_get_minDisparity(long nativeObj);

    // C++: // int minDisparity
    private static native void n_set_minDisparity(long nativeObj, int minDisparity);

    // C++: // int numberOfDisparities
    private static native int n_get_numberOfDisparities(long nativeObj);

    // C++: // int numberOfDisparities
    private static native void n_set_numberOfDisparities(long nativeObj, int numberOfDisparities);

    // C++: // int SADWindowSize
    private static native int n_get_SADWindowSize(long nativeObj);

    // C++: // int SADWindowSize
    private static native void n_set_SADWindowSize(long nativeObj, int SADWindowSize);

    // C++: // int preFilterCap
    private static native int n_get_preFilterCap(long nativeObj);

    // C++: // int preFilterCap
    private static native void n_set_preFilterCap(long nativeObj, int preFilterCap);

    // C++: // int uniquenessRatio
    private static native int n_get_uniquenessRatio(long nativeObj);

    // C++: // int uniquenessRatio
    private static native void n_set_uniquenessRatio(long nativeObj, int uniquenessRatio);

    // C++: // int P1
    private static native int n_get_P1(long nativeObj);

    // C++: // int P1
    private static native void n_set_P1(long nativeObj, int P1);

    // C++: // int P2
    private static native int n_get_P2(long nativeObj);

    // C++: // int P2
    private static native void n_set_P2(long nativeObj, int P2);

    // C++: // int speckleWindowSize
    private static native int n_get_speckleWindowSize(long nativeObj);

    // C++: // int speckleWindowSize
    private static native void n_set_speckleWindowSize(long nativeObj, int speckleWindowSize);

    // C++: // int speckleRange
    private static native int n_get_speckleRange(long nativeObj);

    // C++: // int speckleRange
    private static native void n_set_speckleRange(long nativeObj, int speckleRange);

    // C++: // int disp12MaxDiff
    private static native int n_get_disp12MaxDiff(long nativeObj);

    // C++: // int disp12MaxDiff
    private static native void n_set_disp12MaxDiff(long nativeObj, int disp12MaxDiff);

    // C++: // bool fullDP
    private static native boolean n_get_fullDP(long nativeObj);

    // C++: // bool fullDP
    private static native void n_set_fullDP(long nativeObj, boolean fullDP);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
