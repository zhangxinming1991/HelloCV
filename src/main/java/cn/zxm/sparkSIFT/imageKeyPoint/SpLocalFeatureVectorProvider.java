package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.feature.FeatureVectorProvider;
import org.openimaj.feature.local.LocationProvider;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocalFeatureVectorProvider <L extends SpLocation, T extends SpFeatureVector>
        extends
        SpFeatureVectorProvider<T>,
        SpLocationProvider<L> {
}
