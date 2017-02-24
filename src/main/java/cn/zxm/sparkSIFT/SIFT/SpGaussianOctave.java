package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.processor.SinglebandImageProcessor;

import java.lang.reflect.Array;

/**
 * Created by root on 17-2-23.
 */
public class SpGaussianOctave<IMAGE extends org.openimaj.image.Image<?, IMAGE> & SinglebandImageProcessor.Processable<Float, FImage, IMAGE>>
        extends
        SpOctave<SpGaussianPyramidOptions<IMAGE>, SpGaussianPyramid<IMAGE>, IMAGE> {

    /**
     * Construct a Gaussian octave with the provided parent Pyramid and
     * octaveSize. The octaveSize parameter is the size of the octave's images
     * compared to the original image used to construct the pyramid. An
     * octaveSize of 1 means the same size as the original, 2 means half size, 4
     * means quarter size, etc.
     *
     * @param parent
     *            the pyramid that this octave belongs to
     * @param octaveSize
     *            the size of the octave relative to the original image.
     */
    public SpGaussianOctave(SpGaussianPyramid<IMAGE> parent, float octaveSize) {
        super(parent, octaveSize);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void process(IMAGE image) {
        images = (IMAGE[]) Array.newInstance(image.getClass(), options.scales + options.extraScaleSteps + 1);

        // we want to each level to be separated by a constant factor
        // k=2^(1/scales)
        final float k = (float) Math.pow(2.0, 1.0 / options.scales);

        // image[0] of the octave is the input image
        images[0] = image;

        // the intial (input) image is considered to have sigma initialSigma.
        float prevSigma = options.initialSigma;

        for (int i = 1; i < options.scales + options.extraScaleSteps + 1; i++) {
            images[i] = images[i - 1].clone();

            // compute the amount to increase from prevSigma to prevSigma*k
            final float increase = prevSigma * (float) Math.sqrt(k * k - 1.0);

            images[i].processInplace(options.createGaussianBlur(increase));

            prevSigma *= k;
        }

        // if a processor is defined, apply it
        if (options.getOctaveProcessor() != null)
            options.getOctaveProcessor().process(this);
    }

    /*
 * (non-Javadoc)
 *
 * @see
 * org.openimaj.image.processing.pyramid.AbstractOctave#getNextOctaveImage()
 */
    @Override
    public IMAGE getNextOctaveImage() {
        return images[options.scales];
    }
}
