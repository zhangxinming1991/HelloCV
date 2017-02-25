package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import org.openimaj.image.FImage;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.feature.local.detector.pyramid.AbstractOctaveExtremaFinder;

/**
 * Created by root on 17-2-24.
 */
public class SpBasicOctaveExtremaFinder extends SpAbstractOctaveExtremaFinder<SpGaussianOctave<SpFImage>> {

    public static final float DEFAULT_MAGNITUDE_THRESHOLD = 0.04f; // Lowe's

    protected float scales;
    protected float normMagnitudeScales;

    protected float magnitudeThreshold = DEFAULT_MAGNITUDE_THRESHOLD;

    /**
     * Construct with the given magnitude and Eigenvalue thresholds
     *
     * @param magnitudeThreshold
     *            the magnitude threshold
     * @param eigenvalueRatio
     *            the Eigenvalue threshold
     */
    public SpBasicOctaveExtremaFinder(float magnitudeThreshold, float eigenvalueRatio) {
        super(eigenvalueRatio);
        this.magnitudeThreshold = magnitudeThreshold;
    }

    @Override
    protected void beforeProcess(SpGaussianOctave<SpFImage> octave) {
        scales = octave.options.getScales();

        // the magnitude threshold must be adjusted based on the number of
        // scales,
        // as more scales will result in smaller differences between scales
        normMagnitudeScales = magnitudeThreshold / octave.options.getScales();
    }

    @Override
    protected void processExtrema(SpFImage[] dogs, int s, int x, int y, float octSize) {
        // calculate the actual scale within the octave
        final float octaveScale = octave.options.getInitialSigma() * (float) Math.pow(2.0, s / scales);

        // fire the listener
        if (listener != null)
            listener.foundInterestPoint(this, x, y, octaveScale);
    }

    @Override
    protected boolean firstCheck(float val, int x, int y, int s, SpFImage[] dogs) {
        // perform magnitude check
        if (Math.abs(dogs[s].pixels[y][x]) > normMagnitudeScales) {
            return true;
        }
        return false;
    }
}
