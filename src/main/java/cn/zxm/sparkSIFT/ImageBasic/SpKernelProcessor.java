package cn.zxm.sparkSIFT.ImageBasic;

import org.openimaj.image.processor.Processor;

/**
 * Created by root on 17-2-25.
 */
public interface SpKernelProcessor<Q, I extends SpImage<Q,I>> extends SpProcessor<I> {
    /**
     * 	Get the height of the kernel required by this processor.
     *
     *  @return The height of the kernel required by this processor
     */
    public abstract int getKernelHeight();

    /**
     * 	Get the width of the kernel required by this processor.
     *
     *  @return The width of the kernel required by this processor.
     */
    public abstract int getKernelWidth();

    /**
     * 	Process the patch with this kernel processor and return a value
     * 	that will be used to build the convolved image.
     *
     *  @param patch The patch of pixels from the image to process
     *  @return A value to place in the final convolved image.
     */
    public abstract Q processKernel(I patch);
}
