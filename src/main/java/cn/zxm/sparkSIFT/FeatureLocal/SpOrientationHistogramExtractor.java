package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import org.openimaj.feature.FloatFV;
import org.openimaj.image.feature.local.extraction.FeatureVectorExtractor;

/**
 * Created by root on 17-2-25.
 */
public class SpOrientationHistogramExtractor implements FeatureVectorExtractor<FloatFV, SpGradientScaleSpaceImageExtractorProperties<SpFImage>> {

    /**
     * Default number of orientation histogram bins;
     * Lowe's IJCV paper (p.13) suggests 36 bins.
     */
    public static int DEFAULT_NUM_BINS = 36;

    /**
     * Default value for weighting the scaling Gaussian
     * relative to the keypoint scale.
     * Lowe's IJCV paper (p.13) suggests 1.5.
     */
    public static float DEFAULT_SCALING = 1.5f;

    /**
     * Default value for the number of iterations of the smoothing
     * filter. The vlfeat SIFT implementation uses 6.
     */
    public static int DEFAULT_SMOOTHING_ITERATIONS = 6;

    /**
     * Default value for the size of the sampling window relative
     * to the sampling scale. Lowe's ICCV paper suggests 3;
     */
    public static float DEFAULT_SAMPLING_SIZE = 3.0f;

    protected int numBins;
    protected float scaling;
    protected int smoothingIterations;
    protected float samplingSize;

    /**
     * Default constructor.
     */
    public SpOrientationHistogramExtractor() {
        this(DEFAULT_NUM_BINS, DEFAULT_SCALING, DEFAULT_SMOOTHING_ITERATIONS, DEFAULT_SAMPLING_SIZE);
    }

    /**
     * Construct with the given parameter values.
     * @param numBins number of orientation histogram bins
     * @param scaling weighting for the scaling Gaussian relative to the keypoint scale.
     * @param smoothingIterations the number of iterations of the smoothing filter
     * @param samplingSize size of the sampling window relative to the sampling scale.
     */
    public SpOrientationHistogramExtractor(int numBins, float scaling, int smoothingIterations, float samplingSize) {
        this.numBins = numBins;
        this.scaling = scaling;
        this.smoothingIterations = smoothingIterations;
        this.samplingSize = samplingSize;
    }

    /**
     * Extract the orientation histogram given the properties. This method
     * caches gradient and orientation maps as it's likely to be called
     * multiple times (at different positions) for the same input image.
     *
     * @param props Properties describing the interest point in scale space.
     * @return a FloatFV object representing the orientation histogram.
     */
    @Override
    public FloatFV[] extractFeature(SpGradientScaleSpaceImageExtractorProperties<SpFImage> props) {
        return new FloatFV[] { new FloatFV(extractFeatureRaw(props)) };
    }

    /**
     * Extract the orientation histogram given the properties. This method
     * caches gradient and orientation maps as it's likely to be called
     * multiple times (at different positions) for the same input image.
     *
     * @param properties Properties describing the interest point in scale space.
     * @return a float array representing the orientation histogram.
     */
    public float[] extractFeatureRaw(SpGradientScaleSpaceImageExtractorProperties<SpFImage> properties) {
        return createHistogram(properties.x, properties.y, properties.scale, properties.magnitude, properties.orientation);
    }

    /**
     * Calculate the orientation histogram in a circular region about
     * this interest point. The pixel contributions to the histogram
     * are weighted by a Gaussian of variance sigma, which is proportional
     * to the scale of the interest point. The radius of sampling region is
     * proportional to sigma.
     */
    float [] createHistogram(float fx, float fy, float scale, SpFImage magnitude, SpFImage orientation) {
        float hist[] = new float[numBins];

        int ix = Math.round(fx);
        int iy = Math.round(fy);

        //sigma is calculated relative to the interest point scale
        float sigma = scaling * scale;
        //the radius is relative to sigma
        int radius = (int) (sigma * samplingSize);

        //don't loop outside the valid pixel area
        int startx = Math.max(ix - radius, 1);
        int stopx = Math.min(ix+radius, magnitude.width-2);
        int starty = Math.max(iy - radius, 1);
        int stopy = Math.min(iy+radius, magnitude.height-2);

        float radiusSq = (radius + 0.5f) * (radius + 0.5f); //the square of the radius + half a pel
        double sigmaSq2 = 2.0 * sigma * sigma; //2*sigma*sigma; for the Gaussian

        //loop over the square containing the sampling circle
        for (int y=starty; y<=stopy; y++) {
            for (int x=startx; x<=stopx; x++) {
                float distsq = (y - fy) * (y - fy) + (x - fx) * (x - fx);

                if (distsq <= radiusSq) {
                    float weight = (float) Math.exp(-distsq / sigmaSq2);

                    float angle = orientation.pixels[y][x]; //angle is in range of -PI to PI.

                    //now find the right bin
                    int bin = (int) (numBins * (angle + Math.PI) / (0.00001 + (2.0 * Math.PI)));
                    hist[bin] += weight * magnitude.pixels[y][x];
                }
            }
        }

        //smooth the histogram
        for (int i=0; i<smoothingIterations; i++)
            circularSmooth(hist);

        return hist;
    }

    /**
     * Smooth the values in a circular buffer with a (1/3)[1,1,1] kernel.
     * @param buffer buffer to smooth
     */
    protected void circularSmooth(float[] buffer) {
        float prev = buffer[buffer.length - 1];

        for (int i = 0; i < buffer.length; i++) {
            float temp = buffer[i];
            buffer[i] = (prev + buffer[i] + buffer[(i + 1 == buffer.length) ? 0 : i + 1]) / 3.0f;
            prev = temp;
        }
    }
}
