package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import org.openimaj.image.feature.local.extraction.ExtractorProperties;

/**
 * Created by root on 17-2-25.
 */
public class SpLocalImageExtractorProperties<I extends SpImage<?, I> & SpSinglebandImageProcessor.Processable<Float, SpFImage, I>>
        implements
        ExtractorProperties {

    /**
     * The image being processed
     */
    public I image;

    /**
     * The x-ordinate of the interest point
     */
    public float x;

    /**
     * The y-ordinate of the interest point
     */
    public float y;
}
