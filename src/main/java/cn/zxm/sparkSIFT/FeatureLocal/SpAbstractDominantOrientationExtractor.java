package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;

/**
 * Created by root on 17-2-25.
 */
public abstract class SpAbstractDominantOrientationExtractor implements FeatureVectorExtractor<FloatFV, SpGradientScaleSpaceImageExtractorProperties<SpFImage>> {

    /**
     * Find the dominant orientations
     *
     * @param props Properties describing the interest point in scale space.
     * @return an FloatFV containing the angles of the dominant orientations [-PI to PI].
     */
    @Override
    public FloatFV[] extractFeature(SpGradientScaleSpaceImageExtractorProperties<SpFImage> props) {
        return new FloatFV[] { new FloatFV(extractFeatureRaw(props)) };
    }

    /**
     * Find the dominant orientations.
     *
     * @param props Properties describing the interest point in scale space.
     * @return an array of the angles of the dominant orientations [-PI to PI].
     */
    public abstract float[] extractFeatureRaw(SpGradientScaleSpaceImageExtractorProperties<SpFImage> props);

}
