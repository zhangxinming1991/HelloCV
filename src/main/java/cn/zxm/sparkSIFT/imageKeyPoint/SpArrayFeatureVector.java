package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public abstract class SpArrayFeatureVector <ARRAYTYPE> implements SpFeatureVector{

    private static final long serialVersionUID = 1L;

    /**
     * Array of all the values in the feature vector
     */
    public ARRAYTYPE values;

    /**
     * Get the underlying representation
     *
     * @return the feature as an array
     */
    @Override
    public ARRAYTYPE getVector() {
        return values;
    }

    /**
     * Returns a new featurevector that is a subsequence of this vector. The
     * subsequence begins with the element at the specified index and extends to
     * the end of this vector.
     *
     * @param beginIndex
     *            the beginning index, inclusive.
     * @return the specified subvector.
     * @exception IndexOutOfBoundsException
     *                if <code>beginIndex</code> is negative or larger than the
     *                length of this <code>ArrayFeatureVector</code> object.
     */
    public abstract SpArrayFeatureVector<ARRAYTYPE> subvector(int beginIndex);

    /**
     * Returns a new string that is a subvector of this vector. The subvector
     * begins at the specified <code>beginIndex</code> and extends to the
     * element at index <code>endIndex - 1</code>. Thus the length of the
     * subvector is <code>endIndex-beginIndex</code>.
     *
     * @param beginIndex
     *            the beginning index, inclusive.
     * @param endIndex
     *            the ending index, exclusive.
     * @return the specified subvector.
     * @exception IndexOutOfBoundsException
     *                if the <code>beginIndex</code> is negative, or
     *                <code>endIndex</code> is larger than the length of this
     *                <code>ArrayFeatureVector</code> object, or
     *                <code>beginIndex</code> is larger than
     *                <code>endIndex</code>.
     */
    public abstract SpArrayFeatureVector<ARRAYTYPE> subvector(int beginIndex, int endIndex);

}
