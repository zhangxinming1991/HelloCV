package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramidOptions;
import org.openimaj.image.feature.local.detector.dog.extractor.DominantOrientationExtractor;
import org.openimaj.image.feature.local.detector.pyramid.BasicOctaveExtremaFinder;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public class SpDoGSIFTEngineOptions <IMAGE extends org.openimaj.image.Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
        extends GaussianPyramidOptions<IMAGE> {

    /** Threshold on the magnitude of detected points (Lowe IJCV, p.11) */
    protected float magnitudeThreshold = BasicOctaveExtremaFinder.DEFAULT_MAGNITUDE_THRESHOLD;

    protected float eigenvalueRatio = BasicOctaveExtremaFinder.DEFAULT_EIGENVALUE_RATIO;

    /**
     * Threshold for peak detection in the orientation histogram. A value of 1.0
     * would result in only a single peak being detected.
     */
    protected float peakThreshold = DominantOrientationExtractor.DEFAULT_PEAK_THRESHOLD;

    /**
     * The number of orientation histogram bins for finding the dominant
     * orientations; Lowe's IJCV paper (p.13) suggests 36 bins.
     */
    protected int numOriHistBins = 36;

    /**
     * The value for weighting the scaling Gaussian of the orientation histogram
     * relative to the keypoint scale. Lowe's IJCV paper (p.13) suggests 1.5.
     */
    protected float scaling = 1.5f;

    /**
     * The number of iterations of the smoothing filter. The vlfeat SIFT
     * implementation uses 6.
     */
    protected int smoothingIterations = 6;

    /**
     * The size of the sampling window relative to the sampling scale. Lowe's
     * ICCV paper suggests 3;
     */
    protected float samplingSize = 3.0f;

    /** The number of orientation bins (default 8) */
    protected int numOriBins = 8;

    /** The number of spatial bins in each direction (default 4) */
    protected int numSpatialBins = 4;

    /** Threshold for the maximum value allowed in the histogram (default 0.2) */
    protected float valueThreshold = 0.2f;

    /**
     * The width of the Gaussian used for weighting samples, relative to the
     * half-width of the sampling window (default 1.0).
     */
    protected float gaussianSigma = 1.0f;

    /**
     * The magnification factor determining the size of a spatial SIFT bin
     * relative to the scale. The overall sampling size is related to the number
     * of spatial bins.
     */
    protected float magnificationFactor = 3;
}
