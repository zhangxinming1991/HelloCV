package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-25.
 */
public interface SpSinglebandImageProcessor<T, S extends SpImage<T, S>>
        extends SpImageProcessor<S> {

    /**
     * An interface for {@link SpImage}s that are processable by
     * {@link SpSinglebandImageProcessor}s.
     *
     * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
     * @param <T>
     *            The type of pixel in the image
     * @param <S>
     *            The concrete subclass of the single band image
     * @param <I>
     *            The type of image that is returned after processing
     */
    public interface Processable<T, S extends SpImage<T, S>, I extends SpImage<?, I>>
    {
        /**
         * Process with the given {@link SpSinglebandImageProcessor} returning a
         * new image.
         *
         * @param p
         *            The processor to process the image with
         * @return A new image containing the result.
         */
        public I process(SpSinglebandImageProcessor<T, S> p);

        /**
         * Process with the given {@link SpSinglebandImageProcessor} storing the
         * result in this processable image. Side-affects this processable
         * image.
         *
         * @param p
         *            The processor to process the image with
         * @return A new image containing the result.
         */
        public I processInplace(SpSinglebandImageProcessor<T, S> p);
    }

}
