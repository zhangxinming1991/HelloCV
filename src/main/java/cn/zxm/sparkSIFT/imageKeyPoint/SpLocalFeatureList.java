package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.io.Writeable;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocalFeatureList <T extends SpLocalFeature<?, ?>> extends SpRandomisableList<T>, Writeable {

    /** The header used when writing LocalFeatureLists to streams and files */
    public static final byte[] BINARY_HEADER = "KPT".getBytes();

    /**
     * Get the feature-vector data of the list as a two-dimensional array of
     * data. The number of rows will equal the number of features in the list,
     * and the type &lt;Q&gt;must be compatible with the data type of the features
     * themselves.
     *
     * @param <Q>
     *            the data type
     * @param a
     *            the array to fill
     * @return the array, filled with the feature-vector data.
     */
    public <Q> Q[] asDataArray(Q[] a);

    /**
     * Get the length of the feature-vectors of each local feature if they are
     * constant.
     *
     * This value is used as instantiate new local features in the case that the
     * local feature has a constructor that takes a single integer.
     *
     * @return the feature-vector length
     */
    public int vecLength();

    @Override
    public SpLocalFeatureList<T> subList(int fromIndex, int toIndex);

}
