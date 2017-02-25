package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocationProvider <L extends SpLocation>{

    /**
     * Get the location associated with this object.
     *
     * @return the location.
     */
    L getLocation();
}
