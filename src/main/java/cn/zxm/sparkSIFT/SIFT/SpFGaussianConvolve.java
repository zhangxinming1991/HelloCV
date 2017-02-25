package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-25.
 */
public class SpFGaussianConvolve implements SpSinglebandImageProcessor<Float, SpFImage> {

    /**
     * The default number of sigmas at which the Gaussian function is truncated
     * when building a kernel
     */
    public static final float DEFAULT_GAUSS_TRUNCATE = 4.0f;

    protected float[] kernel;

    /**
     * Construct an {@link SpFGaussianConvolve} with a Gaussian of standard
     * deviation sigma.
     *
     * @param sigma
     *            Gaussian kernel standard deviation
     */
    public SpFGaussianConvolve(float sigma) {
        this(sigma, DEFAULT_GAUSS_TRUNCATE);
    }

    /**
     * Construct an {@link SpFGaussianConvolve} with a Gaussian of standard
     * deviation sigma. The truncate parameter defines how many sigmas wide the
     * kernel is.
     *
     * @param sigma
     * @param truncate
     */
    public SpFGaussianConvolve(float sigma, float truncate) {
        kernel = makeKernel(sigma, truncate);
    }

    /**
     * Construct a zero-mean Gaussian with the specified standard deviation.
     *
     * @param sigma
     *            the standard deviation of the Gaussian
     * @return an array representing a Gaussian function
     */
    public static float[] makeKernel(float sigma) {
        return makeKernel(sigma, DEFAULT_GAUSS_TRUNCATE);
    }

    /**
     * Construct a zero-mean Gaussian with the specified standard deviation.
     *
     * @param sigma
     *            the standard deviation of the Gaussian
     * @param truncate
     *            the number of sigmas from the centre at which to truncate the
     *            Gaussian
     * @return an array representing a Gaussian function
     */
    public static float[] makeKernel(float sigma, float truncate) {
        if (sigma == 0)
            return new float[] { 1f };
        // The kernel is truncated at truncate sigmas from center.
        int ksize = (int) (2.0f * truncate * sigma + 1.0f);
        // ksize = Math.max(1, ksize); // size must be at least 3
        if (ksize % 2 == 0)
            ksize++; // size must be odd

        final float[] kernel = new float[ksize];

        // build kernel
        float sum = 0.0f;
        for (int i = 0; i < ksize; i++) {
            final float x = i - ksize / 2;
            kernel[i] = (float) Math.exp(-x * x / (2.0 * sigma * sigma));
            sum += kernel[i];
        }

        // normalise area to 1
        for (int i = 0; i < ksize; i++) {
            kernel[i] /= sum;
        }

        return kernel;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
     * .image.Image)
     */
    @Override
    public void processImage(SpFImage image) {
        SpFImageConvolveSeparable.convolveHorizontal(image, kernel);
        SpFImageConvolveSeparable.convolveVertical(image, kernel);
    }
}
