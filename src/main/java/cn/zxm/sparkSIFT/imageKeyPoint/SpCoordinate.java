package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.io.ReadWriteable;

/**
 * Created by root on 17-2-25.
 */
public interface SpCoordinate extends ReadWriteable {
    /**
     * Get the ordinate value for a specific dimension.
     *
     * @param dimension
     *            The index of the dimension we are interested in
     * @return The value of the ordinate of the given dimension.
     * @exception IllegalArgumentException
     *                if the Coordinate does not support the dimension.
     */
    public Number getOrdinate(int dimension);

    /**
     * Set the ordinate value for a specific dimension.
     *
     * @param dimension
     *            The index of the dimension we are interested in
     * @param value
     *            The value of the ordinate of the given dimension.
     * @exception IllegalArgumentException
     *                if the Coordinate does not support the dimension.
     */
    public void setOrdinate(int dimension, Number value);

    /**
     * @return The number of dimensions in the coordinate.
     */
    public int getDimensions();
}
