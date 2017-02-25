package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public abstract class SpAbstractOctaveInterestPointFinder<
        OCTAVE extends SpOctave<?,?,IMAGE>,
        IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>>
        implements
        SpOctaveInterestPointFinder<OCTAVE, IMAGE>  {

    /**
     * The index of the scale currently being processed within the octave.
     * This should be changed as the finder progresses through the scales.
     */
    protected int currentScaleIndex;

    /**
     * The listener object that gets informed when interest points are detected.
     */
    protected SpOctaveInterestPointListener<OCTAVE, IMAGE> listener;

    /* (non-Javadoc)
 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#getCurrentScaleIndex()
 */

    /**
     * The current octave.
     */
    protected OCTAVE octave;

    @Override
    public OCTAVE getOctave() {
        return octave;
    }

    @Override
    public int getCurrentScaleIndex() {
        return currentScaleIndex;
    }

    /* (non-Javadoc)
 * @see dogsiftdevel.pyramid.OctaveInterestPointFinder#setInterestPointListener(dogsiftdevel.pyramid.OctaveInterestPointListener)
 */
    @Override
    public void setOctaveInterestPointListener(SpOctaveInterestPointListener<OCTAVE, IMAGE> listener) {
        this.listener = listener;
    }

    @Override
    public SpOctaveInterestPointListener<OCTAVE, IMAGE> getOctaveInterestPointListener() {
        return listener;
    }

}
