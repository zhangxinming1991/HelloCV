package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.processing.resize.ResizeProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public class SpGaussianPyramid<I extends org.openimaj.image.Image<?, I> & SinglebandImageProcessor.Processable<Float, FImage, I>>
        extends
        SpPyramid<SpGaussianPyramidOptions<I>, SpGaussianOctave<I>, I>
        implements
        ImageAnalyser<I>, Iterable<SpGaussianOctave<I>> {

    /**
     * Construct a Pyramid with the given options.
     *
     * @param options
     *            the options
     */
    public SpGaussianPyramid(SpGaussianPyramidOptions<I> options) {
        super(options);
    }


    /*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.openimaj.image.processing.pyramid.AbstractPyramid#process(org.openimaj
	 * .image.Image)
	 */
    @Override
    public void process(I img) {
        if (img.getWidth() <= 1 || img.getHeight() <= 1)
            throw new IllegalArgumentException("Image is too small");

        // the octave image size: 1 means same as input, 0.5 is twice as big as
        // input, 2 is half input, 4 is quarter input, etc
        float octaveSize = 1.0f;

        // if doubleInitialImage is set, then the initial image should be scaled
        // to
        // twice its original size and the
        I image;
        if (options.doubleInitialImage) {
            image = ResizeProcessor.doubleSize(img);
            octaveSize *= 0.5;
        } else
            image = img.clone();

        // Lowe's IJCV paper (P.10) suggests that if you double the size of the
        // initial image then it has a sigma of 1.0; if the image is not doubled
        // the sigma is 0.5
        final float currentSigma = (options.doubleInitialImage ? 1.0f : 0.5f);
        if (options.initialSigma > currentSigma) {
            // we now need to bring the starting image to a sigma of
            // initialSigma
            // in order to start building the pyramid (every octave starts at
            // initialSigma sigmas).
            final float sigma = (float) Math.sqrt(options.initialSigma * options.initialSigma - currentSigma
                    * currentSigma);
            image.processInplace(this.options.createGaussianBlur(sigma));
        }

        // the minimum size image in the pyramid must be bigger than
        // two pixels + whatever border is required by the options
        // (on both sides).
        final int minImageSize = 2 + (2 * options.getBorderPixels());

        while (image.getHeight() > minImageSize && image.getWidth() > minImageSize) {
            // construct empty octave
            final SpGaussianOctave<I> currentOctave = new SpGaussianOctave<I>(this, octaveSize);

            // populate the octave with images; once the octave
            // is complete any OctaveProcessor specified in the
            // options will be applied.
            currentOctave.process(image);

            // get the image with 2*sigma from the octave and
            // half its size ready for the next octave
            image = ResizeProcessor.halfSize(currentOctave.getNextOctaveImage());

            octaveSize *= 2.0; // the size of the octave increases by a factor
            // of two each iteration

            // if the octaves array is not null we want to retain each octave.
            if (octaves != null)
                octaves.add(currentOctave);
        }

        // if a PyramidProcessor was specified in the options it should
        // be applied now all the octaves are complete.
        if (options.getPyramidProcessor() != null) {
            options.getPyramidProcessor().process(this);
        }
    }
}
