package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public interface SpScaleSpacePoint extends SpPoint2d{

    /**
     * Get the scale associated with this point.
     * @return the scale.
     */
    public float getScale();

    /**
     * Set the scale associated with this point.
     * @param scale the scale to set.
     */
    public void setScale(float scale);
}
