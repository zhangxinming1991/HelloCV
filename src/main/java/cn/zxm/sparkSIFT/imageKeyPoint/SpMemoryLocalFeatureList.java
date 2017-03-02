package cn.zxm.sparkSIFT.imageKeyPoint;

import org.apache.hadoop.io.Writable;
import org.openimaj.data.RandomData;
import org.openimaj.feature.local.LocationFilter;
import org.openimaj.io.IOUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by root on 17-2-25.
 */
public class SpMemoryLocalFeatureList <T extends SpLocalFeature<?, ?>> extends ArrayList<T> implements SpLocalFeatureList<T>{


    private static final long serialVersionUID = 1L;

    protected int cached_veclen = -1;

    /**
     * Construct an empty feature list
     */
    public SpMemoryLocalFeatureList() {
    }

    /**
     * Construct an empty list with the given feature-vector length.
     *
     * @param veclen
     *            the expected length of the feature vectors of each local
     *            feature.
     */
    public SpMemoryLocalFeatureList(int veclen) {
        super();
        this.cached_veclen = veclen;
    }

    /**
     * Construct a local feature list from the given collection of local
     * features.
     *
     * @param c
     *            Collection of local feature to add to the list instance.
     */
    public SpMemoryLocalFeatureList(Collection<? extends T> c) {
        super(c);
        if (size() > 0)
            cached_veclen = this.get(0).getFeatureVector().length();
    }

    /**
     * Construct an empty list with the given feature-vector length and
     * pre-allocate the underlying array with space for initialCapacity local
     * features. The list will automatically grow once initialCapacity is
     * reached.
     *
     * @param veclen
     *            the expected length of the feature vectors of each local
     *            feature.
     * @param initialCapacity
     *            the initial capacity of the list.
     */
    public SpMemoryLocalFeatureList(int veclen, int initialCapacity) {
        super(initialCapacity);
        this.cached_veclen = veclen;
    }


    /**
     * Create a MemoryLocalFeatureList by reading all the local features from
     * the specified file.
     *
     * @param <T>
     *            the type of local feature
     * @param keypointFile
     *            the file from which to read the features
     * @param clz
     *            the class of local feature
     * @return a new MemoryLocalFeatureList populated with features from the
     *         file
     * @throws IOException
     *             if an error occurs reading the file
     */
    public static <T extends SpLocalFeature<?, ?>> SpMemoryLocalFeatureList<T> read(File keypointFile, Class<T> clz)
            throws IOException
    {
        final boolean isBinary = IOUtils.isBinary(keypointFile, SpLocalFeatureList.BINARY_HEADER);
        final SpMemoryLocalFeatureList<T> list = new SpMemoryLocalFeatureList<T>();

        if (isBinary) {
            SpLocalFeatureListUtils.readBinary(keypointFile, list, clz);
        } else {
            SpLocalFeatureListUtils.readASCII(keypointFile, list, clz);
        }

        return list;
    }

    /**
     * Create a MemoryLocalFeatureList by reading all the local features from
     * the specified stream.
     *
     * @param <T>
     *            the type of local feature
     * @param stream
     *            the input stream from which to read the features
     * @param clz
     *            the class of local feature
     * @return a new MemoryLocalFeatureList populated with features from the
     *         file
     * @throws IOException
     *             if an error occurs reading the file
     */
    public static <T extends SpLocalFeature<?, ?>> SpMemoryLocalFeatureList<T> read(InputStream stream, Class<T> clz)
            throws IOException
    {
        return read(new BufferedInputStream(stream), clz);
    }

    /**
     * Create a MemoryLocalFeatureList by reading all the local features from
     * the specified stream.
     *
     * @param <T>
     *            the type of local feature
     * @param stream
     *            the input stream from which to read the features
     * @param clz
     *            the class of local feature
     * @return a new MemoryLocalFeatureList populated with features from the
     *         file
     * @throws IOException
     *             if an error occurs reading the file
     */
    public static <T extends SpLocalFeature<?, ?>> SpMemoryLocalFeatureList<T> read(BufferedInputStream stream, Class<T> clz)
            throws IOException
    {
        final boolean isBinary = IOUtils.isBinary(stream, SpLocalFeatureList.BINARY_HEADER);
        final SpMemoryLocalFeatureList<T> list = new SpMemoryLocalFeatureList<T>();

        if (isBinary) {
            SpLocalFeatureListUtils.readBinary(stream, list, clz);
        } else {
            SpLocalFeatureListUtils.readASCII(stream, list, clz);
        }

        return list;
    }

    /**
     * Create a MemoryLocalFeatureList by reading all the local features from
     * the specified {@link DataInput}. Reading of the header is skipped, and it
     * is assumed that the data is in binary format.
     *
     * @param <T>
     *            the type of local feature
     * @param in
     *            the data input from which to read the features
     * @param clz
     *            the class of local feature
     * @return a new MemoryLocalFeatureList populated with features from the
     *         file
     * @throws IOException
     *             if an error occurs reading the file
     */
    public static <T extends SpLocalFeature<?, ?>> SpMemoryLocalFeatureList<T> readNoHeader(DataInput in, Class<T> clz)
            throws IOException
    {
        final SpMemoryLocalFeatureList<T> list = new SpMemoryLocalFeatureList<T>();

        SpLocalFeatureListUtils.readBinary(in, list, clz);

        return list;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Q> Q[] asDataArray(Q[] a) {
        if (a.length < size()) {
            System.out.println(a.getClass());
            a = (Q[]) Array.newInstance(a.getClass().getComponentType(), size());
        }

        int i = 0;
        for (final T t : this) {
            a[i++] = (Q) t.getFeatureVector().getVector();
        }

        return a;
    }

    @Override
    public SpMemoryLocalFeatureList<T> randomSubList(int nelem) {
        SpMemoryLocalFeatureList<T> kl;

        if (nelem > size()) {
            kl = new SpMemoryLocalFeatureList<T>(this);
            Collections.shuffle(kl);
        } else {
            final int[] rnds = RandomData.getUniqueRandomInts(nelem, 0, this.size());
            kl = new SpMemoryLocalFeatureList<T>(cached_veclen);

            for (final int idx : rnds)
                kl.add(this.get(idx));
        }

        return kl;
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        resetVecLength();
        SpLocalFeatureListUtils.writeBinary(out, this);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        resetVecLength();
        SpLocalFeatureListUtils.writeASCII(out, this);
    }

    @Override
    public byte[] binaryHeader() {
        return SpLocalFeatureList.BINARY_HEADER;
    }

    @Override
    public String asciiHeader() {
        return "";
    }

    /*
     * @see org.openimaj.feature.local.list.LocalFeatureList#vecLength()
     */
    @Override
    public int vecLength() {
        resetVecLength();

        if (cached_veclen == -1) {
            if (size() > 0) {
                cached_veclen = get(0).getFeatureVector().length();
            }
        }
        return cached_veclen;
    }

    /**
     * Reset the internal feature vector length to the length of the first
     * feature. You must call this if you change the length of the features
     * within the list.
     */
    public void resetVecLength() {
        if (size() > 0) {
            cached_veclen = get(0).getFeatureVector().length();
        }
    }

    /**
     * Create a new list by applying a {@link LocationFilter} to all the
     * elements of this list. Only items which are accepted by the filter will
     * be added to the new list.
     *
     * @param locationFilter
     *            the location filter
     * @return a filtered list
     */
    public SpMemoryLocalFeatureList<T> filter(SpLocationFilter locationFilter) {
        final SpMemoryLocalFeatureList<T> newlist = new SpMemoryLocalFeatureList<T>();
        for (final T t : this) {
            if (locationFilter.accept(t.getLocation()))
                newlist.add(t);
        }
        return newlist;
    }

    @Override
    public SpMemoryLocalFeatureList<T> subList(int fromIndex, int toIndex) {
        return new SpMemoryLocalFeatureList<T>(super.subList(fromIndex, toIndex));
    }

}
