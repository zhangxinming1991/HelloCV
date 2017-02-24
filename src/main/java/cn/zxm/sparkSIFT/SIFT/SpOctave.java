package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.Image;
import org.openimaj.image.FImage;
import org.openimaj.image.processor.SinglebandImageProcessor;
import org.openimaj.util.array.ArrayIterator;

import java.util.Iterator;

/**
 * Created by root on 17-2-23.
 */
public abstract class SpOctave
        <OPTIONS extends SpPyramidOptions<?, IMAGE>,
        PYRAMID extends SpPyramid<OPTIONS,?,IMAGE>,
        IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>>
        implements
        Iterable<IMAGE> {
    /** The options used for the pyramid construction */
    public OPTIONS options;

    /** The images that make up this Octave */
    public IMAGE [] images;

    /** The pyramid that contains this Octave */
    public PYRAMID parentPyramid;

    /** The size of the octave relative to the original image. */
    public float octaveSize;

    /**
     * Construct a Gaussian octave with the provided parent Pyramid
     * and octaveSize. The octaveSize parameter is the size of
     * the octave's images compared to the original image used
     * to construct the pyramid. An octaveSize of 1 means the
     * same size as the original, 2 means half size, 4 means
     * quarter size, etc.
     *
     * @param parent the pyramid that this octave belongs to
     * @param octaveSize the size of the octave relative to
     * 			the original image.
     */
    public SpOctave(PYRAMID parent, float octaveSize) {
        parentPyramid = parent;
        if (parent != null) this.options = parent.options;
        this.octaveSize = octaveSize;
    }

    /**
     * Populate the octave, starting from the provided image.
     * @param image the image.
     */
    public abstract void process(IMAGE image);

    /**
     * Get the image that starts the next octave.
     * Usually this is the image that has twice the sigma
     * of the image used to initialise this octave.
     *
     * @return image image to start next octave.
     */
    public abstract IMAGE getNextOctaveImage();

    /* (non-Javadoc)
 * @see java.lang.Iterable#iterator()
 */
    @Override
    public Iterator<IMAGE> iterator() {
        return new ArrayIterator<IMAGE>(images);
    }


}
