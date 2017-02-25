package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeatureList;


/**
 * Created by root on 17-2-23.
 */
public interface SpEngine <FEATURE extends SpLocalFeature<?, ?>, IMAGE extends SpImage<?, IMAGE>>{
    /**
     * Find local features in the given image and return them.
     *
     * @param image
     *            the image
     * @return the features.
     */
    public SpLocalFeatureList<FEATURE> findFeatures(IMAGE image);
}
