package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.feature.local.descriptor.gradient.SIFTFeatureProvider;
import org.openimaj.image.feature.local.detector.dog.collector.Collector;
import org.openimaj.image.feature.local.detector.dog.collector.OctaveKeypointCollector;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.GradientFeatureExtractor;
import org.openimaj.image.feature.local.detector.dog.extractor.OrientationHistogramExtractor;
import org.openimaj.image.feature.local.detector.dog.pyramid.DoGOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.feature.local.detector.pyramid.OctaveInterestPointFinder;
import org.openimaj.image.feature.local.keypoints.Keypoint;

/**
 * Created by root on 17-2-23.
 */
public class SpDoGSIFTEngine implements SpEngine<Keypoint, FImage> {
    SpDoGSIFTEngineOptions<FImage> options;

    /**
     * Construct a DoGSIFTEngine with the default options.
     */
    public SpDoGSIFTEngine() {
        this(new SpDoGSIFTEngineOptions<FImage>());
    }

    /**
     * Construct a DoGSIFTEngine with the given options.
     *
     * @param options
     *            the options
     */
    public SpDoGSIFTEngine(SpDoGSIFTEngineOptions<FImage> options) {
        this.options = options;
    }


    @Override
    public LocalFeatureList<Keypoint> findFeatures(FImage image) {
        final OctaveInterestPointFinder<GaussianOctave<FImage>, FImage> finder =
                new DoGOctaveExtremaFinder(new BasicOctaveExtremaFinder(options.magnitudeThreshold,
                        options.eigenvalueRatio));

        final Collector<GaussianOctave<FImage>, Keypoint, FImage> collector = new OctaveKeypointCollector<FImage>(
                new GradientFeatureExtractor(
                        new DominantOrientationExtractor(
                                options.peakThreshold,
                                new OrientationHistogramExtractor(
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

        final GaussianPyramid<FImage> pyr = new GaussianPyramid<FImage>(options);
        pyr.process(image);

        return collector.getFeatures();
    }

    /**
     * @return the current options used by the engine
     */
    public SpDoGSIFTEngineOptions<FImage> getOptions() {
        return options;
    }

}
