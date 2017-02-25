package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.feature.ArrayFeatureVector;
import org.openimaj.feature.ByteFVComparison;
import org.openimaj.feature.DoubleFV;
import org.openimaj.util.concatenate.Concatenatable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
public class SpByteFV extends SpArrayFeatureVector<byte[]> implements SpConcatenatable<SpByteFV, SpByteFV>, Cloneable{

    /**
     * Construct an empty feature vector
     */
    public SpByteFV() {}

    /**
     * Construct empty FV with given number of bins
     * @param nbins the number of bins in each dimension
     */
    public SpByteFV(int nbins) {
        values = new byte[nbins];
    }

    /**
     * Construct from flattened values array and dimensions
     * @param values the flat array of values
     */
    public SpByteFV(byte [] values) {
        this.values = values;
    }

    /**
     * Get the element at the given flat index
     * @param x the flattened element index
     * @return the value corresponding to x
     */
    public byte get(int x) {
        return values[x];
    }

    /**
     * Set the element at the given flat index
     * @param value the value to set
     * @param x the flattened element index
     */
    void set(byte value, int x) {
        values[x] = value;
    }

    /**
     * Element-wise normalisation to 0..1 using separated expected
     * minimum and maximum values for each element of the underlying
     * feature vector.
     *
     * @param min an array containing the minimum expected values
     * @param max an array containing the maximum expected values
     * @return feature vector with each value normalised to 0..1
     */
    @Override
    public DoubleFV normaliseFV(double [] min, double [] max) {
        double [] dvals = asDoubleVector();

        for (int i=0; i<dvals.length; i++) {
            dvals[i] -= min[i];
            dvals[i] /= (max[i]-min[i]);

            if (dvals[i]<0) dvals[i] = 0;
            if (dvals[i]>1) dvals[i] = 1;
        }

        return new DoubleFV(dvals);
    }

    /**
     * Min-Max normalisation of the FV. Each element of the underlying
     * feature vector is normalised to 0..1 based on the provided
     * minimum and maximum expected values.
     *
     * @param min the minimum expected value
     * @param max the maximum expected value
     * @return feature vector with each value normalised to 0..1
     */
    @Override
    public DoubleFV normaliseFV(double min, double max) {
        double [] dvals = asDoubleVector();

        for (int i=0; i<dvals.length; i++) {
            dvals[i] -= min;
            dvals[i] /= (max-min);

            if (dvals[i]<0) dvals[i] = 0;
            if (dvals[i]>1) dvals[i] = 1;
        }

        return new DoubleFV(dvals);
    }

    /**
     * Normalise the FV to unit area.
     *
     * @return feature vector with all elements summing to 1.
     */
    @Override
    public DoubleFV normaliseFV() {
        double [] dvals = asDoubleVector();
        double sum = 0;

        for (int i=0; i<dvals.length; i++)
            sum += dvals[i];

        for (int i=0; i<dvals.length; i++)
            dvals[i] /= sum;

        return new DoubleFV(dvals);
    }

    /**
     * Lp Norm of the FV.
     *
     * @param
     *      p the norm to compute
     *
     * @return feature vector normalised using the Lp norm
     */
    @Override
    public DoubleFV normaliseFV(double p) {
        double [] dvals = asDoubleVector();
        double pnorm = 0;

        for (int i=0; i<dvals.length; i++)
            pnorm += Math.pow(Math.abs(dvals[i]), p);

        pnorm = Math.pow(pnorm, 1.0 / p);

        for (int i=0; i<dvals.length; i++)
            dvals[i] /= pnorm;

        return new DoubleFV(dvals);
    }

    @Override
    public SpByteFV clone() {
        try {
            SpByteFV model = (SpByteFV) super.clone();
            model.values = values.clone();
            return model;
        }catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public String toString() {
        String ret = this.getClass().getName() + Arrays.toString(values);

        return ret;
    }

    /**
     * Convert the FV to a DoubleFV representation
     * @return the DoubleFV representation
     */
    @Override
    public DoubleFV asDoubleFV() {
        return new DoubleFV(asDoubleVector());
    }

    /**
     * Convert the FV to a 1-dimensional double array representation
     * @return the double[] representation
     */
    @Override
    public double [] asDoubleVector() {
        double [] d = new double[values.length];

        for (int i=0; i<values.length; i++) {
            d[i] = values[i];
        }

        return d;
    }

    /**
     * Compare this FV to another with the given method.
     *
     * @param h the feature to compare against.
     * @param method the method to compare with.
     * @return a score determined by the comparison method.
     */
    public double compare(SpByteFV h, SpByteFVComparison method) {
        return method.compare(this, h);
    }

    /* (non-Javadoc)
     * @see org.openimaj.feature.FeatureVector#length()
     */
    @Override
    public int length() {
        return values.length;
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        out.writeInt(values.length);
        for (int i=0; i<values.length; i++) out.writeByte(values[i]);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        out.println(values.length);
        for (int i=0; i<values.length; i++) out.print( values[i] + " ");
        out.println();
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        int nbins = in.readInt();
        values = new byte[nbins];
        for (int i=0; i<nbins; i++) values[i] = in.readByte();
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        int nbins = Integer.parseInt(in.nextLine());
        values = new byte[nbins];
        String [] line = in.nextLine().trim().split(" ");
        for (int i=0; i<nbins; i++) values[i] = Byte.parseByte(line[i]);
    }

    @Override
    public byte[] binaryHeader() {
        return (this.getClass().getName().substring(0,2) + "FV").getBytes();
    }

    @Override
    public String asciiHeader() {
        return this.getClass().getName() + " ";
    }

    @Override
    public SpByteFV concatenate(SpByteFV... ins) {
        int l = values.length;

        for (int i=0; i<ins.length; i++)
            l += ins[i].values.length;

        byte[] data = new byte[l];

        System.arraycopy(values, 0, data, 0, values.length);
        int offset = values.length;
        for (int i=0; i<ins.length; i++) {
            System.arraycopy(ins[i].values, 0, data, offset, ins[i].values.length);
            offset += ins[i].values.length;
        }

        return new SpByteFV(data);
    }

    @Override
    public SpByteFV concatenate(List<SpByteFV> ins) {
        int l = values.length;

        for (int i=0; i<ins.size(); i++)
            l += ins.get(i).values.length;

        byte[] data = new byte[l];

        System.arraycopy(values, 0, data, 0, values.length);
        int offset = values.length;
        for (int i=0; i<ins.size(); i++) {
            System.arraycopy(ins.get(i).values, 0, data, offset, ins.get(i).values.length);
            offset += ins.get(i).values.length;
        }

        return new SpByteFV(data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public boolean equals(Object obj) {
        return Arrays.equals(values, ((SpByteFV)obj).values);
    }

    @Override
    public SpByteFV subvector(int beginIndex) {
        return new SpByteFV(Arrays.copyOfRange(this.values, beginIndex, values.length));
    }

    @Override
    public SpByteFV subvector(int beginIndex, int endIndex) {
        return new SpByteFV(Arrays.copyOfRange(this.values, beginIndex, endIndex));
    }

    @Override
    public double getAsDouble(int i) {
        return values[i];
    }

    @Override
    public void setFromDouble(int i, double v) {
        values[i] = ((byte) v);
    }

    @Override
    public SpByteFV newInstance() {
        return new SpByteFV(length());
    }

}
