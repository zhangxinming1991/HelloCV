package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;

/**
 * Created by root on 17-2-24.
 */
public class SpDoGOctaveExtremaFinder	implements
        SpOctaveInterestPointFinder<SpGaussianOctave<SpFImage>, SpFImage>,
        SpOctaveInterestPointListener<SpGaussianOctave<SpFImage>, SpFImage> {

    SpDoGOctave<SpFImage> dogOctave;	//a difference-of-Gaussian octave constructed from the Gaussian one
    SpOctaveInterestPointFinder<SpGaussianOctave<SpFImage>, SpFImage> innerFinder; //the finder that is applied to the DoG
    SpOctaveInterestPointListener<SpGaussianOctave<SpFImage>, SpFImage> listener; //a listener that is fired as interest points are detected

    SpGaussianOctave<SpFImage> gaussianOctave; //the Gaussian octave

    /**
     * Construct with the given finder.
     * @param finder the finder
     */
    public SpDoGOctaveExtremaFinder(SpOctaveInterestPointFinder<SpGaussianOctave<SpFImage>, SpFImage> finder) {
        this.innerFinder = finder;

        finder.setOctaveInterestPointListener(this);
    }

    @Override
    public void process(SpGaussianOctave<SpFImage> octave) {
        gaussianOctave = octave;

        dogOctave = new SpDoGOctave<SpFImage>(octave.parentPyramid, octave.octaveSize);
        dogOctave.process(octave);

        innerFinder.process(dogOctave);
    }

    @Override
    public SpGaussianOctave<SpFImage> getOctave() {
        return gaussianOctave;
    }

    @Override
    public void setOctaveInterestPointListener(SpOctaveInterestPointListener<SpGaussianOctave<SpFImage>, SpFImage> listener) {
        this.listener = listener;
    }

    @Override
    public SpOctaveInterestPointListener<SpGaussianOctave<SpFImage>, SpFImage> getOctaveInterestPointListener() {
        return listener;
    }

    @Override
    public int getCurrentScaleIndex() {
        return innerFinder.getCurrentScaleIndex();
    }

    @Override
    public void foundInterestPoint(SpOctaveInterestPointFinder<SpGaussianOctave<SpFImage>, SpFImage> finder, float x, float y, float octaveScale) {
        if (listener != null) listener.foundInterestPoint(this, x, y, octaveScale);
    }


}
