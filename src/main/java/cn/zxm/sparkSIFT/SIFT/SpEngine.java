package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.Image;

/**
 * Created by root on 17-2-23.
 */
public interface SpEngine <FEATURE extends LocalFeature<?, ?>, IMAGE extends Image<?, IMAGE>>{
    /**
     * Find local features in the given image and return them.
     *
     * @param image
     *            the image
     * @return the features.
     */
    public LocalFeatureList<FEATURE> findFeatures(IMAGE image);
}
