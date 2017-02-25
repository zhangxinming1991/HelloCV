package cn.zxm.sparkSIFT.imageKeyPoint;

import Jama.Matrix;

/**
 * Created by root on 17-2-25.
 */
public interface SpPoint2d extends SpCoordinate{
    /**
     * @return x coordinate of point
     */
    public float getX();

    /**
     * Set x coordinate of point
     *
     * @param x
     *            x-coordinate
     */
    public void setX(float x);

    /**
     * @return y coordinate of point
     */
    public float getY();

    /**
     * Set y coordinate of point
     *
     * @param y
     *            y-coordinate
     */
    public void setY(float y);

    /**
     * Copy the values of the given point into this point.
     *
     * @param p
     *            The point to copy values from.
     */
    public void copyFrom(SpPoint2d p);

    /**
     * Clone the point
     *
     * @return a copy of the point
     */
    public SpPoint2d copy();

    /**
     * Translate the position of the point by the given amounts
     *
     * @param x
     *            x-amount
     * @param y
     *            y-amount
     */
    public void translate(float x, float y);

    /**
     * Transform the point by the given matrix
     *
     * @param m
     * @return a copy
     */
    public SpPoint2d transform(Matrix m);

    /**
     * Take point point from another point such that return = this - a
     *
     * @param a
     * @return a new point
     */
    public SpPoint2d minus(SpPoint2d a);

    /**
     * Translate the position of the point by the given amounts
     *
     * @param v
     *            the vector to translate by
     */
    void translate(SpPoint2d v);

}
