package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public interface SpOctaveInterestPointListener
        <OCTAVE extends SpOctave<?,?,IMAGE>,
        IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>>  {

    /**
     * Do something with a detected interest point.
     *
     * @param finder the finder that found the point
     * @param x the x position
     * @param y the y position
     * @param octaveScale the scale within the octave
     */
    public void foundInterestPoint(SpOctaveInterestPointFinder<OCTAVE, IMAGE> finder, float x, float y, float octaveScale);
}
