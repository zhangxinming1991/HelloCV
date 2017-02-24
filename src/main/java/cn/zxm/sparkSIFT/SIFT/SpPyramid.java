package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.analyser.ImageAnalyser;
import org.openimaj.image.analysis.pyramid.PyramidOptions;
import org.openimaj.image.processor.SinglebandImageProcessor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by root on 17-2-23.
 */
public abstract class SpPyramid <
        OPTIONS extends SpPyramidOptions<OCTAVE,IMAGE>,
        OCTAVE extends SpOctave<OPTIONS,?,IMAGE>,
        IMAGE extends org.openimaj.image.Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>>
        implements
        ImageAnalyser<IMAGE>, Iterable<OCTAVE> {

    /**
     * Options for the pyramid
     */
    protected OPTIONS options;

    /**
     * A list of all the octaves should you want to keep them.
     * @see PyramidOptions#keepOctaves
     */
    protected List<OCTAVE> octaves;

    /**
     * Construct a Pyramid with the given options.
     * @param options the options
     */
    public SpPyramid(OPTIONS options) {
        this.options = options;

        if (options.keepOctaves || options.pyramidProcessor!=null)
            octaves = new ArrayList<OCTAVE>();
    }

    public abstract void process(IMAGE img);

    @Override
    public void analyseImage(IMAGE image) {
        process(image);
    }

    @Override
    public Iterator<OCTAVE> iterator() {
        if (octaves == null) return null;
        return octaves.iterator();
    }

}
