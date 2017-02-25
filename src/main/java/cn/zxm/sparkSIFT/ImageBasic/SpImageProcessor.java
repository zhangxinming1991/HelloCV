package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-25.
 */
public interface SpImageProcessor<I extends SpImage<?, I>> extends SpProcessor<I> {

    /**
     * Process an image. Implementing classes must alter the image passed
     * in-place or assign the output to the input using
     * {@link Image#internalAssign(Image)}.
     *
     * @param image
     *            The image to process in place.
     */
    public abstract void processImage(I image);
}
