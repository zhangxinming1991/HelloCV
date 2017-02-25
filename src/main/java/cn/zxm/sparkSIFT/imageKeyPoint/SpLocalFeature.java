package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.io.ReadWriteable;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocalFeature <L extends SpLocation, T extends SpFeatureVector>
        extends
        ReadWriteable,
        SpLocalFeatureVectorProvider<L, T> {
}
