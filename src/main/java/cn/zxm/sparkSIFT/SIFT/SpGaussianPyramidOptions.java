package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianOctave;
import org.openimaj.image.analysis.pyramid.gaussian.GaussianPyramid;
import org.openimaj.image.processing.convolution.FGaussianConvolve;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public class SpGaussianPyramidOptions <IMAGE extends org.openimaj.image.Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
        extends
        SpPyramidOptions<SpGaussianOctave<IMAGE>, IMAGE> {

    /**
     * Number of pixels of border for processors to ignore. Also used in
     * calculating the minimum image size for the last octave.
     */
    protected int borderPixels = 5;

    /**
     * Should the starting image of the pyramid be stretched to twice its size?
     */
    protected boolean doubleInitialImage = true;

    /**
     * The number of extra scale steps taken beyond scales.
     */
    protected int extraScaleSteps = 2; // number of extra steps to take beyond
    // doubling sigma

    /**
     * Assumed initial scale of the first image in each octave. For SIFT, Lowe
     * suggested 1.6 (for optimal repeatability; see Lowe's IJCV paper, P.10).
     */
    protected float initialSigma = 1.6f;

    /**
     * The number of scales in this octave minus extraScaleSteps. Levels are
     * constructed so that level[scales] has twice the sigma of level[0].
     */
    protected int scales = 3;

    /**
     * Default constructor.
     */
    public SpGaussianPyramidOptions() {

    }

    /**
     * Create a {@link SinglebandImageProcessor} that performs a Gaussian
     * blurring with a standard deviation given by sigma. This method is used by
     * the {@link GaussianOctave} and {@link GaussianPyramid} to create filters
     * for performing the blurring. By overriding in subclasses, you can control
     * the exact filter implementation (i.e. for speed).
     *
     * @param sigma
     *            the gaussian standard deviation
     * @return the image processor to apply the blur
     */
    public SinglebandImageProcessor<Float, FImage> createGaussianBlur(float sigma) {
        return new FGaussianConvolve(sigma);
    }

    /**
     * Get the number of pixels used for a border that processors shouldn't
     * touch.
     *
     * @return number of border pixels.
     */
    public int getBorderPixels() {
        return borderPixels;
    }

}
