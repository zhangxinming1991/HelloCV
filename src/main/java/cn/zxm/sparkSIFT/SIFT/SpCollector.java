package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeatureList;

/**
 * Created by root on 17-2-24.
 */
public interface SpCollector <OCTAVE extends SpOctave<?, ?, IMAGE>, FEATURE extends SpLocalFeature<?, ?>, IMAGE extends SpImage<?, IMAGE> & SpSinglebandImageProcessor.Processable<Float, SpFImage, IMAGE>>
        extends
        SpOctaveInterestPointListener<OCTAVE, IMAGE> {

    /**
     * Get the list of features collected.
     *
     * @return the features
     */
    public SpLocalFeatureList<FEATURE> getFeatures();
}
