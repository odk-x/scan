
//
// This file is auto-generated. Please don't modify it!
//
package org.opencv.ml;
import org.opencv.core.*;
// C++: class CvERTrees
public class CvERTrees {


    protected final long nativeObj;
    protected CvERTrees(long addr) { nativeObj = addr; }

    //
    // C++:   CvERTrees::CvERTrees()
    //

    public   CvERTrees()
    {

        nativeObj = n_CvERTrees();

        return;
    }


    //
    // C++:  bool CvERTrees::train(Mat trainData, int tflag, Mat responses, Mat varIdx = cv::Mat(), Mat sampleIdx = cv::Mat(), Mat varType = cv::Mat(), Mat missingDataMask = cv::Mat(), CvRTParams params = CvRTParams())
    //

    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType, Mat missingDataMask, CvRTParams params)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj, missingDataMask.nativeObj, params.nativeObj);

        return retVal;
    }

    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType, Mat missingDataMask)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj, missingDataMask.nativeObj);

        return retVal;
    }

    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx, Mat varType)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj, varType.nativeObj);

        return retVal;
    }

    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx, Mat sampleIdx)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj, sampleIdx.nativeObj);

        return retVal;
    }

    public  boolean train(Mat trainData, int tflag, Mat responses, Mat varIdx)
    {

        boolean retVal = n_train(nativeObj, trainData.nativeObj, tflag, responses.nativeObj, varIdx.nativeObj);

        return retVal;
    }

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

    // C++:   CvERTrees::CvERTrees()
    private static native long n_CvERTrees();

    // C++:  bool CvERTrees::train(Mat trainData, int tflag, Mat responses, Mat varIdx = cv::Mat(), Mat sampleIdx = cv::Mat(), Mat varType = cv::Mat(), Mat missingDataMask = cv::Mat(), CvRTParams params = CvRTParams())
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj, long missingDataMask_nativeObj, long params_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj, long missingDataMask_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj, long varType_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj, long sampleIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj, long varIdx_nativeObj);
    private static native boolean n_train(long nativeObj, long trainData_nativeObj, int tflag, long responses_nativeObj);

    // native support for java finalize()
    private static native void n_delete(long nativeObj);

}
