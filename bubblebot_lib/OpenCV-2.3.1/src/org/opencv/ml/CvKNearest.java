
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
import org.opencv.core.*;
// C++: class CvKNearest
public class CvKNearest {


    protected final long nativeObj;
    protected CvKNearest(long addr) { nativeObj = addr; }

    //
    // C++:   CvKNearest::CvKNearest()
    //

    /**
     * Default and training constructors.
     *
     * See "CvKNearest.train" for additional parameters descriptions.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-cvknearest">org.opencv.ml.CvKNearest.CvKNearest</a>
     */
    public   CvKNearest()
    {

        nativeObj = n_CvKNearest();

        return;
    }


    //
    // C++:   CvKNearest::CvKNearest(Mat trainData, Mat responses, Mat sampleIdx = cv::Mat(), bool isRegression = false, int max_k = 32)
    //

    /**
     * Default and training constructors.
     *
     * See "CvKNearest.train" for additional parameters descriptions.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression a isRegression
     * @param max_k a max_k
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-cvknearest">org.opencv.ml.CvKNearest.CvKNearest</a>
     */
    public   CvKNearest(Mat trainData, Mat responses, Mat sampleIdx, boolean isRegression, int max_k)
    {

        nativeObj = n_CvKNearest(trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj, isRegression, max_k);

        return;
    }

    /**
     * Default and training constructors.
     *
     * See "CvKNearest.train" for additional parameters descriptions.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression a isRegression
     * @param max_k a max_k
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-cvknearest">org.opencv.ml.CvKNearest.CvKNearest</a>
     */
    public   CvKNearest(Mat trainData, Mat responses, Mat sampleIdx, boolean isRegression)
    {

        nativeObj = n_CvKNearest(trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj, isRegression);

        return;
    }

    /**
     * Default and training constructors.
     *
     * See "CvKNearest.train" for additional parameters descriptions.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression a isRegression
     * @param max_k a max_k
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-cvknearest">org.opencv.ml.CvKNearest.CvKNearest</a>
     */
    public   CvKNearest(Mat trainData, Mat responses, Mat sampleIdx)
    {

        nativeObj = n_CvKNearest(trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj);

        return;
    }

    /**
     * Default and training constructors.
     *
     * See "CvKNearest.train" for additional parameters descriptions.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression a isRegression
     * @param max_k a max_k
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-cvknearest">org.opencv.ml.CvKNearest.CvKNearest</a>
     */
    public   CvKNearest(Mat trainData, Mat responses)
    {

        nativeObj = n_CvKNearest(trainData.nativeObj, responses.nativeObj);

        return;
    }


    //
    // C++:  float CvKNearest::find_nearest(Mat samples, int k, Mat& results, Mat& neighborResponses, Mat& dists)
    //

    /**
     * Finds the neighbors and predicts responses for input vectors.
     *
     * For each input vector (a row of the matrix "samples"), the method finds the
     * "k" nearest neighbors. In case of regression, the predicted result is a mean
     * value of the particular vector's neighbor responses. In case of
     * classification, the class is determined by voting.
     *
     * For each input vector, the neighbors are sorted by their distances to the
     * vector.
     *
     * In case of C++ interface you can use output pointers to empty matrices and
     * the function will allocate memory itself.
     *
     * If only a single input vector is passed, all output matrices are optional and
     * the predicted value is returned by the method.
     *
     * @param samples Input samples stored by rows. It is a single-precision
     * floating-point matrix of number_of_samples x number_of_features size.
     * @param k Number of used nearest neighbors. It must satisfy constraint: k <=
     * "CvKNearest.get_max_k".
     * @param results Vector with results of prediction (regression or
     * classification) for each input sample. It is a single-precision
     * floating-point vector with "number_of_samples" elements.
     * @param neighborResponses Optional output values for corresponding
     * "neighbors". It is a single-precision floating-point matrix of
     * number_of_samples x k size.
     * @param dists a dists
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-find-nearest">org.opencv.ml.CvKNearest.find_nearest</a>
     */
    public  float find_nearest(Mat samples, int k, Mat results, Mat neighborResponses, Mat dists)
    {

        float retVal = n_find_nearest(nativeObj, samples.nativeObj, k, results.nativeObj, neighborResponses.nativeObj, dists.nativeObj);

        return retVal;
    }


    //
    // C++:  bool CvKNearest::train(Mat trainData, Mat responses, Mat sampleIdx = cv::Mat(), bool isRegression = false, int maxK = 32, bool updateBase = false)
    //

    /**
     * Trains the model.
     *
     * The method trains the K-Nearest model. It follows the conventions of the
     * generic "CvStataModel.train" approach with the following limitations:
     *   * Only "CV_ROW_SAMPLE" data layout is supported.
     *   * Input variables are all ordered.
     *   * Output variables can be either categorical ("is_regression=false") or
     * ordered ("is_regression=true").
     *   * Variable subsets ("var_idx") and missing measurements are not supported.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression Type of the problem: "true" for regression and "false"
     * for classification.
     * @param maxK Number of maximum neighbors that may be passed to the method
     * "CvKNearest.find_nearest".
     * @param updateBase Specifies whether the model is trained from scratch
     * ("update_base=false"), or it is updated using the new training data
     * ("update_base=true"). In the latter case, the parameter "maxK" must not be
     * larger than the original value.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-train">org.opencv.ml.CvKNearest.train</a>
     */
    public  boolean train(Mat trainData, Mat responses, Mat sampleIdx, boolean isRegression, int maxK, boolean updateBase)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj, isRegression, maxK, updateBase);

        return retVal;
    }

    /**
     * Trains the model.
     *
     * The method trains the K-Nearest model. It follows the conventions of the
     * generic "CvStataModel.train" approach with the following limitations:
     *   * Only "CV_ROW_SAMPLE" data layout is supported.
     *   * Input variables are all ordered.
     *   * Output variables can be either categorical ("is_regression=false") or
     * ordered ("is_regression=true").
     *   * Variable subsets ("var_idx") and missing measurements are not supported.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression Type of the problem: "true" for regression and "false"
     * for classification.
     * @param maxK Number of maximum neighbors that may be passed to the method
     * "CvKNearest.find_nearest".
     * @param updateBase Specifies whether the model is trained from scratch
     * ("update_base=false"), or it is updated using the new training data
     * ("update_base=true"). In the latter case, the parameter "maxK" must not be
     * larger than the original value.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-train">org.opencv.ml.CvKNearest.train</a>
     */
    public  boolean train(Mat trainData, Mat responses, Mat sampleIdx, boolean isRegression, int maxK)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj, isRegression, maxK);

        return retVal;
    }

    /**
     * Trains the model.
     *
     * The method trains the K-Nearest model. It follows the conventions of the
     * generic "CvStataModel.train" approach with the following limitations:
     *   * Only "CV_ROW_SAMPLE" data layout is supported.
     *   * Input variables are all ordered.
     *   * Output variables can be either categorical ("is_regression=false") or
     * ordered ("is_regression=true").
     *   * Variable subsets ("var_idx") and missing measurements are not supported.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression Type of the problem: "true" for regression and "false"
     * for classification.
     * @param maxK Number of maximum neighbors that may be passed to the method
     * "CvKNearest.find_nearest".
     * @param updateBase Specifies whether the model is trained from scratch
     * ("update_base=false"), or it is updated using the new training data
     * ("update_base=true"). In the latter case, the parameter "maxK" must not be
     * larger than the original value.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-train">org.opencv.ml.CvKNearest.train</a>
     */
    public  boolean train(Mat trainData, Mat responses, Mat sampleIdx, boolean isRegression)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj, isRegression);

        return retVal;
    }

    /**
     * Trains the model.
     *
     * The method trains the K-Nearest model. It follows the conventions of the
     * generic "CvStataModel.train" approach with the following limitations:
     *   * Only "CV_ROW_SAMPLE" data layout is supported.
     *   * Input variables are all ordered.
     *   * Output variables can be either categorical ("is_regression=false") or
     * ordered ("is_regression=true").
     *   * Variable subsets ("var_idx") and missing measurements are not supported.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression Type of the problem: "true" for regression and "false"
     * for classification.
     * @param maxK Number of maximum neighbors that may be passed to the method
     * "CvKNearest.find_nearest".
     * @param updateBase Specifies whether the model is trained from scratch
     * ("update_base=false"), or it is updated using the new training data
     * ("update_base=true"). In the latter case, the parameter "maxK" must not be
     * larger than the original value.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-train">org.opencv.ml.CvKNearest.train</a>
     */
    public  boolean train(Mat trainData, Mat responses, Mat sampleIdx)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, responses.nativeObj, sampleIdx.nativeObj);

        return retVal;
    }

    /**
     * Trains the model.
     *
     * The method trains the K-Nearest model. It follows the conventions of the
     * generic "CvStataModel.train" approach with the following limitations:
     *   * Only "CV_ROW_SAMPLE" data layout is supported.
     *   * Input variables are all ordered.
     *   * Output variables can be either categorical ("is_regression=false") or
     * ordered ("is_regression=true").
     *   * Variable subsets ("var_idx") and missing measurements are not supported.
     *
     * @param trainData a trainData
     * @param responses a responses
     * @param sampleIdx a sampleIdx
     * @param isRegression Type of the problem: "true" for regression and "false"
     * for classification.
     * @param maxK Number of maximum neighbors that may be passed to the method
     * "CvKNearest.find_nearest".
     * @param updateBase Specifies whether the model is trained from scratch
     * ("update_base=false"), or it is updated using the new training data
     * ("update_base=true"). In the latter case, the parameter "maxK" must not be
     * larger than the original value.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/k_nearest_neighbors.html#cvknearest-train">org.opencv.ml.CvKNearest.train</a>
     */
    public  boolean train(Mat trainData, Mat responses)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, responses.nativeObj);

        return retVal;
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

    // C++:   CvKNearest::CvKNearest()
    private static native long n_CvKNearest();

    // C++:   CvKNearest::CvKNearest(Mat trainData, Mat responses, Mat sampleIdx = cv::Mat(), bool isRegression = false, int max_k = 32)
    private static native long n_CvKNearest(long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj, boolean isRegression, int max_k);
    private static native long n_CvKNearest(long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj, boolean isRegression);
    private static native long n_CvKNearest(long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj);
    private static native long n_CvKNearest(long trainData_nativeObj, long responses_nativeObj);

    // C++:  float CvKNearest::find_nearest(Mat samples, int k, Mat& results, Mat& neighborResponses, Mat& dists)
    private static native float n_find_nearest(long nativeObj, long samples_nativeObj, int k, long results_nativeObj, long neighborResponses_nativeObj, long dists_nativeObj);

    // C++:  bool CvKNearest::train(Mat trainData, Mat responses, Mat sampleIdx = cv::Mat(), bool isRegression = false, int maxK = 32, bool updateBase = false)
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj, boolean isRegression, int maxK, boolean updateBase);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj, boolean isRegression, int maxK);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj, boolean isRegression);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, long responses_nativeObj, long sampleIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, long responses_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
