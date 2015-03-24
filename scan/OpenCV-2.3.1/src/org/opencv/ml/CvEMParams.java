
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvEMParams
public class CvEMParams {


    protected final long nativeObj;
    protected CvEMParams(long addr) { nativeObj = addr; }

    //
    // C++: // int nclusters
    //

    public  int get_nclusters()
    {

        int retVal = n_get_nclusters(nativeObj);

        return retVal;
    }


    //
    // C++: // int nclusters
    //

    public  void set_nclusters(int nclusters)
    {

        n_set_nclusters(nativeObj, nclusters);

        return;
    }


    //
    // C++: // int cov_mat_type
    //

    public  int get_cov_mat_type()
    {

        int retVal = n_get_cov_mat_type(nativeObj);

        return retVal;
    }


    //
    // C++: // int cov_mat_type
    //

    public  void set_cov_mat_type(int cov_mat_type)
    {

        n_set_cov_mat_type(nativeObj, cov_mat_type);

        return;
    }


    //
    // C++: // int start_step
    //

    public  int get_start_step()
    {

        int retVal = n_get_start_step(nativeObj);

        return retVal;
    }


    //
    // C++: // int start_step
    //

    public  void set_start_step(int start_step)
    {

        n_set_start_step(nativeObj, start_step);

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

    // C++: // int nclusters
    private static native int n_get_nclusters(long nativeObj);

    // C++: // int nclusters
    private static native void n_set_nclusters(long nativeObj, int nclusters);

    // C++: // int cov_mat_type
    private static native int n_get_cov_mat_type(long nativeObj);

    // C++: // int cov_mat_type
    private static native void n_set_cov_mat_type(long nativeObj, int cov_mat_type);

    // C++: // int start_step
    private static native int n_get_start_step(long nativeObj);

    // C++: // int start_step
    private static native void n_set_start_step(long nativeObj, int start_step);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
