
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.features2d;
import org.opencv.core.*;
// C++: class KeyPoint
public class KeyPoint {


    protected final long nativeObj;
    protected KeyPoint(long addr) { nativeObj = addr; }

    //
    // C++:   KeyPoint::KeyPoint()
    //

    /**
     * The keypoint constructors
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint()
    {

        nativeObj = n_KeyPoint();

        return;
    }


    //
    // C++:   KeyPoint::KeyPoint(float x, float y, float _size, float _angle = -1, float _response = 0, int _octave = 0, int _class_id = -1)
    //

    /**
     * The keypoint constructors
     *
     * @param x x-coordinate of the keypoint
     * @param y y-coordinate of the keypoint
     * @param _size keypoint diameter
     * @param _angle keypoint orientation
     * @param _response keypoint detector response on the keypoint (that is,
     * strength of the keypoint)
     * @param _octave pyramid octave in which the keypoint has been detected
     * @param _class_id object id
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave, int _class_id)
    {

        nativeObj = n_KeyPoint(x, y, _size, _angle, _response, _octave, _class_id);

        return;
    }

    /**
     * The keypoint constructors
     *
     * @param x x-coordinate of the keypoint
     * @param y y-coordinate of the keypoint
     * @param _size keypoint diameter
     * @param _angle keypoint orientation
     * @param _response keypoint detector response on the keypoint (that is,
     * strength of the keypoint)
     * @param _octave pyramid octave in which the keypoint has been detected
     * @param _class_id object id
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave)
    {

        nativeObj = n_KeyPoint(x, y, _size, _angle, _response, _octave);

        return;
    }

    /**
     * The keypoint constructors
     *
     * @param x x-coordinate of the keypoint
     * @param y y-coordinate of the keypoint
     * @param _size keypoint diameter
     * @param _angle keypoint orientation
     * @param _response keypoint detector response on the keypoint (that is,
     * strength of the keypoint)
     * @param _octave pyramid octave in which the keypoint has been detected
     * @param _class_id object id
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint(float x, float y, float _size, float _angle, float _response)
    {

        nativeObj = n_KeyPoint(x, y, _size, _angle, _response);

        return;
    }

    /**
     * The keypoint constructors
     *
     * @param x x-coordinate of the keypoint
     * @param y y-coordinate of the keypoint
     * @param _size keypoint diameter
     * @param _angle keypoint orientation
     * @param _response keypoint detector response on the keypoint (that is,
     * strength of the keypoint)
     * @param _octave pyramid octave in which the keypoint has been detected
     * @param _class_id object id
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint(float x, float y, float _size, float _angle)
    {

        nativeObj = n_KeyPoint(x, y, _size, _angle);

        return;
    }

    /**
     * The keypoint constructors
     *
     * @param x x-coordinate of the keypoint
     * @param y y-coordinate of the keypoint
     * @param _size keypoint diameter
     * @param _angle keypoint orientation
     * @param _response keypoint detector response on the keypoint (that is,
     * strength of the keypoint)
     * @param _octave pyramid octave in which the keypoint has been detected
     * @param _class_id object id
     *
     * @see <a href="http://opencv.itseez.com/modules/features2d/doc/common_interfaces_of_feature_detectors.html#keypoint-keypoint">org.opencv.features2d.KeyPoint.KeyPoint</a>
     */
    public   KeyPoint(float x, float y, float _size)
    {

        nativeObj = n_KeyPoint(x, y, _size);

        return;
    }


    //
    // C++: // Point2f pt
    //

    public  Point get_pt()
    {

        Point retVal = new Point(n_get_pt(nativeObj));

        return retVal;
    }


    //
    // C++: // Point2f pt
    //

    public  void set_pt(Point pt)
    {

        n_set_pt(nativeObj, pt.x, pt.y);

        return;
    }


    //
    // C++: // float size
    //

    public  float get_size()
    {

        float retVal = n_get_size(nativeObj);

        return retVal;
    }


    //
    // C++: // float size
    //

    public  void set_size(float size)
    {

        n_set_size(nativeObj, size);

        return;
    }


    //
    // C++: // float angle
    //

    public  float get_angle()
    {

        float retVal = n_get_angle(nativeObj);

        return retVal;
    }


    //
    // C++: // float angle
    //

    public  void set_angle(float angle)
    {

        n_set_angle(nativeObj, angle);

        return;
    }


    //
    // C++: // float response
    //

    public  float get_response()
    {

        float retVal = n_get_response(nativeObj);

        return retVal;
    }


    //
    // C++: // float response
    //

    public  void set_response(float response)
    {

        n_set_response(nativeObj, response);

        return;
    }


    //
    // C++: // int octave
    //

    public  int get_octave()
    {

        int retVal = n_get_octave(nativeObj);

        return retVal;
    }


    //
    // C++: // int octave
    //

    public  void set_octave(int octave)
    {

        n_set_octave(nativeObj, octave);

        return;
    }


    //
    // C++: // int class_id
    //

    public  int get_class_id()
    {

        int retVal = n_get_class_id(nativeObj);

        return retVal;
    }


    //
    // C++: // int class_id
    //

    public  void set_class_id(int class_id)
    {

        n_set_class_id(nativeObj, class_id);

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

    // C++:   KeyPoint::KeyPoint()
    private static native long n_KeyPoint();

    // C++:   KeyPoint::KeyPoint(float x, float y, float _size, float _angle = -1, float _response = 0, int _octave = 0, int _class_id = -1)
    private static native long n_KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave, int _class_id);
    private static native long n_KeyPoint(float x, float y, float _size, float _angle, float _response, int _octave);
    private static native long n_KeyPoint(float x, float y, float _size, float _angle, float _response);
    private static native long n_KeyPoint(float x, float y, float _size, float _angle);
    private static native long n_KeyPoint(float x, float y, float _size);

    // C++: // Point2f pt
    private static native double[] n_get_pt(long nativeObj);

    // C++: // Point2f pt
    private static native void n_set_pt(long nativeObj, double pt_x, double pt_y);

    // C++: // float size
    private static native float n_get_size(long nativeObj);

    // C++: // float size
    private static native void n_set_size(long nativeObj, float size);

    // C++: // float angle
    private static native float n_get_angle(long nativeObj);

    // C++: // float angle
    private static native void n_set_angle(long nativeObj, float angle);

    // C++: // float response
    private static native float n_get_response(long nativeObj);

    // C++: // float response
    private static native void n_set_response(long nativeObj, float response);

    // C++: // int octave
    private static native int n_get_octave(long nativeObj);

    // C++: // int octave
    private static native void n_set_octave(long nativeObj, int octave);

    // C++: // int class_id
    private static native int n_get_class_id(long nativeObj);

    // C++: // int class_id
    private static native void n_set_class_id(long nativeObj, int class_id);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
