package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-25.
 */
public interface SpSinglebandKernelProcessor<T, I extends SpImage<T,I>> extends SpKernelProcessor<T, I> {

    /**
     * Interfaces for objects that allow themselves to be processed by a
     * SinglebandKernelProcessor.
     *
     * @param <T> The pixel type that is processed
     * @param <S> The image type of the underlying single band images
     * @param <I> The image type that is processed
     */
    public interface Processable<T, S extends SpImage<T,S>, I extends SpImage<?,I>> {
        /**
         * @see Image#process(SpKernelProcessor)
         * @param p the processor
         * @return the processed image
         */
        public I process(SpSinglebandKernelProcessor<T,S> p);

        /**
         * @see Image#process(SpKernelProcessor)
         * @param p the processor
         * @return the processed image
         */
        public I processInplace(SpSinglebandKernelProcessor<T,S> p);

        /**
         * @see Image#process(SpKernelProcessor, boolean)
         * @param p the processor
         * @param pad Should the image be zero padded so the
         * 				kernel reaches the edges of the output
         * @return the processed image
         */
        public I process(SpSinglebandKernelProcessor<T,S> p, boolean pad);

        /**
         * @see Image#processInplace(SpKernelProcessor, boolean)
         * @param p the processor
         * @param pad Should the image be zero padded so the
         * 				kernel reaches the edges of the output
         * @return the processed image
         */
        public I processInplace(SpSinglebandKernelProcessor<T,S> p, boolean pad);
    }

}
