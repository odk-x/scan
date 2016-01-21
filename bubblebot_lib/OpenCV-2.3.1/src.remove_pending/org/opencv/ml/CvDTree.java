
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
import org.opencv.core.*;
// C++: class CvDTree
public class CvDTree {


    protected final long nativeObj;
    protected CvDTree(long addr) { nativeObj = addr; }

    //
    // C++:   CvDTree::CvDTree()
    //

    public   CvDTree()
    {

        nativeObj = n_CvDTree();

        return;
    }


    //
    // C++:  void CvDTree::clear()
    //

    public  void clear()
    {

        n_clear(nativeObj);

        return;
    }


    //
    // C++:  Mat CvDTree::getVarImportance()
    //

    /**
     * Returns the variable importance array.
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-getvarimportance">org.opencv.ml.CvDTree.getVarImportance</a>
     */
    public  Mat getVarImportance()
    {

        Mat retVal = new Mat(n_getVarImportance(nativeObj));

        return retVal;
    }


    //
    // C++:  CvDTreeNode* CvDTree::predict(Mat sample, Mat missingDataMask = cv::Mat(), bool preprocessedInput = false)
    //

    // Return type 'CvDTreeNode*' is not supported, skipping the function


    //
    // C++:  bool CvDTree::train(Mat trainData, int tflag, Mat responses, Mat varIdx = cv::Mat(), Mat sampleIdx = cv::Mat(), Mat varType = cv::Mat(), Mat missingDataMask = cv::Mat(), CvDTreeParams params = CvDTreeParams())
    //

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType, Mat missingDataMask, CvDTreeParams params)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj, missingDataMask.nativeObj, params.nativeObj);

        return retVal;
    }

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType, Mat missingDataMask)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj, missingDataMask.nativeObj);

        return retVal;
    }

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj);

        return retVal;
    }

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj);

        return retVal;
    }

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj);

        return retVal;
    }

    /**
     * Trains a decision tree.
     *
     * There are four "train" methods in "CvDTree":
     *   * The first two methods follow the generic "CvStatModel.train"
     * conventions. It is the most complete form. Both data layouts
     * ("tflag=CV_ROW_SAMPLE" and "tflag=CV_COL_SAMPLE") are supported, as well as
     * sample and variable subsets, missing measurements, arbitrary combinations of
     * input and output variable types, and so on. The last parameter contains all
     * of the necessary training parameters (see the "CvDTreeParams" description).
     *   * The third method uses "CvMLData" to pass training data to a decision
     * tree.
     *   * The last method "train" is mostly used for building tree ensembles. It
     * takes the pre-constructed "CvDTreeTrainData" instance and an optional subset
     * of the training set. The indices in "subsampleIdx" are counted relatively to
     * the "_sample_idx", passed to the "CvDTreeTrainData" constructor. For example,
     * if "_sample_idx=[1, 5, 7, 100]", then "subsampleIdx=[0,3]" means that the
     * samples "[1, 100]" of the original training set are used.
     *
     * @param trainData a trainData
     * @param tflag a tflag
     * @param responses a responses
     * @param varIdx a varIdx
     * @param sampleIdx a sampleIdx
     * @param varType a varType
     * @param missingDataMask a missingDataMask
     * @param params a params
     *
     * @see <a href="http://opencv.itseez.com/modules/ml/doc/decision_trees.html#cvdtree-train">org.opencv.ml.CvDTree.train</a>
     */
    public  boolean train(Mat trainData, int tflag, Mat responses)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj);

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

    // C++:   CvDTree::CvDTree()
    private static native long n_CvDTree();

    // C++:  void CvDTree::clear()
    private static native void n_clear(long nativeObj);

    // C++:  Mat CvDTree::getVarImportance()
    private static native long n_getVarImportance(long nativeObj);

    // C++:  bool CvDTree::train(Mat trainData, int tflag, Mat responses, Mat varIdx = cv::Mat(), Mat sampleIdx = cv::Mat(), Mat varType = cv::Mat(), Mat missingDataMask = cv::Mat(), CvDTreeParams params = CvDTreeParams())
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj, long missingDataMask_nativeObj, long params_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj, long missingDataMask_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
