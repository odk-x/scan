
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvANN_MLP_TrainParams
public class CvANN_MLP_TrainParams {


    protected final long nativeObj;
    protected CvANN_MLP_TrainParams(long addr) { nativeObj = addr; }

    public static final int
            BACKPROP = 0,
            RPROP = 1;


    //
    // C++: // CvTermCriteria term_crit
    //

    // Return type 'CvTermCriteria' is not supported, skipping the function


    //
    // C++: // CvTermCriteria term_crit
    //

    // Unknown type 'CvTermCriteria' (I), skipping the function


    //
    // C++: // int train_method
    //

    public  int get_train_method()
    {

        int retVal = n_get_train_method(nativeObj);

        return retVal;
    }


    //
    // C++: // int train_method
    //

    public  void set_train_method(int train_method)
    {

        n_set_train_method(nativeObj, train_method);

        return;
    }


    //
    // C++: // double bp_dw_scale
    //

    public  double get_bp_dw_scale()
    {

        double retVal = n_get_bp_dw_scale(nativeObj);

        return retVal;
    }


    //
    // C++: // double bp_dw_scale
    //

    public  void set_bp_dw_scale(double bp_dw_scale)
    {

        n_set_bp_dw_scale(nativeObj, bp_dw_scale);

        return;
    }


    //
    // C++: // double bp_moment_scale
    //

    public  double get_bp_moment_scale()
    {

        double retVal = n_get_bp_moment_scale(nativeObj);

        return retVal;
    }


    //
    // C++: // double bp_moment_scale
    //

    public  void set_bp_moment_scale(double bp_moment_scale)
    {

        n_set_bp_moment_scale(nativeObj, bp_moment_scale);

        return;
    }


    //
    // C++: // double rp_dw0
    //

    public  double get_rp_dw0()
    {

        double retVal = n_get_rp_dw0(nativeObj);

        return retVal;
    }


    //
    // C++: // double rp_dw0
    //

    public  void set_rp_dw0(double rp_dw0)
    {

        n_set_rp_dw0(nativeObj, rp_dw0);

        return;
    }


    //
    // C++: // double rp_dw_plus
    //

    public  double get_rp_dw_plus()
    {

        double retVal = n_get_rp_dw_plus(nativeObj);

        return retVal;
    }


    //
    // C++: // double rp_dw_plus
    //

    public  void set_rp_dw_plus(double rp_dw_plus)
    {

        n_set_rp_dw_plus(nativeObj, rp_dw_plus);

        return;
    }


    //
    // C++: // double rp_dw_minus
    //

    public  double get_rp_dw_minus()
    {

        double retVal = n_get_rp_dw_minus(nativeObj);

        return retVal;
    }


    //
    // C++: // double rp_dw_minus
    //

    public  void set_rp_dw_minus(double rp_dw_minus)
    {

        n_set_rp_dw_minus(nativeObj, rp_dw_minus);

        return;
    }


    //
    // C++: // double rp_dw_min
    //

    public  double get_rp_dw_min()
    {

        double retVal = n_get_rp_dw_min(nativeObj);

        return retVal;
    }


    //
    // C++: // double rp_dw_min
    //

    public  void set_rp_dw_min(double rp_dw_min)
    {

        n_set_rp_dw_min(nativeObj, rp_dw_min);

        return;
    }


    //
    // C++: // double rp_dw_max
    //

    public  double get_rp_dw_max()
    {

        double retVal = n_get_rp_dw_max(nativeObj);

        return retVal;
    }


    //
    // C++: // double rp_dw_max
    //

    public  void set_rp_dw_max(double rp_dw_max)
    {

        n_set_rp_dw_max(nativeObj, rp_dw_max);

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

    // C++: // int train_method
    private static native int n_get_train_method(long nativeObj);

    // C++: // int train_method
    private static native void n_set_train_method(long nativeObj, int train_method);

    // C++: // double bp_dw_scale
    private static native double n_get_bp_dw_scale(long nativeObj);

    // C++: // double bp_dw_scale
    private static native void n_set_bp_dw_scale(long nativeObj, double bp_dw_scale);

    // C++: // double bp_moment_scale
    private static native double n_get_bp_moment_scale(long nativeObj);

    // C++: // double bp_moment_scale
    private static native void n_set_bp_moment_scale(long nativeObj, double bp_moment_scale);

    // C++: // double rp_dw0
    private static native double n_get_rp_dw0(long nativeObj);

    // C++: // double rp_dw0
    private static native void n_set_rp_dw0(long nativeObj, double rp_dw0);

    // C++: // double rp_dw_plus
    private static native double n_get_rp_dw_plus(long nativeObj);

    // C++: // double rp_dw_plus
    private static native void n_set_rp_dw_plus(long nativeObj, double rp_dw_plus);

    // C++: // double rp_dw_minus
    private static native double n_get_rp_dw_minus(long nativeObj);

    // C++: // double rp_dw_minus
    private static native void n_set_rp_dw_minus(long nativeObj, double rp_dw_minus);

    // C++: // double rp_dw_min
    private static native double n_get_rp_dw_min(long nativeObj);

    // C++: // double rp_dw_min
    private static native void n_set_rp_dw_min(long nativeObj, double rp_dw_min);

    // C++: // double rp_dw_max
    private static native double n_get_rp_dw_max(long nativeObj);

    // C++: // double rp_dw_max
    private static native void n_set_rp_dw_max(long nativeObj, double rp_dw_max);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
