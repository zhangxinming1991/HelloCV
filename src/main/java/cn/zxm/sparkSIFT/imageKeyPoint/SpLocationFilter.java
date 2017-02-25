package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocationFilter {
    /**
     * Test whether a {@link SpLocation} should be accepted
     * or rejected.
     * @param l the {@link SpLocation} to test.
     * @return true if the {@link SpLocation} should be accepted; false otherwise.
     */
    public abstract boolean accept(SpLocation l);
}
