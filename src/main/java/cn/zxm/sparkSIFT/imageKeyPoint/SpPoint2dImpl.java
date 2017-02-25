package cn.zxm.sparkSIFT.imageKeyPoint;

import Jama.Matrix;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
public class SpPoint2dImpl implements SpPoint2d, Cloneable{

    /**
     * The x-coordinate
     */
    public float x;

    /**
     * The y-coordinate
     */
    public float y;

    /**
     * Construct a Point2dImpl with the given (x, y) coordinates
     *
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     */
    public SpPoint2dImpl(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    /**
     * Construct a Point2dImpl with the (x,y) coordinates given via another
     * point.
     *
     * @param p
     *            The point to copy from.
     */
    public SpPoint2dImpl(SpPoint2d p)
    {
        this.copyFrom(p);
    }

    /**
     * Construct a Point2dImpl at the origin.
     */
    public SpPoint2dImpl()
    {
        // do nothing
    }

    /**
     * Construct a {@link SpPoint2dImpl} using the first two ordinates of a
     * {@link SpCoordinate}.
     *
     * @param coord
     *            the {@link SpCoordinate}
     */
    public SpPoint2dImpl(SpCoordinate coord) {
        x = coord.getOrdinate(0).floatValue();
        y = coord.getOrdinate(1).floatValue();
    }

    /**
     * Construct a Point2dImpl with the given (x, y) coordinates. The values
     * will be cast to single precision.
     *
     * @param x
     *            x-coordinate
     * @param y
     *            y-coordinate
     */
    public SpPoint2dImpl(double x, double y)
    {
        this.x = (float) x;
        this.y = (float) y;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public void copyFrom(SpPoint2d p)
    {
        this.x = p.getX();
        this.y = p.getY();
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }

    @Override
    public SpPoint2dImpl clone() {
        SpPoint2dImpl clone;
        try {
            clone = (SpPoint2dImpl) super.clone();
        } catch (final CloneNotSupportedException e) {
            return null;
        }
        return clone;
    }

    @Override
    public Float getOrdinate(int dimension) {
        if (dimension == 0)
            return x;
        return y;
    }

    @Override
    public int getDimensions() {
        return 2;
    }

    @Override
    public void translate(float x, float y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public void translate(SpPoint2d v) {
        this.x += v.getX();
        this.y += v.getY();
    }

    @Override
    public SpPoint2dImpl transform(Matrix transform) {
        if (transform.getRowDimension() == 3) {
            float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
                    + (float) transform.get(0, 2);
            float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
                    + (float) transform.get(1, 2);
            final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
                    + (float) transform.get(2, 2);

            xt /= zt;
            yt /= zt;

            return new SpPoint2dImpl(xt, yt);
        } else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 2) {
            final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY();
            final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY();

            return new SpPoint2dImpl(xt, yt);
        } else if (transform.getRowDimension() == 2 && transform.getColumnDimension() == 3) {
            final float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
                    + (float) transform.get(0, 2);
            final float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
                    + (float) transform.get(1, 2);

            return new SpPoint2dImpl(xt, yt);
        }
        throw new IllegalArgumentException("Transform matrix has unexpected size");
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SpPoint2d))
            return false;
        final SpPoint2d p = (SpPoint2d) o;
        return p.getX() == this.x && p.getY() == this.y;
    }

    @Override
    public int hashCode()
    {
        return toString().hashCode();
    }

    @Override
    public SpPoint2d minus(SpPoint2d a) {
        return new SpPoint2dImpl(this.x - a.getX(), this.y - a.getY());
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        x = in.nextFloat();
        y = in.nextFloat();
    }

    @Override
    public String asciiHeader() {
        return "Point2d";
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public byte[] binaryHeader() {
        return "PT2D".getBytes();
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        out.format("%f %f", x, y);
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        out.writeFloat(x);
        out.writeFloat(y);
    }

    @Override
    public SpPoint2dImpl copy() {
        return clone();
    }

    /**
     * Create a random point in ([0..1], [0..1]).
     *
     * @return random point.
     */
    public static SpPoint2d createRandomPoint() {
        return new SpPoint2dImpl((float) Math.random(), (float) Math.random());
    }

    /**
     * Create a random point in ([0..1], [0..1]) with the given random number
     * generator.
     *
     * @param rng
     *            the random number generator
     * @return random point.
     */
    public static SpPoint2d createRandomPoint(Random rng) {
        return new SpPoint2dImpl(rng.nextFloat(), rng.nextFloat());
    }

    /**
     * @param calculateCentroid
     * @return a point from a double array
     */
    public static SpPoint2d fromDoubleArray(double[] calculateCentroid) {
        return new SpPoint2dImpl((float) calculateCentroid[0], (float) calculateCentroid[1]);
    }

    @Override
    public void setOrdinate(int dimension, Number value) {
        if (dimension == 0)
            x = value.floatValue();
        if (dimension == 1)
            y = value.floatValue();
    }
}
