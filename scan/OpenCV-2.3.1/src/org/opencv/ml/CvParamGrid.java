
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvParamGrid
public class CvParamGrid {


    protected final long nativeObj;
    protected CvParamGrid(long addr) { nativeObj = addr; }

    public static final int
            SVM_C = 0,
            SVM_GAMMA = 1,
            SVM_P = 2,
            SVM_NU = 3,
            SVM_COEF = 4,
            SVM_DEGREE = 5;


    //
    // C++: // double min_val
    //

    public  double get_min_val()
    {

        double retVal = n_get_min_val(nativeObj);

        return retVal;
    }


    //
    // C++: // double min_val
    //

    public  void set_min_val(double min_val)
    {

        n_set_min_val(nativeObj, min_val);

        return;
    }


    //
    // C++: // double max_val
    //

    public  double get_max_val()
    {

        double retVal = n_get_max_val(nativeObj);

        return retVal;
    }


    //
    // C++: // double max_val
    //

    public  void set_max_val(double max_val)
    {

        n_set_max_val(nativeObj, max_val);

        return;
    }


    //
    // C++: // double step
    //

    public  double get_step()
    {

        double retVal = n_get_step(nativeObj);

        return retVal;
    }


    //
    // C++: // double step
    //

    public  void set_step(double step)
    {

        n_set_step(nativeObj, step);

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

    // C++: // double min_val
    private static native double n_get_min_val(long nativeObj);

    // C++: // double min_val
    private static native void n_set_min_val(long nativeObj, double min_val);

    // C++: // double max_val
    private static native double n_get_max_val(long nativeObj);

    // C++: // double max_val
    private static native void n_set_max_val(long nativeObj, double max_val);

    // C++: // double step
    private static native double n_get_step(long nativeObj);

    // C++: // double step
    private static native void n_set_step(long nativeObj, double step);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
