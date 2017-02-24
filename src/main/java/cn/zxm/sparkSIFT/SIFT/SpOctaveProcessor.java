package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public interface SpOctaveProcessor<
        OCTAVE extends
                SpOctave<?,?,IMAGE>,
        IMAGE extends
                org.openimaj.image.Image<?,IMAGE> &
                SinglebandImageProcessor.Processable<Float,FImage,IMAGE>
        >{
    /**
     * Process the provided octave.
     *
     * @param octave the octave.
     */
    public void process(OCTAVE octave);
}
