package cn.zxm.sparkSIFT.FeatureLocal;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import gnu.trove.list.array.TFloatArrayList;

/**
 * Created by root on 17-2-25.
 */
public class SpDominantOrientationExtractor extends SpAbstractDominantOrientationExtractor {

    /**
     * Default value for the threshold at which other peaks are detected
     * relative to the biggest peak. Lowe's IJCV paper suggests a
     * value of 80%.
     */
    public static float DEFAULT_PEAK_THRESHOLD = 0.8f;

    protected SpOrientationHistogramExtractor oriHistExtractor;

    /**
     * Threshold for peak detection. A value of 1.0 would
     * result in only a single peak being detected.
     */
    protected float peakThreshold;

    /**
     * Construct with default values.
     */
    public SpDominantOrientationExtractor() {
        this(DEFAULT_PEAK_THRESHOLD, new SpOrientationHistogramExtractor());
    }

    /**
     * Construct with given parameters.
     * @param peakThreshold threshold at which other peaks are detected relative to the biggest peak
     * @param oriHistExtractor the orientation histogram extractor
     */
    public SpDominantOrientationExtractor(float peakThreshold, SpOrientationHistogramExtractor oriHistExtractor) {
        this.peakThreshold = peakThreshold;
        this.oriHistExtractor = oriHistExtractor;
    }

    /**
     * Extract an orientation histogram and find the dominant orientations
     * by looking for peaks.
     *
     * @param properties Properties describing the interest point in scale space.
     * @return an array of the angles of the dominant orientations [-PI to PI].
     */
    @Override
    public float [] extractFeatureRaw(SpGradientScaleSpaceImageExtractorProperties<SpFImage> properties) {
        //extract histogram
        float[] hist = getOriHistExtractor().extractFeatureRaw(properties);

        //find max
        float maxval = 0;
        for (int i = 0; i < getOriHistExtractor().numBins; i++)
            if (hist[i] > maxval)
                maxval = hist[i];

        float thresh = peakThreshold * maxval;

        //search for peaks within peakThreshold of the maximum
        TFloatArrayList dominantOrientations = new TFloatArrayList();
        for (int i = 0; i < getOriHistExtractor().numBins; i++) {
            float prevVal = hist[(i == 0 ? getOriHistExtractor().numBins - 1 : i - 1)];
            float nextVal = hist[(i == getOriHistExtractor().numBins - 1 ? 0 : i + 1)];
            float thisVal = hist[i];

            if (thisVal >= thresh && thisVal > prevVal && thisVal > nextVal) {
                //fit a parabola to the peak to find the position of the actual maximum
                float peakDelta = fitPeak(prevVal, thisVal, nextVal);
                float angle = 2.0f * (float)Math.PI * (i + 0.5f + peakDelta) / getOriHistExtractor().numBins - (float)Math.PI;

                dominantOrientations.add(angle);
            }
        }

        return dominantOrientations.toArray();
    }

    /**
     * Fit a parabola to three evenly spaced samples and return the relative
     * position of the peak to the second sample.
     */
    float fitPeak(float a, float b, float c) {
        //a is at x=-1, b at x=0, c at x=1
        //y = A*x*x + B*x + C
        //y' = 2*A*x + B
        //solve for A,B,C then for x where y'=0

        return 0.5f * (a - c) / (a - 2.0f * b + c);
    }


    /**
     * @return the orientation histogram extractor
     */
    public SpOrientationHistogramExtractor getOriHistExtractor() {
        return oriHistExtractor;
    }
}
