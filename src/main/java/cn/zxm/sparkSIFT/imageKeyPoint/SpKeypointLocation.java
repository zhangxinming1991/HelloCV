package cn.zxm.sparkSIFT.imageKeyPoint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
public class SpKeypointLocation extends SpScaleSpaceLocation{

    private static final long serialVersionUID = 1L;

    /**
     * The dominant orientation of the {@link SpKeypoint}
     */
    public float orientation;

    /**
     * Construct with zero location, orientation and scale.
     */
    public SpKeypointLocation() {
    }

    /**
     * Construct with the given parameters
     *
     * @param x
     *            x-ordinate of feature
     * @param y
     *            y-ordinate of feature
     * @param scale
     *            scale of feature
     * @param orientation
     *            orientation of feature
     */
    public SpKeypointLocation(float x, float y, float orientation, float scale) {
        super(x, y, scale);
        this.orientation = orientation;
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
        out.writeFloat(this.scale);
        out.writeFloat(this.orientation);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        // for legacy reasons ascii format writes y, x, scale, ori
        out.format("%4.2f %4.2f %4.2f %4.3f", y, x, scale, orientation);
        out.println();
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        super.readBinary(in);
        orientation = in.readFloat();
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        super.readASCII(in);
        orientation = Float.parseFloat(in.next());
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
        final float[] pos = { x, y, scale, orientation };
        return pos[dimension];
    }

    @Override
    public int getDimensions() {
        return 3;
    }

}
