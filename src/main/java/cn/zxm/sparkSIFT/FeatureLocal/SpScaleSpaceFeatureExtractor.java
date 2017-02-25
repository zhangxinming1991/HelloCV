package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import org.openimaj.feature.FeatureVector;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;

/**
 * Created by root on 17-2-25.
 */
public interface SpScaleSpaceFeatureExtractor	<F extends FeatureVector,
        IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>>
        extends
        FeatureVectorExtractor<F, SpScaleSpaceImageExtractorProperties<IMAGE>> {


}
