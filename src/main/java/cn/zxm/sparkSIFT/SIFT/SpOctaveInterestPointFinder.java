package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public interface SpOctaveInterestPointFinder<OCTAVE extends SpOctave<?,?,IMAGE>,
        IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>>
        extends
        SpOctaveProcessor<OCTAVE,IMAGE>  {

    /**
     * Get the octave from which we are operating
     *
     * @return the octave
     */
    public OCTAVE getOctave();

    /**
     * Set a listener object that will listen to events triggered when interest
     * points are detected.
     *
     * @param listener the listener
     */
    public void setOctaveInterestPointListener(SpOctaveInterestPointListener<OCTAVE, IMAGE> listener);

    /**
     * Get the current scale index within the octave. Index 0 is the
     * first level of the octave.
     *
     * @return the current scale index.
     */
    public int getCurrentScaleIndex();

    /**
     * Get the current listener object.
     *
     * @return the listener, or null if none set.
     */
    public SpOctaveInterestPointListener<OCTAVE,IMAGE> getOctaveInterestPointListener();
}
