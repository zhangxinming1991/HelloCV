package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.FeatureLocal.SpScaleSpaceImageExtractorProperties;
import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeatureList;
import cn.zxm.sparkSIFT.imageKeyPoint.SpMemoryLocalFeatureList;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;

/**
 * Created by root on 17-2-24.
 */
public abstract class SpAbstractOctaveLocalFeatureCollector <OCTAVE extends SpOctave<?, ?, IMAGE>, EXTRACTOR extends FeatureVectorExtractor<?, SpScaleSpaceImageExtractorProperties<IMAGE>>, FEATURE extends SpLocalFeature<?, ?>, IMAGE extends SpImage<?, IMAGE> & SpSinglebandImageProcessor.Processable<Float, SpFImage, IMAGE>>
        implements
        SpCollector<OCTAVE, FEATURE, IMAGE> {

    protected EXTRACTOR featureExtractor;
    protected SpLocalFeatureList<FEATURE> features = new SpMemoryLocalFeatureList<FEATURE>();

    /**
     * Construct the AbstractOctaveLocalFeatureCollector with the given feature
     * extractor.
     *
     * @param featureExtractor
     *            the feature extractor
     */
    public SpAbstractOctaveLocalFeatureCollector(EXTRACTOR featureExtractor) {
        this.featureExtractor = featureExtractor;
    }

    /**
     * Get the list of features collected.
     *
     * @return the features
     */
    @Override
    public SpLocalFeatureList<FEATURE> getFeatures() {
        return features;
    }

}
