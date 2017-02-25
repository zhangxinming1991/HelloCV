package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-25.
 */
public class SpGradientScaleSpaceImageExtractorProperties<I extends SpImage<?, I> & SpSinglebandImageProcessor.Processable<Float, SpFImage, I>>
        extends
        SpScaleSpaceImageExtractorProperties<I> {

    /**
     * The gradient magnitude map
     */
    public SpFImage magnitude;

    /**
     * The gradient orientation map
     */
    public SpFImage orientation;
}
