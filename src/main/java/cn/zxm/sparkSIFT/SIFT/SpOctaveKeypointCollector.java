package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.FeatureLocal.SpScaleSpaceFeatureExtractor;
import cn.zxm.sparkSIFT.FeatureLocal.SpScaleSpaceImageExtractorProperties;
import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import org.openimaj.feature.OrientedFeatureVector;
import org.openimaj.image.feature.local.detector.dog.extractor.ScaleSpaceFeatureExtractor;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.extraction.ScaleSpaceImageExtractorProperties;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.image.processor.SinglebandImageProcessor;

import javax.validation.Valid;

/**
 * Created by root on 17-2-24.
 */
public class SpOctaveKeypointCollector<
        IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>>
        extends
        SpAbstractOctaveLocalFeatureCollector<
                SpGaussianOctave<IMAGE>,
                SpScaleSpaceFeatureExtractor<OrientedFeatureVector, IMAGE>,
                Keypoint,
                IMAGE
                > {

    protected SpScaleSpaceImageExtractorProperties<IMAGE> extractionProperties = new SpScaleSpaceImageExtractorProperties<IMAGE>();

    public SpOctaveKeypointCollector(SpScaleSpaceFeatureExtractor<OrientedFeatureVector, IMAGE> featureExtractor) {
        super(featureExtractor);
    }

    @Override
    public void foundInterestPoint(SpOctaveInterestPointFinder<SpGaussianOctave<IMAGE>, IMAGE> finder, float x, float y, float octaveScale) {
        int currentScaleIndex = finder.getCurrentScaleIndex();
        extractionProperties.image = finder.getOctave().images[currentScaleIndex];
        extractionProperties.scale = octaveScale;
        extractionProperties.x = x;
        extractionProperties.y = y;

        float octSize = finder.getOctave().octaveSize;

        addFeature(octSize * x, octSize * y, octSize * octaveScale);
    }

    protected void addFeature(float imx, float imy, float imscale) {
        OrientedFeatureVector[] fvs = featureExtractor.extractFeature(extractionProperties);

        for (OrientedFeatureVector fv : fvs) {
            features.add(new Keypoint(imx, imy, fv.orientation, imscale, fv.values));
        }
    }

}
