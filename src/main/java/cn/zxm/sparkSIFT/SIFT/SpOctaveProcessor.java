package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-23.
 */
public interface SpOctaveProcessor<
        OCTAVE extends
                SpOctave<?,?,IMAGE>,
        IMAGE extends
                SpImage<?,IMAGE> &
                SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>
        >{
    /**
     * Process the provided octave.
     *
     * @param octave the octave.
     */
    public void process(OCTAVE octave);
}
