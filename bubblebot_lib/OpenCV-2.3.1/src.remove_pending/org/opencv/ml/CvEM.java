
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
import org.opencv.core.*;
import org.opencv.utils;
// C++: class CvEM
public class CvEM {


    protected final long nativeObj;
    protected CvEM(long addr) { nativeObj = addr; }

    public static final int
            COV_MAT_SPHERICAL = 0,
            COV_MAT_DIAGONAL = 1,
            COV_MAT_GENERIC = 2,
            START_E_STEP = 1,
            START_M_STEP = 2,
            START_AUTO_STEP = 0;


    //
    // C++:   CvEM::CvEM()
    //

    public   CvEM()
    {

        nativeObj = n_CvEM();

        return;
    }


    //
    // C++:   CvEM::CvEM(Mat samples, Mat sampleIdx = cv::Mat(), CvEMParams params = CvEMParams())
    //

    public   CvEM(Mat samples, Mat sampleIdx, CvEMParams params)
    {

        nativeObj = n_CvEM(samples.nativeObj, sampleIdx.nativeObj, params.nativeObj);

        return;
    }

    public   CvEM(Mat samples, Mat sampleIdx)
    {

        nativeObj = n_CvEM(samples.nativeObj, sampleIdx.nativeObj);

        return;
    }

    public   CvEM(Mat samples)
    {

        nativeObj = n_CvEM(samples.nativeObj);

        return;
    }


    //
    // C++:  double CvEM::calcLikelihood(Mat sample)
    //

    public  double calcLikelihood(Mat sample)
    {

        double retVal = n_calcLikelihood(nativeObj, sample.nativeObj);

        return retVal;
    }


    //
    // C++:  void CvEM::clear()
    //

    public  void clear()
    {

        n_clear(nativeObj);

        return;
    }


    //
    // C++:  void CvEM::getCovs(vector_Mat& covs)
    //

    /**
     * Returns mixture covariance matrices S_k.
     *
     * @param covs a covs
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getcovs">org.opencv.ml.CvEM.getCovs</a>
     */
    public  void getCovs(java.util.List<Mat> covs)
    {
        Mat covs_mat = new Mat();
        n_getCovs(nativeObj, covs_mat.nativeObj);
        utils.Mat_to_vector_Mat(covs_mat, covs);
        return;
    }


    //
    // C++:  double CvEM::getLikelihood()
    //

    /**
     * Returns logarithm of likelihood.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getlikelihood">org.opencv.ml.CvEM.getLikelihood</a>
     */
    public  double getLikelihood()
    {

        double retVal = n_getLikelihood(nativeObj);

        return retVal;
    }


    //
    // C++:  double CvEM::getLikelihoodDelta()
    //

    /**
     * Returns difference between logarithm of likelihood on the last iteration and
     * logarithm of likelihood on the previous iteration.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getlikelihooddelta">org.opencv.ml.CvEM.getLikelihoodDelta</a>
     */
    public  double getLikelihoodDelta()
    {

        double retVal = n_getLikelihoodDelta(nativeObj);

        return retVal;
    }


    //
    // C++:  Mat CvEM::getMeans()
    //

    /**
     * Returns mixture means a_k.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getmeans">org.opencv.ml.CvEM.getMeans</a>
     */
    public  Mat getMeans()
    {

        Mat retVal = new Mat(n_getMeans(nativeObj));

        return retVal;
    }


    //
    // C++:  int CvEM::getNClusters()
    //

    /**
     * Returns the number of mixture components M in the gaussian mixture model.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getnclusters">org.opencv.ml.CvEM.getNClusters</a>
     */
    public  int getNClusters()
    {

        int retVal = n_getNClusters(nativeObj);

        return retVal;
    }


    //
    // C++:  Mat CvEM::getProbs()
    //

    /**
     * Returns vectors of probabilities for each training sample.
     *
     * For each training sample i (that have been passed to the constructor or to
     * "CvEM.train") returns probabilites p_(i,k) to belong to a mixture component
     * k.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getprobs">org.opencv.ml.CvEM.getProbs</a>
     */
    public  Mat getProbs()
    {

        Mat retVal = new Mat(n_getProbs(nativeObj));

        return retVal;
    }


    //
    // C++:  Mat CvEM::getWeights()
    //

    /**
     * Returns mixture weights pi_k.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-getweights">org.opencv.ml.CvEM.getWeights</a>
     */
    public  Mat getWeights()
    {

        Mat retVal = new Mat(n_getWeights(nativeObj));

        return retVal;
    }


    //
    // C++:  float CvEM::predict(Mat sample, Mat* probs = 0)
    //

    /**
     * Returns a mixture component index of a sample.
     *
     * @param sample A sample for classification.
     * @param probs If it is not null then the method will write posterior
     * probabilities of each component given the sample data to this parameter.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-predict">org.opencv.ml.CvEM.predict</a>
     */
    public  float predict(Mat sample, Mat probs)
    {

        float retVal = n_predict(nativeObj, sample.nativeObj, probs.nativeObj);

        return retVal;
    }

    /**
     * Returns a mixture component index of a sample.
     *
     * @param sample A sample for classification.
     * @param probs If it is not null then the method will write posterior
     * probabilities of each component given the sample data to this parameter.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-predict">org.opencv.ml.CvEM.predict</a>
     */
    public  float predict(Mat sample)
    {

        float retVal = n_predict(nativeObj, sample.nativeObj);

        return retVal;
    }


    //
    // C++:  bool CvEM::train(Mat samples, Mat sampleIdx = cv::Mat(), CvEMParams params = CvEMParams(), Mat* labels = 0)
    //

    /**
     * Estimates the Gaussian mixture parameters from a sample set.
     *
     * Unlike many of the ML models, EM is an unsupervised learning algorithm and it
     * does not take responses (class labels or function values) as input. Instead,
     * it computes the *Maximum Likelihood Estimate* of the Gaussian mixture
     * parameters from an input sample set, stores all the parameters inside the
     * structure: p_(i,k) in "probs", a_k in "means", S_k in "covs[k]", pi_k in
     * "weights", and optionally computes the output "class label" for each sample:
     * labels_i=arg max_k(p_(i,k)), i=1..N (indices of the most probable mixture
     * component for each sample).
     *
     * The trained model can be used further for prediction, just like any other
     * classifier. The trained model is similar to the "CvBayesClassifier".
     *
     * For an example of clustering random samples of the multi-Gaussian
     * distribution using EM, see "em.cpp" sample in the OpenCV distribution.
     *
     * @param samples Samples from which the Gaussian mixture model will be
     * estimated.
     * @param sampleIdx a sampleIdx
     * @param params Parameters of the EM algorithm.
     * @param labels The optional output "class label" for each sample: labels_i=arg
     * max_k(p_(i,k)), i=1..N (indices of the most probable mixture component for
     * each sample).
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-train">org.opencv.ml.CvEM.train</a>
     */
    public  boolean train(Mat samples, Mat sampleIdx, CvEMParams params, Mat labels)
    {

        boolean retVal = n_train(nativeObj, samples.nativeObj, sampleIdx.nativeObj, params.nativeObj, labels.nativeObj);

        return retVal;
    }

    /**
     * Estimates the Gaussian mixture parameters from a sample set.
     *
     * Unlike many of the ML models, EM is an unsupervised learning algorithm and it
     * does not take responses (class labels or function values) as input. Instead,
     * it computes the *Maximum Likelihood Estimate* of the Gaussian mixture
     * parameters from an input sample set, stores all the parameters inside the
     * structure: p_(i,k) in "probs", a_k in "means", S_k in "covs[k]", pi_k in
     * "weights", and optionally computes the output "class label" for each sample:
     * labels_i=arg max_k(p_(i,k)), i=1..N (indices of the most probable mixture
     * component for each sample).
     *
     * The trained model can be used further for prediction, just like any other
     * classifier. The trained model is similar to the "CvBayesClassifier".
     *
     * For an example of clustering random samples of the multi-Gaussian
     * distribution using EM, see "em.cpp" sample in the OpenCV distribution.
     *
     * @param samples Samples from which the Gaussian mixture model will be
     * estimated.
     * @param sampleIdx a sampleIdx
     * @param params Parameters of the EM algorithm.
     * @param labels The optional output "class label" for each sample: labels_i=arg
     * max_k(p_(i,k)), i=1..N (indices of the most probable mixture component for
     * each sample).
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-train">org.opencv.ml.CvEM.train</a>
     */
    public  boolean train(Mat samples, Mat sampleIdx, CvEMParams params)
    {

        boolean retVal = n_train(nativeObj, samples.nativeObj, sampleIdx.nativeObj, params.nativeObj);

        return retVal;
    }

    /**
     * Estimates the Gaussian mixture parameters from a sample set.
     *
     * Unlike many of the ML models, EM is an unsupervised learning algorithm and it
     * does not take responses (class labels or function values) as input. Instead,
     * it computes the *Maximum Likelihood Estimate* of the Gaussian mixture
     * parameters from an input sample set, stores all the parameters inside the
     * structure: p_(i,k) in "probs", a_k in "means", S_k in "covs[k]", pi_k in
     * "weights", and optionally computes the output "class label" for each sample:
     * labels_i=arg max_k(p_(i,k)), i=1..N (indices of the most probable mixture
     * component for each sample).
     *
     * The trained model can be used further for prediction, just like any other
     * classifier. The trained model is similar to the "CvBayesClassifier".
     *
     * For an example of clustering random samples of the multi-Gaussian
     * distribution using EM, see "em.cpp" sample in the OpenCV distribution.
     *
     * @param samples Samples from which the Gaussian mixture model will be
     * estimated.
     * @param sampleIdx a sampleIdx
     * @param params Parameters of the EM algorithm.
     * @param labels The optional output "class label" for each sample: labels_i=arg
     * max_k(p_(i,k)), i=1..N (indices of the most probable mixture component for
     * each sample).
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-train">org.opencv.ml.CvEM.train</a>
     */
    public  boolean train(Mat samples, Mat sampleIdx)
    {

        boolean retVal = n_train(nativeObj, samples.nativeObj, sampleIdx.nativeObj);

        return retVal;
    }

    /**
     * Estimates the Gaussian mixture parameters from a sample set.
     *
     * Unlike many of the ML models, EM is an unsupervised learning algorithm and it
     * does not take responses (class labels or function values) as input. Instead,
     * it computes the *Maximum Likelihood Estimate* of the Gaussian mixture
     * parameters from an input sample set, stores all the parameters inside the
     * structure: p_(i,k) in "probs", a_k in "means", S_k in "covs[k]", pi_k in
     * "weights", and optionally computes the output "class label" for each sample:
     * labels_i=arg max_k(p_(i,k)), i=1..N (indices of the most probable mixture
     * component for each sample).
     *
     * The trained model can be used further for prediction, just like any other
     * classifier. The trained model is similar to the "CvBayesClassifier".
     *
     * For an example of clustering random samples of the multi-Gaussian
     * distribution using EM, see "em.cpp" sample in the OpenCV distribution.
     *
     * @param samples Samples from which the Gaussian mixture model will be
     * estimated.
     * @param sampleIdx a sampleIdx
     * @param params Parameters of the EM algorithm.
     * @param labels The optional output "class label" for each sample: labels_i=arg
     * max_k(p_(i,k)), i=1..N (indices of the most probable mixture component for
     * each sample).
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/expectation_maximization.html#cvem-train">org.opencv.ml.CvEM.train</a>
     */
    public  boolean train(Mat samples)
    {

        boolean retVal = n_train(nativeObj, samples.nativeObj);

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

    // C++:   CvEM::CvEM()
    private static native long n_CvEM();

    // C++:   CvEM::CvEM(Mat samples, Mat sampleIdx = cv::Mat(), CvEMParams params = CvEMParams())
    private static native long n_CvEM(long samples_nativeObj, long sampleIdx_nativeObj, long params_nativeObj);
    private static native long n_CvEM(long samples_nativeObj, long sampleIdx_nativeObj);
    private static native long n_CvEM(long samples_nativeObj);

    // C++:  double CvEM::calcLikelihood(Mat sample)
    private static native double n_calcLikelihood(long nativeObj, long sample_nativeObj);

    // C++:  void CvEM::clear()
    private static native void n_clear(long nativeObj);

    // C++:  void CvEM::getCovs(vector_Mat& covs)
    private static native void n_getCovs(long nativeObj, long covs_mat_nativeObj);

    // C++:  double CvEM::getLikelihood()
    private static native double n_getLikelihood(long nativeObj);

    // C++:  double CvEM::getLikelihoodDelta()
    private static native double n_getLikelihoodDelta(long nativeObj);

    // C++:  Mat CvEM::getMeans()
    private static native long n_getMeans(long nativeObj);

    // C++:  int CvEM::getNClusters()
    private static native int n_getNClusters(long nativeObj);

    // C++:  Mat CvEM::getProbs()
    private static native long n_getProbs(long nativeObj);

    // C++:  Mat CvEM::getWeights()
    private static native long n_getWeights(long nativeObj);

    // C++:  float CvEM::predict(Mat sample, Mat* probs = 0)
    private static native float n_predict(long nativeObj, long sample_nativeObj, long probs_nativeObj);
    private static native float n_predict(long nativeObj, long sample_nativeObj);

    // C++:  bool CvEM::train(Mat samples, Mat sampleIdx = cv::Mat(), CvEMParams params = CvEMParams(), Mat* labels = 0)
    private static native boolean n_train(long nativeObj, long samples_nativeObj, long sampleIdx_nativeObj, long params_nativeObj, long labels_nativeObj);
    private static native boolean n_train(long nativeObj, long samples_nativeObj, long sampleIdx_nativeObj, long params_nativeObj);
    private static native boolean n_train(long nativeObj, long samples_nativeObj, long sampleIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long samples_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
