package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.FImage;
import org.openimaj.image.Image;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public class SpPyramidOptions <OCTAVE extends SpOctave<?,?,IMAGE>,
        IMAGE extends Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> {


    /**
     * Should the Pyramid hold onto its octaves?
     */
    protected boolean keepOctaves = false;

    /**
     * PyramidProcessor for processing the pyramid after construction.
     * Setting a PyramidProcessor will cause the Pyramid to hold onto
     * its entire set of octaves.
     */
    protected SpPyramidProcessor<IMAGE> pyramidProcessor = null;

    /**
     * An OctaveProcessor to apply to each octave of the pyramid.
     */
    protected SpOctaveProcessor<OCTAVE,IMAGE> octaveProcessor;

    /**
     * Get an OctaveProcessor to apply to each octave of the pyramid or null
     * if none is set.
     *
     * @return the octaveProcessor or null
     */
    public SpOctaveProcessor<OCTAVE,IMAGE> getOctaveProcessor() {
        return octaveProcessor;
    }

    /**
     * Gets the currently set PyramidProcessor or null if none is set.
     *
     * PyramidProcessors process the pyramid after construction.
     * Setting a PyramidProcessor will cause the Pyramid to hold onto
     * its entire set of octaves.
     *
     * @return the pyramidProcessor or null
     */
    public SpPyramidProcessor<IMAGE> getPyramidProcessor() {
        return pyramidProcessor;
    }
}
