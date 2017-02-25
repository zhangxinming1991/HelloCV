package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;


/**
 * Created by root on 17-2-23.
 */
public class SpGaussianPyramidOptions <IMAGE extends SpImage<?, IMAGE> & SpSinglebandImageProcessor.Processable<Float, SpFImage, IMAGE>>
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
     * Create a {@link SpSinglebandImageProcessor} that performs a Gaussian
     * blurring with a standard deviation given by sigma. This method is used by
     * the {@link SpGaussianOctave} and {@link SpGaussianPyramid} to create filters
     * for performing the blurring. By overriding in subclasses, you can control
     * the exact filter implementation (i.e. for speed).
     *
     * @param sigma
     *            the gaussian standard deviation
     * @return the image processor to apply the blur
     */
    public SpSinglebandImageProcessor<Float, SpFImage> createGaussianBlur(float sigma) {
        return new SpFGaussianConvolve(sigma);
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

    /**
     * Get the number of scales in this octave minus extraScaleSteps. Levels of
     * each octave are constructed so that level[scales] has twice the sigma of
     * level[0].
     *
     * @return the scales
     */
    public int getScales() {
        return scales;
    }

    /**
     * Get the number of extra scale steps taken beyond scales.
     *
     * @see #getScales()
     *
     * @return the extraScaleSteps
     */
    public int getExtraScaleSteps() {
        return extraScaleSteps;
    }

    /**
     * Get the assumed initial scale of the first image in each octave.
     *
     * @return the initialSigma
     */
    public float getInitialSigma() {
        return initialSigma;
    }

}
