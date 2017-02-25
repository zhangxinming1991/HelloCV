package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.feature.DoubleFV;
import org.openimaj.io.ReadWriteable;

import java.io.Serializable;

/**
 * Created by root on 17-2-25.
 */
public interface SpFeatureVector extends Cloneable, Serializable, ReadWriteable {

    /**
     * Get the underlying data array.
     *
     * @return underlying data
     */
    public Object getVector();

    /**
     * Get the length of this vector
     *
     * @return the length of this vector
     */
    public int length();

    /**
     * Element-wise normalisation to 0..1 using separated expected minimum and
     * maximum values for each element of the underlying feature vector.
     *
     * @param min
     *            an array containing the minimum expected values
     * @param max
     *            an array containing the maximum expected values
     * @return copy of the feature vector with each value normalised to 0..1
     */
    public DoubleFV normaliseFV(double[] min, double[] max);

    /**
     * Min-Max normalisation of the FV. Each element of the underlying feature
     * vector is normalised to 0..1 based on the provided minimum and maximum
     * expected values.
     *
     * @param min
     *            the minimum expected value
     * @param max
     *            the maximum expected value
     * @return copy of the feature vector with each value normalised to 0..1
     */
    public DoubleFV normaliseFV(double min, double max);

    /**
     * Normalise the FV to unit length
     *
     * @return a copy of the feature vector as a DoubleFV, normalised to unit
     *         length
     */
    public DoubleFV normaliseFV();

    /**
     * Convert the FV to a DoubleFV representation
     *
     * @return a copy of the feature vector as a DoubleFV
     */
    public DoubleFV asDoubleFV();

    /**
     * Convert the FV to a 1-dimensional double array representation
     *
     * @return a copy of the feature vector as a double array
     */
    public double[] asDoubleVector();

    /**
     * Lp Norm of the FV.
     *
     * @param p
     *            the norm to compute
     *
     * @return feature vector normalised using the Lp norm
     */
    public DoubleFV normaliseFV(double p);

    /**
     * Get an element of the feature as a double value
     *
     * @param i
     *            the element index
     * @return the value as a double
     */
    public double getAsDouble(int i);

    /**
     * Set an element of the feature from a double value
     *
     * @param i
     *            the element index
     * @param v
     *            the value
     */
    public void setFromDouble(int i, double v);

    /**
     * Construct a new instance of this featurevector. Implementors must return
     * an instance of themselves (rather than a different type of feature).
     *
     * @return a new instance.
     */
    public SpFeatureVector newInstance();

}
