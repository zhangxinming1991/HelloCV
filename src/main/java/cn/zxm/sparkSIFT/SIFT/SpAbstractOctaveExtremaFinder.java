package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;

/**
 * Created by root on 17-2-24.
 */
public abstract class SpAbstractOctaveExtremaFinder<OCTAVE extends SpGaussianOctave<SpFImage>>
        extends
        SpAbstractOctaveInterestPointFinder<OCTAVE, SpFImage> {

    /** The default threshold for the edge response Eigenvalue ratio */
    public static final float DEFAULT_EIGENVALUE_RATIO = 10.0f;

    // Threshold on the ratio of the Eigenvalues of the Hessian matrix (Lowe
    // IJCV, p.12)
    protected float eigenvalueRatio = DEFAULT_EIGENVALUE_RATIO;

    /**
     * Construct an AbstractOctaveExtremaFinder with the given Eigenvalue ratio
     * threshold.
     *
     * @param eigenvalueRatio
     */
    public SpAbstractOctaveExtremaFinder(float eigenvalueRatio) {
        this.eigenvalueRatio = eigenvalueRatio;
    }

    @Override
    public OCTAVE getOctave() {
        return octave;
    }

    @Override
    public void process(OCTAVE octave) {
        beforeProcess(octave);

        this.octave = octave;

        final SpFImage[] images = octave.images;
        final int height = images[0].height;
        final int width = images[0].width;
        final int borderDist = octave.options.getBorderPixels();

        // search through the scale-space images, leaving a border
        for (currentScaleIndex = 1; currentScaleIndex < images.length - 1; currentScaleIndex++) {
            for (int y = borderDist; y < height - borderDist; y++) {
                for (int x = borderDist; x < width - borderDist; x++) {
                    final float val = images[currentScaleIndex].pixels[y][x];

                    if (firstCheck(val, x, y, currentScaleIndex, images) &&
                            isLocalExtremum(val, images[currentScaleIndex - 1], x, y) &&
                            isLocalExtremum(val, images[currentScaleIndex], x, y) &&
                            isLocalExtremum(val, images[currentScaleIndex + 1], x, y) &&
                            isNotEdge(images[currentScaleIndex], x, y))
                    {
                        processExtrema(images, currentScaleIndex, x, y, octave.octaveSize);
                    }
                }
            }
        }
    }

    /**
     * Called at the start of
     * {@link SpAbstractOctaveExtremaFinder#process(OCTAVE)}
     *
     * @param octave
     *            the octave being processed
     */
    protected void beforeProcess(OCTAVE octave) {
        // do nothing
    }

    protected boolean firstCheck(float val, int x, int y, int scaleIndex, SpFImage[] images) {
        return true;
    }

    /**
     * Test to see if a point is a local extremum by searching the +/- 1 pixel
     * neighbourhood in x and y.
     *
     * @param val
     *            the value at x,y
     * @param image
     *            the image to test against
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @return true if extremum, false otherwise.
     */
    protected boolean isLocalExtremum(float val, SpFImage image, int x, int y) {
        final float pix[][] = image.pixels;

        if (val > 0.0) {
            for (int yy = y - 1; yy <= y + 1; yy++)
                for (int xx = x - 1; xx <= x + 1; xx++)
                    if (pix[yy][xx] > val)
                        return false;
        } else {
            for (int yy = y - 1; yy <= y + 1; yy++)
                for (int xx = x - 1; xx <= x + 1; xx++)
                    if (pix[yy][xx] < val)
                        return false;
        }
        return true;
    }

    /**
     * Test if the pixel at x,y in the image is NOT on an edge.
     *
     * @param image
     *            the image
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     * @return true if the pixel is not an edge, false otherwise
     */
    protected boolean isNotEdge(SpFImage image, int x, int y) {
        final float pix[][] = image.pixels;

        // estimate Hessian from finite differences
        final float H00 = pix[y - 1][x] - 2.0f * pix[y][x] + pix[y + 1][x];
        final float H11 = pix[y][x - 1] - 2.0f * pix[y][x] + pix[y][x + 1];
        final float H01 = ((pix[y + 1][x + 1] - pix[y + 1][x - 1]) - (pix[y - 1][x + 1] - pix[y - 1][x - 1])) / 4.0f;

        // determinant and trace of Hessian
        final float det = H00 * H11 - H01 * H01;
        final float trace = H00 + H11;

        final float eigenvalueRatio1 = eigenvalueRatio + 1.0f;

        return (det * eigenvalueRatio1 * eigenvalueRatio1 > eigenvalueRatio * trace * trace);
    }

    /**
     * Perform any additional checks on the point, and then inform the listener
     * that a point has been found.
     *
     * @param images
     *            the stack of images in this octave
     * @param s
     *            the interest-point scale
     * @param x
     *            the x-coordinate of the interest-point
     * @param y
     *            the y-coordinate of the interest-point
     * @param octSize
     */
    protected abstract void processExtrema(SpFImage[] images, int s, int x, int y, float octSize);

}
