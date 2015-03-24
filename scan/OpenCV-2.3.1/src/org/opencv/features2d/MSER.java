
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;

// C++: class MSER
public class MSER {


    protected final long nativeObj;
    protected MSER(long addr) { nativeObj = addr; }

    //
    // C++:   MSER::MSER()
    //

    public   MSER()
    {

        nativeObj = n_MSER();

        return;
    }


    //
    // C++:   MSER::MSER(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size)
    //

    public   MSER(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size)
    {

        nativeObj = n_MSER(_delta, _min_area, _max_area, _max_variation, _min_diversity, _max_evolution, _area_threshold, _min_margin, _edge_blur_size);

        return;
    }


    //
    // C++:  void MSER::operator()(Mat image, vector_vector_Point& msers, Mat mask)
    //

    // Unknown type 'vector_vector_Point' (O), skipping the function


    @Override
    protected void finalize() throws Throwable {
        n_delete(nativeObj);
        super.finalize();
    }



    //
    // native stuff
    //
    static { System.loadLibrary("opencv_java"); }

    // C++:   MSER::MSER()
    private static native long n_MSER();

    // C++:   MSER::MSER(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size)
    private static native long n_MSER(int _delta, int _min_area, int _max_area, double _max_variation, double _min_diversity, int _max_evolution, double _area_threshold, double _min_margin, int _edge_blur_size);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
