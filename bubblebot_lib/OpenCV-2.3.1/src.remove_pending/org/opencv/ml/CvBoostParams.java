
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvBoostParams
public class CvBoostParams {


    protected final long nativeObj;
    protected CvBoostParams(long addr) { nativeObj = addr; }

    //
    // C++: // int boost_type
    //

    public  int get_boost_type()
    {

        int retVal = n_get_boost_type(nativeObj);

        return retVal;
    }


    //
    // C++: // int boost_type
    //

    public  void set_boost_type(int boost_type)
    {

        n_set_boost_type(nativeObj, boost_type);

        return;
    }


    //
    // C++: // int weak_count
    //

    public  int get_weak_count()
    {

        int retVal = n_get_weak_count(nativeObj);

        return retVal;
    }


    //
    // C++: // int weak_count
    //

    public  void set_weak_count(int weak_count)
    {

        n_set_weak_count(nativeObj, weak_count);

        return;
    }


    //
    // C++: // int split_criteria
    //

    public  int get_split_criteria()
    {

        int retVal = n_get_split_criteria(nativeObj);

        return retVal;
    }


    //
    // C++: // int split_criteria
    //

    public  void set_split_criteria(int split_criteria)
    {

        n_set_split_criteria(nativeObj, split_criteria);

        return;
    }


    //
    // C++: // double weight_trim_rate
    //

    public  double get_weight_trim_rate()
    {

        double retVal = n_get_weight_trim_rate(nativeObj);

        return retVal;
    }


    //
    // C++: // double weight_trim_rate
    //

    public  void set_weight_trim_rate(double weight_trim_rate)
    {

        n_set_weight_trim_rate(nativeObj, weight_trim_rate);

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

    // C++: // int boost_type
    private static native int n_get_boost_type(long nativeObj);

    // C++: // int boost_type
    private static native void n_set_boost_type(long nativeObj, int boost_type);

    // C++: // int weak_count
    private static native int n_get_weak_count(long nativeObj);

    // C++: // int weak_count
    private static native void n_set_weak_count(long nativeObj, int weak_count);

    // C++: // int split_criteria
    private static native int n_get_split_criteria(long nativeObj);

    // C++: // int split_criteria
    private static native void n_set_split_criteria(long nativeObj, int split_criteria);

    // C++: // double weight_trim_rate
    private static native double n_get_weight_trim_rate(long nativeObj);

    // C++: // double weight_trim_rate
    private static native void n_set_weight_trim_rate(long nativeObj, double weight_trim_rate);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
