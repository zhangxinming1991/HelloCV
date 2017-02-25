package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;

/**
 * Created by root on 17-2-23.
 */
public interface SpEngine <FEATURE extends LocalFeature<?, ?>, IMAGE extends SpImage<?, IMAGE>>{
    /**
     * Find local features in the given image and return them.
     *
     * @param image
     *            the image
     * @return the features.
     */
    public LocalFeatureList<FEATURE> findFeatures(IMAGE image);
}
