package cn.zxm.sparkSIFT.imageKeyPoint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
public class SpSpatialLocation extends SpPoint2dImpl implements SpLocation, Cloneable{
    private static final long serialVersionUID = 1L;

    /**
     * Construct the ScaleSpaceLocation at 0, 0.
     */
    public SpSpatialLocation() {
        super(0, 0);
    }

    /**
     * Construct the SpatialLocation with the given x and y coordinates.
     *
     * @param x
     *            the x-coordinate
     * @param y
     *            the y-coordinate
     */
    public SpSpatialLocation(float x, float y) {
        super(x, y);
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        // for legacy reasons ascii format writes y, x
        out.format("%4.2f %4.2f", y, x);
        out.println();
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        y = Float.parseFloat(in.next());
        x = Float.parseFloat(in.next());
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
    public Float getOrdinate(int dimension) {
        final float[] pos = { x, y };
        return pos[dimension];
    }

    @Override
    public int getDimensions() {
        return 3;
    }

    @Override
    public SpSpatialLocation clone() {
        final SpSpatialLocation c = (SpSpatialLocation) super.clone();
        return c;
    }
}
