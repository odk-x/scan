
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvSVMParams
public class CvSVMParams {


    protected final long nativeObj;
    protected CvSVMParams(long addr) { nativeObj = addr; }

    //
    // C++: // int svm_type
    //

    public  int get_svm_type()
    {

        int retVal = n_get_svm_type(nativeObj);

        return retVal;
    }


    //
    // C++: // int svm_type
    //

    public  void set_svm_type(int svm_type)
    {

        n_set_svm_type(nativeObj, svm_type);

        return;
    }


    //
    // C++: // int kernel_type
    //

    public  int get_kernel_type()
    {

        int retVal = n_get_kernel_type(nativeObj);

        return retVal;
    }


    //
    // C++: // int kernel_type
    //

    public  void set_kernel_type(int kernel_type)
    {

        n_set_kernel_type(nativeObj, kernel_type);

        return;
    }


    //
    // C++: // double degree
    //

    public  double get_degree()
    {

        double retVal = n_get_degree(nativeObj);

        return retVal;
    }


    //
    // C++: // double degree
    //

    public  void set_degree(double degree)
    {

        n_set_degree(nativeObj, degree);

        return;
    }


    //
    // C++: // double gamma
    //

    public  double get_gamma()
    {

        double retVal = n_get_gamma(nativeObj);

        return retVal;
    }


    //
    // C++: // double gamma
    //

    public  void set_gamma(double gamma)
    {

        n_set_gamma(nativeObj, gamma);

        return;
    }


    //
    // C++: // double coef0
    //

    public  double get_coef0()
    {

        double retVal = n_get_coef0(nativeObj);

        return retVal;
    }


    //
    // C++: // double coef0
    //

    public  void set_coef0(double coef0)
    {

        n_set_coef0(nativeObj, coef0);

        return;
    }


    //
    // C++: // double C
    //

    public  double get_C()
    {

        double retVal = n_get_C(nativeObj);

        return retVal;
    }


    //
    // C++: // double C
    //

    public  void set_C(double C)
    {

        n_set_C(nativeObj, C);

        return;
    }


    //
    // C++: // double nu
    //

    public  double get_nu()
    {

        double retVal = n_get_nu(nativeObj);

        return retVal;
    }


    //
    // C++: // double nu
    //

    public  void set_nu(double nu)
    {

        n_set_nu(nativeObj, nu);

        return;
    }


    //
    // C++: // double p
    //

    public  double get_p()
    {

        double retVal = n_get_p(nativeObj);

        return retVal;
    }


    //
    // C++: // double p
    //

    public  void set_p(double p)
    {

        n_set_p(nativeObj, p);

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

    // C++: // int svm_type
    private static native int n_get_svm_type(long nativeObj);

    // C++: // int svm_type
    private static native void n_set_svm_type(long nativeObj, int svm_type);

    // C++: // int kernel_type
    private static native int n_get_kernel_type(long nativeObj);

    // C++: // int kernel_type
    private static native void n_set_kernel_type(long nativeObj, int kernel_type);

    // C++: // double degree
    private static native double n_get_degree(long nativeObj);

    // C++: // double degree
    private static native void n_set_degree(long nativeObj, double degree);

    // C++: // double gamma
    private static native double n_get_gamma(long nativeObj);

    // C++: // double gamma
    private static native void n_set_gamma(long nativeObj, double gamma);

    // C++: // double coef0
    private static native double n_get_coef0(long nativeObj);

    // C++: // double coef0
    private static native void n_set_coef0(long nativeObj, double coef0);

    // C++: // double C
    private static native double n_get_C(long nativeObj);

    // C++: // double C
    private static native void n_set_C(long nativeObj, double C);

    // C++: // double nu
    private static native double n_get_nu(long nativeObj);

    // C++: // double nu
    private static native void n_set_nu(long nativeObj, double nu);

    // C++: // double p
    private static native double n_get_p(long nativeObj);

    // C++: // double p
    private static native void n_set_p(long nativeObj, double p);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
