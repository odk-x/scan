
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvRTParams
public class CvRTParams {


    protected final long nativeObj;
    protected CvRTParams(long addr) { nativeObj = addr; }

    //
    // C++: // bool calc_var_importance
    //

    public  boolean get_calc_var_importance()
    {

        boolean retVal = n_get_calc_var_importance(nativeObj);

        return retVal;
    }


    //
    // C++: // bool calc_var_importance
    //

    public  void set_calc_var_importance(boolean calc_var_importance)
    {

        n_set_calc_var_importance(nativeObj, calc_var_importance);

        return;
    }


    //
    // C++: // int nactive_vars
    //

    public  int get_nactive_vars()
    {

        int retVal = n_get_nactive_vars(nativeObj);

        return retVal;
    }


    //
    // C++: // int nactive_vars
    //

    public  void set_nactive_vars(int nactive_vars)
    {

        n_set_nactive_vars(nativeObj, nactive_vars);

        return;
    }


    //
    // C++: // CvTermCriteria term_crit
    //

    // Return type 'CvTermCriteria' is not supported, skipping the function


    //
    // C++: // CvTermCriteria term_crit
    //

    // Unknown type 'CvTermCriteria' (I), skipping the function


    @Override
    protected void finalize() throws Throwable {
        n_delete(nativeObj);
        super.finalize();
    }



    //
    // native stuff
    //
    static { System.loadLibrary("opencv_java"); }

    // C++: // bool calc_var_importance
    private static native boolean n_get_calc_var_importance(long nativeObj);

    // C++: // bool calc_var_importance
    private static native void n_set_calc_var_importance(long nativeObj, boolean calc_var_importance);

    // C++: // int nactive_vars
    private static native int n_get_nactive_vars(long nativeObj);

    // C++: // int nactive_vars
    private static native void n_set_nactive_vars(long nativeObj, int nactive_vars);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
