
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
// C++: class CvStatModel
public class CvStatModel {


    protected final long nativeObj;
    protected CvStatModel(long addr) { nativeObj = addr; }

    //
    // C++:  void CvStatModel::load(c_string filename, c_string name = 0)
    //

    /**
     * Loads the model from a file.
     *
     * The method "load" loads the complete model state with the specified name (or
     * default model-dependent name) from the specified XML or YAML file. The
     * previous model state is cleared by "CvStatModel.clear".
     *
     * @param filename a filename
     * @param name a name
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/statistical_models.html#cvstatmodel-load">org.opencv.ml.CvStatModel.load</a>
     */
    public  void load(java.lang.String filename, java.lang.String name)
    {

        n_load(nativeObj, filename, name);

        return;
    }

    /**
     * Loads the model from a file.
     *
     * The method "load" loads the complete model state with the specified name (or
     * default model-dependent name) from the specified XML or YAML file. The
     * previous model state is cleared by "CvStatModel.clear".
     *
     * @param filename a filename
     * @param name a name
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/statistical_models.html#cvstatmodel-load">org.opencv.ml.CvStatModel.load</a>
     */
    public  void load(java.lang.String filename)
    {

        n_load(nativeObj, filename);

        return;
    }


    //
    // C++:  void CvStatModel::save(c_string filename, c_string name = 0)
    //

    /**
     * Saves the model to a file.
     *
     * The method "save" saves the complete model state to the specified XML or YAML
     * file with the specified name or default name (which depends on a particular
     * class). *Data persistence* functionality from "CxCore" is used.
     *
     * @param filename a filename
     * @param name a name
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/statistical_models.html#cvstatmodel-save">org.opencv.ml.CvStatModel.save</a>
     */
    public  void save(java.lang.String filename, java.lang.String name)
    {

        n_save(nativeObj, filename, name);

        return;
    }

    /**
     * Saves the model to a file.
     *
     * The method "save" saves the complete model state to the specified XML or YAML
     * file with the specified name or default name (which depends on a particular
     * class). *Data persistence* functionality from "CxCore" is used.
     *
     * @param filename a filename
     * @param name a name
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/statistical_models.html#cvstatmodel-save">org.opencv.ml.CvStatModel.save</a>
     */
    public  void save(java.lang.String filename)
    {

        n_save(nativeObj, filename);

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

    // C++:  void CvStatModel::load(c_string filename, c_string name = 0)
    private static native void n_load(long nativeObj, java.lang.String filename, java.lang.String name);
    private static native void n_load(long nativeObj, java.lang.String filename);

    // C++:  void CvStatModel::save(c_string filename, c_string name = 0)
    private static native void n_save(long nativeObj, java.lang.String filename, java.lang.String name);
    private static native void n_save(long nativeObj, java.lang.String filename);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
