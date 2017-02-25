package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.FeatureLocal.SpDominantOrientationExtractor;
import cn.zxm.sparkSIFT.FeatureLocal.SpOrientationHistogramExtractor;
import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
//import org.openimaj.feature.local.list.LocalFeatureList;

import cn.zxm.sparkSIFT.imageKeyPoint.SpKeypoint;
import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeatureList;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
//import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Created by root on 17-2-23.
 */
public class SpDoGSIFTEngine implements SpEngine<SpKeypoint, SpFImage> {
    SpDoGSIFTEngineOptions<SpFImage> options;

    /**
     * Construct a DoGSIFTEngine with the default options.
     */
    public SpDoGSIFTEngine() {
        this(new SpDoGSIFTEngineOptions<SpFImage>());
    }

    /**
     * Construct a DoGSIFTEngine with the given options.
     *
     * @param options
     *            the options
     */
    public SpDoGSIFTEngine(SpDoGSIFTEngineOptions<SpFImage> options) {
        this.options = options;
    }


    @Override
    public SpLocalFeatureList<SpKeypoint> findFeatures(SpFImage image) {
        final SpOctaveInterestPointFinder<SpGaussianOctave<SpFImage>, SpFImage> finder =
                new SpDoGOctaveExtremaFinder(new SpBasicOctaveExtremaFinder(options.magnitudeThreshold,
                        options.eigenvalueRatio));

        final SpCollector<SpGaussianOctave<SpFImage>, SpKeypoint, SpFImage> collector = new SpOctaveKeypointCollector<SpFImage>(
                new SpGradientFeatureExtractor(
                        new SpDominantOrientationExtractor(
                                options.peakThreshold,
                                new SpOrientationHistogramExtractor(
                                        options.numOriHistBins,
                                        options.scaling,
                                        options.smoothingIterations,
                                        options.samplingSize
                                )
                        ),
                        new SIFTFeatureProvider(
                                options.numOriBins,
                                options.numSpatialBins,
                                options.valueThreshold,
                                options.gaussianSigma
                        ),
                        options.magnificationFactor * options.numSpatialBins
                )
        );

        finder.setOctaveInterestPointListener(collector);

        options.setOctaveProcessor(finder);

        final SpGaussianPyramid<SpFImage> pyr = new SpGaussianPyramid<SpFImage>(options);
        pyr.process(image);

        return collector.getFeatures();
    }

    /**
     * @return the current options used by the engine
     */
    public SpDoGSIFTEngineOptions<SpFImage> getOptions() {
        return options;
    }

}
