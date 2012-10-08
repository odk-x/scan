
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvGBTreesParams
public class CvGBTreesParams {


    protected final long nativeObj;
    protected CvGBTreesParams(long addr) { nativeObj = addr; }

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
    // C++: // int loss_function_type
    //

    public  int get_loss_function_type()
    {

        int retVal = n_get_loss_function_type(nativeObj);

        return retVal;
    }


    //
    // C++: // int loss_function_type
    //

    public  void set_loss_function_type(int loss_function_type)
    {

        n_set_loss_function_type(nativeObj, loss_function_type);

        return;
    }


    //
    // C++: // float subsample_portion
    //

    public  float get_subsample_portion()
    {

        float retVal = n_get_subsample_portion(nativeObj);

        return retVal;
    }


    //
    // C++: // float subsample_portion
    //

    public  void set_subsample_portion(float subsample_portion)
    {

        n_set_subsample_portion(nativeObj, subsample_portion);

        return;
    }


    //
    // C++: // float shrinkage
    //

    public  float get_shrinkage()
    {

        float retVal = n_get_shrinkage(nativeObj);

        return retVal;
    }


    //
    // C++: // float shrinkage
    //

    public  void set_shrinkage(float shrinkage)
    {

        n_set_shrinkage(nativeObj, shrinkage);

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

    // C++: // int weak_count
    private static native int n_get_weak_count(long nativeObj);

    // C++: // int weak_count
    private static native void n_set_weak_count(long nativeObj, int weak_count);

    // C++: // int loss_function_type
    private static native int n_get_loss_function_type(long nativeObj);

    // C++: // int loss_function_type
    private static native void n_set_loss_function_type(long nativeObj, int loss_function_type);

    // C++: // float subsample_portion
    private static native float n_get_subsample_portion(long nativeObj);

    // C++: // float subsample_portion
    private static native void n_set_subsample_portion(long nativeObj, float subsample_portion);

    // C++: // float shrinkage
    private static native float n_get_shrinkage(long nativeObj);

    // C++: // float shrinkage
    private static native void n_set_shrinkage(long nativeObj, float shrinkage);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
