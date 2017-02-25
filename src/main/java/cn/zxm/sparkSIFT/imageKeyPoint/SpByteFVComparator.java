package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public interface SpByteFVComparator extends SpFVComparator<SpByteFV> {

    /**
     * Compare two feature vectors in the form of native arrays,
     * returning a score or distance.
     *
     * @param h1 the first feature array
     * @param h2 the second feature array
     * @return a score or distance
     */
    public abstract double compare(byte[] h1, byte[] h2);
}
