
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.core;

// C++: class KDTree
public class KDTree {


    protected final long nativeObj;
    protected KDTree(long addr) { nativeObj = addr; }

    //
    // C++:   KDTree::KDTree()
    //

    public   KDTree()
    {

        nativeObj = n_KDTree();

        return;
    }


    //
    // C++:   KDTree::KDTree(Mat points, bool copyAndReorderPoints = false)
    //

    public   KDTree(Mat points, boolean copyAndReorderPoints)
    {

        nativeObj = n_KDTree(points.nativeObj, copyAndReorderPoints);

        return;
    }

    public   KDTree(Mat points)
    {

        nativeObj = n_KDTree(points.nativeObj);

        return;
    }


    //
    // C++:   KDTree::KDTree(Mat points, Mat _labels, bool copyAndReorderPoints = false)
    //

    public   KDTree(Mat points, Mat _labels, boolean copyAndReorderPoints)
    {

        nativeObj = n_KDTree(points.nativeObj, _labels.nativeObj, copyAndReorderPoints);

        return;
    }

    public   KDTree(Mat points, Mat _labels)
    {

        nativeObj = n_KDTree(points.nativeObj, _labels.nativeObj);

        return;
    }


    //
    // C++:  void KDTree::build(Mat points, bool copyAndReorderPoints = false)
    //

    public  void build(Mat points, boolean copyAndReorderPoints)
    {

        n_build(nativeObj, points.nativeObj, copyAndReorderPoints);

        return;
    }

    public  void build(Mat points)
    {

        n_build(nativeObj, points.nativeObj);

        return;
    }


    //
    // C++:  void KDTree::build(Mat points, Mat labels, bool copyAndReorderPoints = false)
    //

    public  void build(Mat points, Mat labels, boolean copyAndReorderPoints)
    {

        n_build(nativeObj, points.nativeObj, labels.nativeObj, copyAndReorderPoints);

        return;
    }

    public  void build(Mat points, Mat labels)
    {

        n_build(nativeObj, points.nativeObj, labels.nativeObj);

        return;
    }


    //
    // C++:  int KDTree::dims()
    //

    public  int dims()
    {

        int retVal = n_dims(nativeObj);

        return retVal;
    }


    //
    // C++:  int KDTree::findNearest(Mat vec, int K, int Emax, Mat& neighborsIdx, Mat& neighbors = Mat(), Mat& dist = Mat(), Mat& labels = Mat())
    //

    public  int findNearest(Mat vec, int K, int Emax, Mat neighborsIdx, Mat neighbors, Mat dist, Mat labels)
    {

        int retVal = n_findNearest(nativeObj, vec.nativeObj, K, Emax, neighborsIdx.nativeObj, neighbors.nativeObj, dist.nativeObj, labels.nativeObj);

        return retVal;
    }

    public  int findNearest(Mat vec, int K, int Emax, Mat neighborsIdx, Mat neighbors, Mat dist)
    {

        int retVal = n_findNearest(nativeObj, vec.nativeObj, K, Emax, neighborsIdx.nativeObj, neighbors.nativeObj, dist.nativeObj);

        return retVal;
    }

    public  int findNearest(Mat vec, int K, int Emax, Mat neighborsIdx, Mat neighbors)
    {

        int retVal = n_findNearest(nativeObj, vec.nativeObj, K, Emax, neighborsIdx.nativeObj, neighbors.nativeObj);

        return retVal;
    }

    public  int findNearest(Mat vec, int K, int Emax, Mat neighborsIdx)
    {

        int retVal = n_findNearest(nativeObj, vec.nativeObj, K, Emax, neighborsIdx.nativeObj);

        return retVal;
    }


    //
    // C++:  void KDTree::findOrthoRange(Mat minBounds, Mat maxBounds, Mat& neighborsIdx, Mat& neighbors = Mat(), Mat& labels = Mat())
    //

    public  void findOrthoRange(Mat minBounds, Mat maxBounds, Mat neighborsIdx, Mat neighbors, Mat labels)
    {

        n_findOrthoRange(nativeObj, minBounds.nativeObj, maxBounds.nativeObj, neighborsIdx.nativeObj, neighbors.nativeObj, labels.nativeObj);

        return;
    }

    public  void findOrthoRange(Mat minBounds, Mat maxBounds, Mat neighborsIdx, Mat neighbors)
    {

        n_findOrthoRange(nativeObj, minBounds.nativeObj, maxBounds.nativeObj, neighborsIdx.nativeObj, neighbors.nativeObj);

        return;
    }

    public  void findOrthoRange(Mat minBounds, Mat maxBounds, Mat neighborsIdx)
    {

        n_findOrthoRange(nativeObj, minBounds.nativeObj, maxBounds.nativeObj, neighborsIdx.nativeObj);

        return;
    }


    //
    // C++:  void KDTree::getPoints(Mat idx, Mat& pts, Mat& labels = Mat())
    //

    public  void getPoints(Mat idx, Mat pts, Mat labels)
    {

        n_getPoints(nativeObj, idx.nativeObj, pts.nativeObj, labels.nativeObj);

        return;
    }

    public  void getPoints(Mat idx, Mat pts)
    {

        n_getPoints(nativeObj, idx.nativeObj, pts.nativeObj);

        return;
    }


    //
    // C++: // Mat points
    //

    public  Mat get_points()
    {

        Mat retVal = new Mat(n_get_points(nativeObj));

        return retVal;
    }


    //
    // C++: // int maxDepth
    //

    public  int get_maxDepth()
    {

        int retVal = n_get_maxDepth(nativeObj);

        return retVal;
    }


    //
    // C++: // int normType
    //

    public  int get_normType()
    {

        int retVal = n_get_normType(nativeObj);

        return retVal;
    }


    //
    // C++: // int normType
    //

    public  void set_normType(int normType)
    {

        n_set_normType(nativeObj, normType);

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

    // C++:   KDTree::KDTree()
    private static native long n_KDTree();

    // C++:   KDTree::KDTree(Mat points, bool copyAndReorderPoints = false)
    private static native long n_KDTree(long points_nativeObj, boolean copyAndReorderPoints);
    private static native long n_KDTree(long points_nativeObj);

    // C++:   KDTree::KDTree(Mat points, Mat _labels, bool copyAndReorderPoints = false)
    private static native long n_KDTree(long points_nativeObj, long _labels_nativeObj, boolean copyAndReorderPoints);
    private static native long n_KDTree(long points_nativeObj, long _labels_nativeObj);

    // C++:  void KDTree::build(Mat points, bool copyAndReorderPoints = false)
    private static native void n_build(long nativeObj, long points_nativeObj, boolean copyAndReorderPoints);
    private static native void n_build(long nativeObj, long points_nativeObj);

    // C++:  void KDTree::build(Mat points, Mat labels, bool copyAndReorderPoints = false)
    private static native void n_build(long nativeObj, long points_nativeObj, long labels_nativeObj, boolean copyAndReorderPoints);
    private static native void n_build(long nativeObj, long points_nativeObj, long labels_nativeObj);

    // C++:  int KDTree::dims()
    private static native int n_dims(long nativeObj);

    // C++:  int KDTree::findNearest(Mat vec, int K, int Emax, Mat& neighborsIdx, Mat& neighbors = Mat(), Mat& dist = Mat(), Mat& labels = Mat())
    private static native int n_findNearest(long nativeObj, long vec_nativeObj, int K, int Emax, long neighborsIdx_nativeObj, long neighbors_nativeObj, long dist_nativeObj, long labels_nativeObj);
    private static native int n_findNearest(long nativeObj, long vec_nativeObj, int K, int Emax, long neighborsIdx_nativeObj, long neighbors_nativeObj, long dist_nativeObj);
    private static native int n_findNearest(long nativeObj, long vec_nativeObj, int K, int Emax, long neighborsIdx_nativeObj, long neighbors_nativeObj);
    private static native int n_findNearest(long nativeObj, long vec_nativeObj, int K, int Emax, long neighborsIdx_nativeObj);

    // C++:  void KDTree::findOrthoRange(Mat minBounds, Mat maxBounds, Mat& neighborsIdx, Mat& neighbors = Mat(), Mat& labels = Mat())
    private static native void n_findOrthoRange(long nativeObj, long minBounds_nativeObj, long maxBounds_nativeObj, long neighborsIdx_nativeObj, long neighbors_nativeObj, long labels_nativeObj);
    private static native void n_findOrthoRange(long nativeObj, long minBounds_nativeObj, long maxBounds_nativeObj, long neighborsIdx_nativeObj, long neighbors_nativeObj);
    private static native void n_findOrthoRange(long nativeObj, long minBounds_nativeObj, long maxBounds_nativeObj, long neighborsIdx_nativeObj);

    // C++:  void KDTree::getPoints(Mat idx, Mat& pts, Mat& labels = Mat())
    private static native void n_getPoints(long nativeObj, long idx_nativeObj, long pts_nativeObj, long labels_nativeObj);
    private static native void n_getPoints(long nativeObj, long idx_nativeObj, long pts_nativeObj);

    // C++: // Mat points
    private static native long n_get_points(long nativeObj);

    // C++: // int maxDepth
    private static native int n_get_maxDepth(long nativeObj);

    // C++: // int normType
    private static native int n_get_normType(long nativeObj);

    // C++: // int normType
    private static native void n_set_normType(long nativeObj, int normType);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
