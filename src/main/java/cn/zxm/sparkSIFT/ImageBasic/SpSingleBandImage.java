package cn.zxm.sparkSIFT.ImageBasic;


/**
 * Created by root on 17-2-22.
 */
public abstract class SpSingleBandImage<Q extends Comparable<Q>, I extends SpSingleBandImage<Q, I>>
        extends
        SpImage<Q, I>
        implements
        SpSinglebandImageProcessor.Processable<Q, I, I>,
        SpSinglebandKernelProcessor.Processable<Q, I, I>{

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
    @Override
    public I process(SpKernelProcessor<Q, I> p) {
        return process(p, false);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#process(org.openimaj.image.processor.KernelProcessor,
     *      boolean)
     */
    @Override
    public I process(SpKernelProcessor<Q, I> p, boolean pad) {
        final I newImage = newInstance(width, height);
        final I tmp = newInstance(p.getKernelWidth(), p.getKernelHeight());

        final int hh = p.getKernelHeight() / 2;
        final int hw = p.getKernelWidth() / 2;

        if (!pad) {
            for (int y = hh; y < getHeight() - (p.getKernelHeight() - hh); y++) {
                for (int x = hw; x < getWidth() - (p.getKernelWidth() - hw); x++) {
                    newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
                }
            }
        } else {
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
                }
            }
        }

        return newImage;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor)
     */
    @Override
    public I process(SpSinglebandKernelProcessor<Q, I> p) {
        return process(p, false);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#process(org.openimaj.image.processor.SinglebandKernelProcessor,
     *      boolean)
     */
    @Override
    public I process(SpSinglebandKernelProcessor<Q, I> p, boolean pad) {
        final I newImage = newInstance(width, height);
        final I tmp = newInstance(p.getKernelWidth(), p.getKernelHeight());

        final int hh = p.getKernelHeight() / 2;
        final int hw = p.getKernelWidth() / 2;

        if (!pad) {
            for (int y = hh; y < getHeight() - (p.getKernelHeight() - hh); y++) {
                for (int x = hw; x < getWidth() - (p.getKernelWidth() - hw); x++) {
                    newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
                }
            }
        } else {
            for (int y = 0; y < getHeight(); y++) {
                for (int x = 0; x < getWidth(); x++) {
                    newImage.setPixel(x, y, p.processKernel(this.extractROI(x, y, tmp)));
                }
            }
        }

        return newImage;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor)
     */
    @Override
    public I processInplace(SpSinglebandKernelProcessor<Q, I> p) {
        return processInplace(p, false);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandKernelProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandKernelProcessor,
     *      boolean)
     */
    @Override
    @SuppressWarnings("unchecked")
    public I processInplace(SpSinglebandKernelProcessor<Q, I> p, boolean pad) {
        final I newImage = process(p, pad);
        this.internalAssign(newImage);
        return (I) this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#process(org.openimaj.image.processor.SinglebandImageProcessor)
     */
    @Override
    public I process(SpSinglebandImageProcessor<Q, I> p) {
        final I newImage = this.clone();
        newImage.processInplace(p);
        return newImage;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.processor.SinglebandImageProcessor.Processable#processInplace(org.openimaj.image.processor.SinglebandImageProcessor)
     */
    @SuppressWarnings("unchecked")
    @Override
    public I processInplace(SpSinglebandImageProcessor<Q, I> p) {
        p.processImage((I) this);
        return (I) this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#fill(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    @Override
    public I fill(Q colour) {
        for (int y = 0; y < getHeight(); y++)
            for (int x = 0; x < getWidth(); x++)
                this.setPixel(x, y, colour);

        return (I) this;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openimaj.image.Image#clone()
     */
    @Override
    public abstract I clone();

    @Override
    public boolean equals(Object obj) {
        @SuppressWarnings("unchecked")
        final I that = (I) obj;
        for (int y = 0; y < getHeight(); y++)
            for (int x = 0; x < getWidth(); x++)
            {
                final boolean fail = !this.getPixel(x, y).equals(that.getPixel(x, y));
                if (fail)
                    return false;
            }

        return true;
    }

}
