
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvDTreeParams
public class CvDTreeParams {


    protected final long nativeObj;
    protected CvDTreeParams(long addr) { nativeObj = addr; }

    //
    // C++: // int max_categories
    //

    public  int get_max_categories()
    {

        int retVal = n_get_max_categories(nativeObj);

        return retVal;
    }


    //
    // C++: // int max_categories
    //

    public  void set_max_categories(int max_categories)
    {

        n_set_max_categories(nativeObj, max_categories);

        return;
    }


    //
    // C++: // int max_depth
    //

    public  int get_max_depth()
    {

        int retVal = n_get_max_depth(nativeObj);

        return retVal;
    }


    //
    // C++: // int max_depth
    //

    public  void set_max_depth(int max_depth)
    {

        n_set_max_depth(nativeObj, max_depth);

        return;
    }


    //
    // C++: // int min_sample_count
    //

    public  int get_min_sample_count()
    {

        int retVal = n_get_min_sample_count(nativeObj);

        return retVal;
    }


    //
    // C++: // int min_sample_count
    //

    public  void set_min_sample_count(int min_sample_count)
    {

        n_set_min_sample_count(nativeObj, min_sample_count);

        return;
    }


    //
    // C++: // int cv_folds
    //

    public  int get_cv_folds()
    {

        int retVal = n_get_cv_folds(nativeObj);

        return retVal;
    }


    //
    // C++: // int cv_folds
    //

    public  void set_cv_folds(int cv_folds)
    {

        n_set_cv_folds(nativeObj, cv_folds);

        return;
    }


    //
    // C++: // bool use_surrogates
    //

    public  boolean get_use_surrogates()
    {

        boolean retVal = n_get_use_surrogates(nativeObj);

        return retVal;
    }


    //
    // C++: // bool use_surrogates
    //

    public  void set_use_surrogates(boolean use_surrogates)
    {

        n_set_use_surrogates(nativeObj, use_surrogates);

        return;
    }


    //
    // C++: // bool use_1se_rule
    //

    public  boolean get_use_1se_rule()
    {

        boolean retVal = n_get_use_1se_rule(nativeObj);

        return retVal;
    }


    //
    // C++: // bool use_1se_rule
    //

    public  void set_use_1se_rule(boolean use_1se_rule)
    {

        n_set_use_1se_rule(nativeObj, use_1se_rule);

        return;
    }


    //
    // C++: // bool truncate_pruned_tree
    //

    public  boolean get_truncate_pruned_tree()
    {

        boolean retVal = n_get_truncate_pruned_tree(nativeObj);

        return retVal;
    }


    //
    // C++: // bool truncate_pruned_tree
    //

    public  void set_truncate_pruned_tree(boolean truncate_pruned_tree)
    {

        n_set_truncate_pruned_tree(nativeObj, truncate_pruned_tree);

        return;
    }


    //
    // C++: // float regression_accuracy
    //

    public  float get_regression_accuracy()
    {

        float retVal = n_get_regression_accuracy(nativeObj);

        return retVal;
    }


    //
    // C++: // float regression_accuracy
    //

    public  void set_regression_accuracy(float regression_accuracy)
    {

        n_set_regression_accuracy(nativeObj, regression_accuracy);

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

    // C++: // int max_categories
    private static native int n_get_max_categories(long nativeObj);

    // C++: // int max_categories
    private static native void n_set_max_categories(long nativeObj, int max_categories);

    // C++: // int max_depth
    private static native int n_get_max_depth(long nativeObj);

    // C++: // int max_depth
    private static native void n_set_max_depth(long nativeObj, int max_depth);

    // C++: // int min_sample_count
    private static native int n_get_min_sample_count(long nativeObj);

    // C++: // int min_sample_count
    private static native void n_set_min_sample_count(long nativeObj, int min_sample_count);

    // C++: // int cv_folds
    private static native int n_get_cv_folds(long nativeObj);

    // C++: // int cv_folds
    private static native void n_set_cv_folds(long nativeObj, int cv_folds);

    // C++: // bool use_surrogates
    private static native boolean n_get_use_surrogates(long nativeObj);

    // C++: // bool use_surrogates
    private static native void n_set_use_surrogates(long nativeObj, boolean use_surrogates);

    // C++: // bool use_1se_rule
    private static native boolean n_get_use_1se_rule(long nativeObj);

    // C++: // bool use_1se_rule
    private static native void n_set_use_1se_rule(long nativeObj, boolean use_1se_rule);

    // C++: // bool truncate_pruned_tree
    private static native boolean n_get_truncate_pruned_tree(long nativeObj);

    // C++: // bool truncate_pruned_tree
    private static native void n_set_truncate_pruned_tree(long nativeObj, boolean truncate_pruned_tree);

    // C++: // float regression_accuracy
    private static native float n_get_regression_accuracy(long nativeObj);

    // C++: // float regression_accuracy
    private static native void n_set_regression_accuracy(long nativeObj, float regression_accuracy);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
