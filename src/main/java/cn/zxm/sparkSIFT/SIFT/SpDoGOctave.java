package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

import java.lang.reflect.Array;

/**
 * Created by root on 17-2-24.
 */
public class SpDoGOctave<
        I extends SpImage<?,I> & SpSinglebandImageProcessor.Processable<Float,SpFImage,I>>
        extends
        SpGaussianOctave<I> implements SpOctaveProcessor<SpGaussianOctave<I>, I> {

    /**
     * Construct a Difference of Gaussian octave with the provided parent Pyramid
     * and octaveSize. The octaveSize parameter is the size of
     * the octave's images compared to the original image used
     * to construct the pyramid. An octaveSize of 1 means the
     * same size as the original, 2 means half size, 4 means
     * quarter size, etc.
     *
     * @param parent the pyramid that this octave belongs to
     * @param octSize the size of the octave relative to
     * 			the original image.
     */
    public SpDoGOctave(SpGaussianPyramid<I> parent, float octSize) {
        super(parent, octSize);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void process(SpGaussianOctave<I> octave) {
        images = (I[]) Array.newInstance(octave.images[0].getClass(), options.getScales() + options.getExtraScaleSteps());

        //compute DoG by subtracting adjacent levels
        for (int i = 0; i < images.length; i++) {
            images[i] = octave.images[i].clone();
            images[i].subtractInplace(octave.images[i + 1]);
        }
    }
}
