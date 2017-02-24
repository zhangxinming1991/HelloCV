package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-22.
 */
public abstract class SpSingleBandImage<Q extends Comparable<Q>, I extends SpSingleBandImage<Q, I>>
        extends
        SpImage<Q, I> {
    private static final long serialVersionUID = 1L;

    /** The image height */
    public int height;

    /** The image width */
    public int width;

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#getHeight()
     */
    @Override
    public int getHeight() {
        return height;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#getWidth()
     */
    @Override
    public int getWidth() {
        return width;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#process(org.openimaj.image.processor.KernelProcessor)
     */
}
