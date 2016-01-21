package org.opencv.core;

/**
 * OpenCV C++ n-dimensional dense array class
 *
 * The class "Mat" represents an n-dimensional dense numerical single-channel or
 * multi-channel array. It can be used to store real or complex-valued vectors
 * and matrices, grayscale or color images, voxel volumes, vector fields, point
 * clouds, tensors, histograms (though, very high-dimensional histograms may be
 * better stored in a "SparseMat"). The data layout of the array M is defined by
 * the array "M.step[]", so that the address of element (i_0,...,i_(M.dims-1)),
 * where 0 <= i_k<M.size[k], is computed as:
 *
 * addr(M_(i_0,...,i_(M.dims-1))) = M.data + M.step[0]*i_0 + M.step[1]*i_1 +...
 * + M.step[M.dims-1]*i_(M.dims-1)
 *
 * In case of a 2-dimensional array, the above formula is reduced to:
 *
 * addr(M_(i,j)) = M.data + M.step[0]*i + M.step[1]*j
 *
 * Note that "M.step[i] >= M.step[i+1]" (in fact, "M.step[i] >=
 * M.step[i+1]*M.size[i+1]"). This means that 2-dimensional matrices are stored
 * row-by-row, 3-dimensional matrices are stored plane-by-plane, and so on.
 * "M.step[M.dims-1]" is minimal and always equal to the element size
 * "M.elemSize()".
 *
 * So, the data layout in "Mat" is fully compatible with "CvMat", "IplImage",
 * and "CvMatND" types from OpenCV 1.x. It is also compatible with the majority
 * of dense array types from the standard toolkits and SDKs, such as Numpy
 * (ndarray), Win32 (independent device bitmaps), and others, that is, with any
 * array that uses *steps* (or *strides*) to compute the position of a pixel.
 * Due to this compatibility, it is possible to make a "Mat" header for
 * user-allocated data and process it in-place using OpenCV functions.
 *
 * There are many different ways to create a "Mat" object. The most popular
 * options are listed below:
 *   * Use the "create(nrows, ncols, type)" method or the similar "Mat(nrows,
 * ncols, type[, fillValue])" constructor. A new array of the specified size and
 * type is allocated. "type" has the same meaning as in the "cvCreateMat"
 * method.
 * For example, "CV_8UC1" means a 8-bit single-channel array, "CV_32FC2" means a
 * 2-channel (complex) floating-point array, and so on.
 *
 * As noted in the introduction to this chapter, "create()" allocates only a new
 * array when the shape or type of the current array are different from the
 * specified ones.
 *   * Create a multi-dimensional array:
 *
 * It passes the number of dimensions =1 to the "Mat" constructor but the
 * created array will be 2-dimensional with the number of columns set to 1. So,
 * "Mat.dims" is always >= 2 (can also be 0 when the array is empty).
 *   * Use a copy constructor or assignment operator where there can be an array
 * or expression on the right side (see below). As noted in the introduction,
 * the array assignment is an O(1) operation because it only copies the header
 * and increases the reference counter. The "Mat.clone()" method can be used to
 * get a full (deep) copy of the array when you need it.
 *   * Construct a header for a part of another array. It can be a single row,
 * single column, several rows, several columns, rectangular region in the array
 * (called a *minor* in algebra) or a diagonal. Such operations are also O(1)
 * because the new header references the same data. You can actually modify a
 * part of the array using this feature, for example:
 *
 * Due to the additional "datastart" and "dataend" members, it is possible to
 * compute a relative sub-array position in the main *container* array using
 * "locateROI()":
 *
 * As in case of whole matrices, if you need a deep copy, use the "clone()"
 * method of the extracted sub-matrices.
 *   * Make a header for user-allocated data. It can be useful to do the
 * following:
 *   #. Process "foreign" data using OpenCV (for example, when you implement a
 * DirectShow* filter or a processing module for "gstreamer", and so on). For
 * example:
 *   #. Quickly initialize small matrices and/or get a super-fast element
 * access.
 *
 * Partial yet very common cases of this *user-allocated data* case are
 * conversions from "CvMat" and "IplImage" to "Mat". For this purpose, there are
 * special constructors taking pointers to "CvMat" or "IplImage" and the
 * optional flag indicating whether to copy the data or not.
 *
 * Backward conversion from "Mat" to "CvMat" or "IplImage" is provided via cast
 * operators "Mat.operator CvMat() const" and "Mat.operator IplImage()". The
 * operators do NOT copy the data.
 *   * Use MATLAB-style array initializers, "zeros(), ones(), eye()", for
 * example:
 *   * Use a comma-separated initializer:
 *
 * With this approach, you first call a constructor of the "Mat_" class with the
 * proper parameters, and then you just put "<<" operator followed by
 * comma-separated values that can be constants, variables, expressions, and so
 * on. Also, note the extra parentheses required to avoid compilation errors.
 *
 * Once the array is created, it is automatically managed via a
 * reference-counting mechanism. If the array header is built on top of
 * user-allocated data, you should handle the data by yourself.
 * The array data is deallocated when no one points to it. If you want to
 * release the data pointed by a array header before the array destructor is
 * called, use "Mat.release()".
 *
 * The next important thing to learn about the array class is element access.
 * This manual already described how to compute an address of each array
 * element. Normally, you are not required to use the formula directly in the
 * code. If you know the array element type (which can be retrieved using the
 * method "Mat.type()"), you can access the element M_(ij) of a 2-dimensional
 * array as:
 *
 * assuming that M is a double-precision floating-point array. There are several
 * variants of the method "at" for a different number of dimensions.
 *
 * If you need to process a whole row of a 2D array, the most efficient way is
 * to get the pointer to the row first, and then just use the plain C operator
 * "[]" :
 *
 * Some operations, like the one above, do not actually depend on the array
 * shape. They just process elements of an array one by one (or elements from
 * multiple arrays that have the same coordinates, for example, array addition).
 * Such operations are called *element-wise*. It makes sense to check whether
 * all the input/output arrays are continuous, namely, have no gaps at the end
 * of each row. If yes, process them as a long single row:
 *
 * In case of the continuous matrix, the outer loop body is executed just once.
 * So, the overhead is smaller, which is especially noticeable in case of small
 * matrices.
 *
 * Finally, there are STL-style iterators that are smart enough to skip gaps
 * between successive rows:
 *
 * The matrix iterators are random-access iterators, so they can be passed to
 * any STL algorithm, including "std.sort()".
 *
 * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat">org.opencv.core.Mat</a>
 */
public class Mat {


    public Mat(long nativeMat) {
        /*if(nativeMat == 0)
            throw new java.lang.UnsupportedOperationException("Native object address is NULL");*/
        this.nativeObj = nativeMat;
    }

    /**
     * Various Mat constructors
     *
     * These are various constructors that form a matrix. As noted in the
     * "AutomaticAllocation", often the default constructor is enough, and the
     * proper matrix will be allocated by an OpenCV function. The constructed matrix
     * can further be assigned to another matrix or matrix expression or can be
     * allocated with "Mat.create". In the former case, the old content is
     * de-referenced.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
     */
    public Mat() {
        this( nCreateMat() );
    }

    /**
     * Various Mat constructors
     *
     * These are various constructors that form a matrix. As noted in the
     * "AutomaticAllocation", often the default constructor is enough, and the
     * proper matrix will be allocated by an OpenCV function. The constructed matrix
     * can further be assigned to another matrix or matrix expression or can be
     * allocated with "Mat.create". In the former case, the old content is
     * de-referenced.
     *
     * @param rows Number of rows in a 2D array.
     * @param cols Number of columns in a 2D array.
     * @param type Array type. Use "CV_8UC1,..., CV_64FC4" to create 1-4 channel
     * matrices, or "CV_8UC(n),..., CV_64FC(n)" to create multi-channel (up to
     * "CV_MAX_CN" channels) matrices.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
     */
    public Mat(int rows, int cols, CvType type) {
        this( nCreateMat(rows, cols, type.toInt()) );
    }

    /**
     * Various Mat constructors
     *
     * These are various constructors that form a matrix. As noted in the
     * "AutomaticAllocation", often the default constructor is enough, and the
     * proper matrix will be allocated by an OpenCV function. The constructed matrix
     * can further be assigned to another matrix or matrix expression or can be
     * allocated with "Mat.create". In the former case, the old content is
     * de-referenced.
     *
     * @param rows Number of rows in a 2D array.
     * @param cols Number of columns in a 2D array.
     * @param depth a depth
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
     */
    public Mat(int rows, int cols, int depth) {
        this( rows, cols, new CvType(depth, 1) );
    }

    /**
     * Various Mat constructors
     *
     * These are various constructors that form a matrix. As noted in the
     * "AutomaticAllocation", often the default constructor is enough, and the
     * proper matrix will be allocated by an OpenCV function. The constructed matrix
     * can further be assigned to another matrix or matrix expression or can be
     * allocated with "Mat.create". In the former case, the old content is
     * de-referenced.
     *
     * @param rows Number of rows in a 2D array.
     * @param cols Number of columns in a 2D array.
     * @param type Array type. Use "CV_8UC1,..., CV_64FC4" to create 1-4 channel
     * matrices, or "CV_8UC(n),..., CV_64FC(n)" to create multi-channel (up to
     * "CV_MAX_CN" channels) matrices.
     * @param s An optional value to initialize each matrix element with. To set all
     * the matrix elements to the particular value after the construction, use the
     * assignment operator "Mat.operator=(const Scalar& value)".
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
     */
    public Mat(int rows, int cols, CvType type, Scalar s) {
        this( nCreateMat(rows, cols, type.toInt(), s.val[0], s.val[1], s.val[2], s.val[3]) );
    }

    /**
     * Various Mat constructors
     *
     * These are various constructors that form a matrix. As noted in the
     * "AutomaticAllocation", often the default constructor is enough, and the
     * proper matrix will be allocated by an OpenCV function. The constructed matrix
     * can further be assigned to another matrix or matrix expression or can be
     * allocated with "Mat.create". In the former case, the old content is
     * de-referenced.
     *
     * @param rows Number of rows in a 2D array.
     * @param cols Number of columns in a 2D array.
     * @param depth a depth
     * @param s An optional value to initialize each matrix element with. To set all
     * the matrix elements to the particular value after the construction, use the
     * assignment operator "Mat.operator=(const Scalar& value)".
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-mat">org.opencv.core.Mat.Mat</a>
     */
    public Mat(int rows, int cols, int depth, Scalar s) {
        this( rows, cols, new CvType(depth, 1), s );
    }

    public void dispose() {
        nRelease(nativeObj);
    }

    @Override
    protected void finalize() throws Throwable {
        nDelete(nativeObj);
        super.finalize();
    }

    @Override
    public String toString() {
        if(nativeObj == 0) return  "Mat [ nativeObj=NULL ]";
        return  "Mat [ " +
                rows() + "*" + cols() + "*" + type() +
                ", isCont=" + isContinuous() + ", isSubmat=" + isSubmatrix() +
                ", nativeObj=0x" + Long.toHexString(nativeObj) +
                ", dataAddr=0x" + Long.toHexString(dataAddr()) +
                " ]";
    }

    public String dump() {
        return nDump(nativeObj);
    }

    /**
     * Returns "true" if the array has no elemens.
     *
     * The method returns "true" if "Mat.total()" is 0 or if "Mat.data" is NULL.
     * Because of "pop_back()" and "resize()" methods "M.total() == 0" does not
     * imply that "M.data == NULL".
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-empty">org.opencv.core.Mat.empty</a>
     */
    public boolean empty() {
        if(nativeObj == 0) return true;
        return nIsEmpty(nativeObj);
    }

    /**
     * Returns a matrix size.
     *
     * The method returns a matrix size: "Size(cols, rows)". When the matrix is more
     * than 2-dimensional, the returned size is (-1, -1).
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-size">org.opencv.core.Mat.size</a>
     */
    public Size size() {
        if(nativeObj == 0) return new Size();
        return new Size(nSize(nativeObj));
    }

    private void checkNull() {
        if(nativeObj == 0)
            throw new java.lang.UnsupportedOperationException("Native object address is NULL");
    }

    /**
     * Returns the type of a matrix element.
     *
     * The method returns a matrix element type. This is an identifier compatible
     * with the "CvMat" type system, like "CV_16SC3" or 16-bit signed 3-channel
     * array, and so on.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-type">org.opencv.core.Mat.type</a>
     */
    public CvType type() {
        checkNull();
        return new CvType( nType(nativeObj) );
    }

    /**
     * Returns the depth of a matrix element.
     *
     * The method returns the identifier of the matrix element depth (the type of
     * each individual channel). For example, for a 16-bit signed 3-channel array,
     * the method returns "CV_16S". A complete list of matrix types contains the
     * following values:
     *   * "CV_8U" - 8-bit unsigned integers ("0..255")
     *   * "CV_8S" - 8-bit signed integers ("-128..127")
     *   * "CV_16U" - 16-bit unsigned integers ("0..65535")
     *   * "CV_16S" - 16-bit signed integers ("-32768..32767")
     *   * "CV_32S" - 32-bit signed integers ("-2147483648..2147483647")
     *   * "CV_32F" - 32-bit floating-point numbers ("-FLT_MAX..FLT_MAX, INF, NAN")
     *   * "CV_64F" - 64-bit floating-point numbers ("-DBL_MAX..DBL_MAX, INF, NAN")
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-depth">org.opencv.core.Mat.depth</a>
     */
    public int depth() { return type().depth(); }

    /**
     * Returns the number of matrix channels.
     *
     * The method returns the number of matrix channels.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-channels">org.opencv.core.Mat.channels</a>
     */
    public int channels() { return type().channels(); }

    /**
     * Returns the matrix element size in bytes.
     *
     * The method returns the matrix element size in bytes. For example, if the
     * matrix type is "CV_16SC3", the method returns "3*sizeof(short)" or 6.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-elemsize">org.opencv.core.Mat.elemSize</a>
     */
    public int elemSize() { return type().CV_ELEM_SIZE(); }

    public int rows() {
        if(nativeObj == 0)
            return 0;
        return nRows(nativeObj);
    }

    public int height() { return rows(); }

    public int cols() {
        if(nativeObj == 0)
            return 0;
        return nCols(nativeObj);
    }

    public int width() { return cols(); }

    /**
     * Returns the total number of array elements.
     *
     * The method returns the number of array elements (a number of pixels if the
     * array represents an image).
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-total">org.opencv.core.Mat.total</a>
     */
    public int total() { return rows() * cols(); }

    public long dataAddr() {
        if(nativeObj == 0)
            return 0;
        return nData(nativeObj);
    }

    /**
     * Reports whether the matrix is continuous or not.
     *
     * The method returns "true" if the matrix elements are stored continuously
     * without gaps at the end of each row. Otherwise, it returns "false".
     * Obviously, "1x1" or "1xN" matrices are always continuous. Matrices created
     * with "Mat.create" are always continuous. But if you extract a part of the
     * matrix using "Mat.col", "Mat.diag", and so on, or constructed a matrix
     * header for externally allocated data, such matrices may no longer have this
     * property.
     *
     * The continuity flag is stored as a bit in the "Mat.flags" field and is
     * computed automatically when you construct a matrix header. Thus, the
     * continuity check is a very fast operation, though theoretically it could be
     * done as follows:
     *
     * The method is used in quite a few of OpenCV functions. The point is that
     * element-wise operations (such as arithmetic and logical operations, math
     * functions, alpha blending, color space transformations, and others) do not
     * depend on the image geometry. Thus, if all the input and output arrays are
     * continuous, the functions can process them as very long single-row vectors.
     * The example below illustrates how an alpha-blending function can be
     * implemented.
     *
     * This approach, while being very simple, can boost the performance of a simple
     * element-operation by 10-20 percents, especially if the image is rather small
     * and the operation is quite simple.
     *
     * Another OpenCV idiom in this function, a call of "Mat.create" for the
     * destination array, that allocates the destination array unless it already has
     * the proper size and type. And while the newly allocated arrays are always
     * continuous, you still need to check the destination array because "create"
     * does not always allocate a new matrix.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-iscontinuous">org.opencv.core.Mat.isContinuous</a>
     */
    public boolean isContinuous() {
        if(nativeObj == 0)
            return false; // maybe throw an exception instead?
        return nIsCont(nativeObj);
    }

    public boolean isSubmatrix() {
        if(nativeObj == 0)
            return false; // maybe throw an exception instead?
        return nIsSubmat(nativeObj);
    }

    public Mat submat(int rowStart, int rowEnd, int colStart, int colEnd) {
        checkNull();
        return new Mat( nSubmat(nativeObj, rowStart, rowEnd, colStart, colEnd) );
    }

    /**
     * Creates a matrix header for the specified row span.
     *
     * The method makes a new header for the specified row span of the matrix.
     * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.
     *
     * @param startrow A 0-based start index of the row span.
     * @param endrow A 0-based ending index of the row span.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-rowrange">org.opencv.core.Mat.rowRange</a>
     */
    public Mat rowRange(int startrow, int endrow) { return submat(startrow, endrow, 0, -1); }

    /**
     * Creates a matrix header for the specified matrix row.
     *
     * The method makes a new header for the specified matrix row and returns it.
     * This is an O(1) operation, regardless of the matrix size. The underlying data
     * of the new matrix is shared with the original matrix. Here is the example of
     * one of the classical basic matrix processing operations, "axpy", used by LU
     * and many other algorithms:
     *
     * Note:
     *
     * In the current implementation, the following code does not work as expected:
     *
     * @param i A 0-based row index.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-row">org.opencv.core.Mat.row</a>
     */
    public Mat row(int i) { return submat(i, i+1, 0, -1); }

    /**
     * Creates a matrix header for the specified row span.
     *
     * The method makes a new header for the specified column span of the matrix.
     * Similarly to "Mat.row" and "Mat.col", this is an O(1) operation.
     *
     * @param startcol A 0-based start index of the column span.
     * @param endcol A 0-based ending index of the column span.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-colrange">org.opencv.core.Mat.colRange</a>
     */
    public Mat colRange(int startcol, int endcol) { return submat(0, -1, startcol, endcol); }

    /**
     * Creates a matrix header for the specified matrix column.
     *
     * The method makes a new header for the specified matrix column and returns it.
     * This is an O(1) operation, regardless of the matrix size. The underlying data
     * of the new matrix is shared with the original matrix. See also the "Mat.row"
     * description.
     *
     * @param j A 0-based column index.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-col">org.opencv.core.Mat.col</a>
     */
    public Mat col(int j) { return submat(0, -1, j, j+1); }

    /**
     * Creates a full copy of the array and the underlying data.
     *
     * The method creates a full copy of the array. The original "step[]" is not
     * taken into account. So, the array copy is a continuous array occupying
     * "total()*elemSize()" bytes.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-clone">org.opencv.core.Mat.clone</a>
     */
    public Mat clone() {
        checkNull();
        return new Mat( nClone(nativeObj) );
    }

    public int put(int row, int col, double...data) {
        checkNull();
        if(data != null)
            return nPutD(nativeObj, row, col, data.length, data);
        else
            return 0;
    }

    public int put(int row, int col, float[] data) {
        checkNull();
        if(data != null) {
            CvType t = type();
            if(t.depth() == CvType.CV_32F) {
                return nPutF(nativeObj, row, col, data.length, data);
            }
            throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
        } else return 0;
    }

    public int put(int row, int col, int[] data) {
        checkNull();
        if(data != null) {
            CvType t = type();
            if(t.depth() == CvType.CV_32S) {
                return nPutI(nativeObj, row, col, data.length, data);
            }
            throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
        } else return 0;
    }

    public int put(int row, int col, short[] data) {
        checkNull();
        if(data != null) {
            CvType t = type();
            if(t.depth() == CvType.CV_16U || t.depth() == CvType.CV_16S) {
                return nPutS(nativeObj, row, col, data.length, data);
            }
            throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
        } else return 0;
    }

    public int put(int row, int col, byte[] data) {
        checkNull();
        if(data != null) {
            CvType t = type();
            if(t.depth() == CvType.CV_8U || t.depth() == CvType.CV_8S) {
                return nPutB(nativeObj, row, col, data.length, data);
            }
            throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
        } else return 0;
    }

    public int get(int row, int col, byte[] data) {
        checkNull();
        CvType t = type();
        if(t.depth() == CvType.CV_8U || t.depth() == CvType.CV_8S) {
            return nGetB(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, short[] data) {
        checkNull();
        CvType t = type();
        if(t.depth() == CvType.CV_16U || t.depth() == CvType.CV_16S) {
            return nGetS(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, int[] data) {
        checkNull();
        CvType t = type();
        if(t.depth() == CvType.CV_32S) {
            return nGetI(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, float[] data) {
        checkNull();
        CvType t = type();
        if(t.depth() == CvType.CV_32F) {
            return nGetF(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public int get(int row, int col, double[] data) {
        checkNull();
        CvType t = type();
        if(t.depth() == CvType.CV_64F) {
            return nGetD(nativeObj, row, col, data.length, data);
        }
        throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }

    public double[] get(int row, int col) {
        checkNull();
        //CvType t = type();
        //if(t.depth() == CvType.CV_64F) {
            return nGet(nativeObj, row, col);
        //}
        //throw new java.lang.UnsupportedOperationException("Mat data type is not compatible: " + t);
    }


    /**
     * Sets all or some of the array elements to the specified value.
     *
     * @param s Assigned scalar converted to the actual array type.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-setto">org.opencv.core.Mat.setTo</a>
     */
    public void setTo(Scalar s) {
        checkNull();
        nSetTo(nativeObj, s.val[0], s.val[1], s.val[2], s.val[3]);
    }

    /**
     * Copies the matrix to another one.
     *
     * The method copies the matrix data to another matrix. Before copying the data,
     * the method invokes
     *
     * so that the destination matrix is reallocated if needed. While "m.copyTo(m);"
     * works flawlessly, the function does not handle the case of a partial overlap
     * between the source and the destination matrices.
     *
     * When the operation mask is specified, and the "Mat.create" call shown above
     * reallocated the matrix, the newly allocated matrix is initialized with all
     * zeros before copying the data.
     *
     * @param m Destination matrix. If it does not have a proper size or type before
     * the operation, it is reallocated.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-copyto">org.opencv.core.Mat.copyTo</a>
     */
    public void copyTo(Mat m) {
        checkNull();
        if(m.nativeObj == 0)
            throw new java.lang.UnsupportedOperationException("Destination native object address is NULL");
        nCopyTo(nativeObj, m.nativeObj);
    }

    /**
     * Computes a dot-product of two vectors.
     *
     * The method computes a dot-product of two matrices. If the matrices are not
     * single-column or single-row vectors, the top-to-bottom left-to-right scan
     * ordering is used to treat them as 1D vectors. The vectors must have the same
     * size and type. If the matrices have more than one channel, the dot products
     * from all the channels are summed together.
     *
     * @param m Another dot-product operand.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-dot">org.opencv.core.Mat.dot</a>
     */
    public double dot(Mat m) {
        checkNull();
        return nDot(nativeObj, m.nativeObj);
    }

    /**
     * Computes a cross-product of two 3-element vectors.
     *
     * The method computes a cross-product of two 3-element vectors. The vectors
     * must be 3-element floating-point vectors of the same shape and size. The
     * result is another 3-element vector of the same shape and type as operands.
     *
     * @param m Another cross-product operand.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-cross">org.opencv.core.Mat.cross</a>
     */
    public Mat cross(Mat m) {
        checkNull();
        return new Mat( nCross(nativeObj, m.nativeObj) );
    }

    /**
     * Inverses a matrix.
     *
     * The method performs a matrix inversion by means of matrix expressions. This
     * means that a temporary matrix inversion object is returned by the method and
     * can be used further as a part of more complex matrix expressions or can be
     * assigned to a matrix.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-inv">org.opencv.core.Mat.inv</a>
     */
    public Mat inv() {
        checkNull();
        return new Mat( nInv(nativeObj) );
    }

    public long getNativeObjAddr() {
        return nativeObj;
    }

    /**
     * Returns an identity matrix of the specified size and type.
     *
     * The method returns a Matlab-style identity matrix initializer, similarly to
     * "Mat.zeros". Similarly to "Mat.ones", you can use a scale operation to
     * create a scaled identity matrix efficiently:
     *
     * @param rows Number of rows.
     * @param cols Number of columns.
     * @param type Created matrix type.
     *
     * @see <a href="http://opencv.itseez.com/modules/core/doc/basic_structures.html#mat-eye">org.opencv.core.Mat.eye</a>
     */
    static public Mat eye(int rows, int cols, CvType type) {
        return new Mat( nEye(rows, cols, type.toInt()) );
    }

    // native stuff
    static { System.loadLibrary("opencv_java"); }
    public final long nativeObj;
    private static native long nCreateMat();
    private static native long nCreateMat(int rows, int cols, int type);
    private static native long nCreateMat(int rows, int cols, int type, double v0, double v1, double v2, double v3);
    private static native void nRelease(long self);
    private static native void nDelete(long self);
    private static native int nType(long self);
    private static native int nRows(long self);
    private static native int nCols(long self);
    private static native long nData(long self);
    private static native boolean nIsEmpty(long self);
    private static native boolean nIsCont(long self);
    private static native boolean nIsSubmat(long self);
    private static native double[] nSize(long self);
    private static native long nSubmat(long self, int rowStart, int rowEnd, int colStart, int colEnd);
    private static native long nClone(long self);
    private static native int nPutD(long self, int row, int col, int count, double[] data);
    private static native int nPutF(long self, int row, int col, int count, float[] data);
    private static native int nPutI(long self, int row, int col, int count, int[] data);
    private static native int nPutS(long self, int row, int col, int count, short[] data);
    private static native int nPutB(long self, int row, int col, int count, byte[] data);
    private static native int nGetB(long self, int row, int col, int count, byte[] vals);
    private static native int nGetS(long self, int row, int col, int count, short[] vals);
    private static native int nGetI(long self, int row, int col, int count, int[] vals);
    private static native int nGetF(long self, int row, int col, int count, float[] vals);
    private static native int nGetD(long self, int row, int col, int count, double[] vals);
    private static native double[] nGet(long self, int row, int col);
    private static native void nSetTo(long self, double v0, double v1, double v2, double v3);
    private static native void nCopyTo(long self, long mat);
    private static native double nDot(long self, long mat);
    private static native long nCross(long self, long mat);
    private static native long nInv(long self);
    private static native long nEye(int rows, int cols, int type);
    private static native String nDump(long self);

}
