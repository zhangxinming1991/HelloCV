package cn.zxm.sparkSIFT.imageKeyPoint;

import Jama.Matrix;
import cern.jet.random.Normal;
import org.openimaj.feature.ByteFV;
import org.openimaj.io.VariableLength;

import java.io.*;
import java.util.*;

/**
 * Created by root on 17-2-25.
 */
public class SpKeypoint implements
        Serializable,
        SpScaleSpacePoint,
        SpLocalFeature<SpKeypointLocation, SpByteFV>,
        VariableLength,
        Cloneable{

    static final long serialVersionUID = 1234554345;

    /**
     * Default length of standard SIFT features.
     */
    private final static int DEFAULT_LENGTH = 128;

    /**
     * keypoint feature descriptor (i.e. SIFT)
     */
    public byte[] ivec;

    /**
     * dominant orientation of keypoint
     */
    public float ori;

    /**
     * scale of keypoint
     */
    public float scale;

    /**
     * x-position of keypoint
     */
    public float x;

    /**
     * y-position of keypoint
     */
    public float y;

    /**
     * Construct with the default feature vector length for SIFT (128).
     */
    public SpKeypoint() {
        this.ivec = new byte[DEFAULT_LENGTH];
    }

    /**
     * Construct with the given feature vector length.
     *
     * @param length
     *            the length of the feature vector
     */
    public SpKeypoint(int length) {
        if (length < 0)
            length = DEFAULT_LENGTH;
        this.ivec = new byte[length];
    }

    /**
     * Construct with the given parameters.
     *
     * @param x
     *            the x-ordinate of the keypoint
     * @param y
     *            the y-ordinate of the keypoint
     * @param ori
     *            the orientation of the keypoint
     * @param scale
     *            the scale of the keypoint
     * @param ivec
     *            the feature vector of the keypoint
     */
    public SpKeypoint(float x, float y, float ori, float scale, byte[] ivec) {
        this.x = x;
        this.y = y;
        this.ori = ori;
        this.scale = scale;
        this.ivec = ivec;
    }

    /**
     * Construct by copying from another {@link SpKeypoint}
     *
     * @param k
     *            the {@link SpKeypoint} to copy from
     */
    public SpKeypoint(SpKeypoint k) {
        this(k.x, k.y, k.ori, k.scale, Arrays.copyOf(k.ivec, k.ivec.length));
    }

    @Override
    public Float getOrdinate(int dimension) {
        if (dimension == 0)
            return x;
        if (dimension == 1)
            return y;
        if (dimension == 2)
            return scale;
        return null;
    }

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public float getX() {
        return x;
    }

    @Override
    public float getY() {
        return y;
    }

    @Override
    public void setX(float x) {
        this.x = x;
    }

    @Override
    public void setY(float y) {
        this.y = y;
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public void setScale(float scale) {
        this.scale = scale;
    }

    @Override
    public String toString() {
        return ("Keypoint(" + this.x + ", " + this.y + ", " + this.scale + ", " + this.ori + ")");
    }

    /**
     * Test whether the location of this {@link SpKeypoint} and another
     * {@link SpKeypoint} is the same.
     *
     * @param obj
     *            the other keypoint
     * @return true if the locations match; false otherwise.
     */
    public boolean locationEquals(Object obj) {
        if (obj instanceof SpKeypoint) {
            final SpKeypoint kobj = (SpKeypoint) obj;

            if (kobj.x == x && kobj.y == y && kobj.scale == scale)
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SpKeypoint) {
            final SpKeypoint kobj = (SpKeypoint) obj;

            if (kobj.x == x && kobj.y == y && kobj.scale == scale && Arrays.equals(ivec, kobj.ivec))
                return true;
        }

        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 1;
        hash = hash * 31 + Float.floatToIntBits(y);
        hash = hash * 31 + Float.floatToIntBits(x);
        hash = hash * 31 + Float.floatToIntBits(scale);
        return hash;
    }

    @Override
    public SpKeypoint clone() {
        final SpKeypoint clone = new SpKeypoint();

        clone.x = x;
        clone.ori = ori;
        clone.y = y;
        clone.scale = scale;

        clone.ivec = new byte[ivec.length];
        System.arraycopy(ivec, 0, clone.ivec, 0, ivec.length);

        return clone;
    }

    @Override
    public void copyFrom(SpPoint2d p) {
        setX(p.getX());
        setY(p.getY());
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        getLocation().writeBinary(out);
        out.write(this.ivec);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
		/* Output data for the keypoint. */
        getLocation().writeASCII(out);
        for (int i = 0; i < ivec.length; i++) {
            if (i > 0 && i % 20 == 0)
                out.println();
            out.print(" " + (ivec[i] + 128));
        }
        out.println();
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        final SpKeypointLocation l = getLocation();
        l.readBinary(in);
        setLocation(l);

        in.readFully(ivec);
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        final SpKeypointLocation l = getLocation();
        l.readASCII(in);
        setLocation(l);

        int i = 0;
        while (i < ivec.length) {
            final String line = in.nextLine();
            final StringTokenizer st = new StringTokenizer(line);

            while (st.hasMoreTokens()) {
                ivec[i] = (byte) (Integer.parseInt(st.nextToken()) - 128);
                i++;
            }
        }
    }

    @Override
    public byte[] binaryHeader() {
        return "".getBytes();
    }

    @Override
    public String asciiHeader() {
        return "";
    }

    @Override
    public SpByteFV getFeatureVector() {
        return new SpByteFV(ivec);
    }

    @Override
    public SpKeypointLocation getLocation() {
        return new SpKeypointLocation(x, y, ori, scale);
    }

    /**
     * Set the location of this {@link SpKeypoint}
     *
     * @param location
     *            the location
     */
    public void setLocation(SpKeypointLocation location) {
        x = location.x;
        y = location.y;
        scale = location.scale;
        ori = location.orientation;
    }

    /**
     * Create a list of {@link SpKeypoint}s from the input list, but with the
     * positions offset by the given amount.
     *
     * @param keypoints
     *            the input list
     * @param x
     *            the x offset
     * @param y
     *            the y offset
     * @return the new keypoints
     */
    public static List<SpKeypoint> getRelativeKeypoints(List<SpKeypoint> keypoints, float x, float y) {
        final List<SpKeypoint> shifted = new ArrayList<SpKeypoint>();
        for (final SpKeypoint old : keypoints) {
            final SpKeypoint n = new SpKeypoint();
            n.x = old.x - x;
            n.y = old.y - y;
            n.ivec = old.ivec;
            n.scale = old.scale;
            n.ori = old.ori;
            shifted.add(n);
        }
        return shifted;
    }

    /**
     * Add Gaussian noise the feature vectors of some features. The original
     * features are untouched; the returned list contains a copy.
     *
     * @param siftFeatures
     *            the input features
     * @param mean
     *            the mean of the noise
     * @param sigma
     *            the standard deviation of the noise
     * @return the noisy keypoints
     */
    public static List<SpKeypoint> addGaussianNoise(List<SpKeypoint> siftFeatures, double mean, double sigma) {
        final List<SpKeypoint> toRet = new ArrayList<SpKeypoint>();
        for (final SpKeypoint keypoint : siftFeatures) {
            final SpKeypoint kpClone = keypoint.clone();
            for (int i = 0; i < keypoint.ivec.length; i++) {
                final double deviation = Normal.staticNextDouble(mean, sigma);
                int value = 0xff & keypoint.ivec[i];
                value += deviation;
                if (value < 0)
                    value = 0;
                else if (value > 255)
                    value = 255;

                kpClone.ivec[i] = (byte) value;
            }
            toRet.add(kpClone);
        }
        return toRet;
    }

    /**
     * Scale a list of keypoints by the given amount. This scales the location
     * and scale of each keypoint. The original features are untouched; the
     * returned list contains a copy.
     *
     * @param keypoints
     *            the input features.
     * @param toScale
     *            the scale factor
     * @return the scaled features.
     */
    public static List<SpKeypoint> getScaledKeypoints(List<SpKeypoint> keypoints, int toScale) {
        final List<SpKeypoint> shifted = new ArrayList<SpKeypoint>();
        for (final SpKeypoint old : keypoints) {
            final SpKeypoint n = new SpKeypoint();
            n.x = old.x * toScale;
            n.y = old.y * toScale;
            n.ivec = old.ivec;
            n.scale = old.scale * toScale;
            n.ori = old.ori;
            shifted.add(n);
        }
        return shifted;
    }

    @Override
    public void translate(float x, float y) {
        this.x += x;
        this.y += y;
    }

    @Override
    public SpKeypoint transform(Matrix transform) {
        float xt = (float) transform.get(0, 0) * getX() + (float) transform.get(0, 1) * getY()
                + (float) transform.get(0, 2);
        float yt = (float) transform.get(1, 0) * getX() + (float) transform.get(1, 1) * getY()
                + (float) transform.get(1, 2);
        final float zt = (float) transform.get(2, 0) * getX() + (float) transform.get(2, 1) * getY()
                + (float) transform.get(2, 2);

        xt /= zt;
        yt /= zt;

        return new SpKeypoint(xt, yt, this.ori, this.scale, this.ivec.clone());
    }

    @Override
    public SpPoint2d minus(SpPoint2d a) {
        final SpKeypoint kp = this.clone();
        kp.x = this.x - (int) a.getX();
        kp.y = this.y - (int) a.getY();
        return null;
    }

    @Override
    public void translate(SpPoint2d v) {
        this.translate(v.getX(), v.getY());
    }

    @Override
    public SpPoint2d copy() {
        return clone();
    }

    @Override
    public void setOrdinate(int dimension, Number value) {
        if (dimension == 0)
            x = value.floatValue();
        if (dimension == 1)
            y = value.floatValue();
    }

}
