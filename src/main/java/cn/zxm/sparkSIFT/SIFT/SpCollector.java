package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import org.openimaj.feature.local.LocalFeature;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public interface SpCollector <OCTAVE extends SpOctave<?, ?, IMAGE>, FEATURE extends LocalFeature<?, ?>, IMAGE extends SpImage<?, IMAGE> & SpSinglebandImageProcessor.Processable<Float, SpFImage, IMAGE>>
        extends
        SpOctaveInterestPointListener<OCTAVE, IMAGE> {

    /**
     * Get the list of features collected.
     *
     * @return the features
     */
    public LocalFeatureList<FEATURE> getFeatures();
}
