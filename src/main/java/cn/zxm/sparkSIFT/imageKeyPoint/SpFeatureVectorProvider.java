package cn.zxm.sparkSIFT.imageKeyPoint;


/**
 * Created by root on 17-2-25.
 */
public interface SpFeatureVectorProvider <T extends SpFeatureVector>{

    /**
     * Get the FeatureVector associated with this object.
     *
     * @return the feature vector.
     */
    T getFeatureVector();
}
