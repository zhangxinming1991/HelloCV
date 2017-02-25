package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-24.
 */
public interface SpPyramidProcessor <IMAGE extends SpImage<?,IMAGE> & SpSinglebandImageProcessor.Processable<Float,SpFImage,IMAGE>> {
    /**
     * Process the given pyramid.
     *
     * @param pyramid the pyramid.
     */
    public void process(SpGaussianPyramid<IMAGE> pyramid);
}
