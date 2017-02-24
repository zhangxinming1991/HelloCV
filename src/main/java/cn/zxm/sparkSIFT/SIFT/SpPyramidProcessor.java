package cn.zxm.sparkSIFT.SIFT;

import org.openimaj.image.*;
import org.openimaj.image.processor.SinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public interface SpPyramidProcessor <IMAGE extends org.openimaj.image.Image<?,IMAGE> & SinglebandImageProcessor.Processable<Float,FImage,IMAGE>> {
    /**
     * Process the given pyramid.
     *
     * @param pyramid the pyramid.
     */
    public void process(SpGaussianPyramid<IMAGE> pyramid);
}
