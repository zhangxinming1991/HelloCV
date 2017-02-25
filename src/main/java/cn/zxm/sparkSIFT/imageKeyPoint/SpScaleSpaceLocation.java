package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.math.geometry.point.ScaleSpacePoint;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
public class SpScaleSpaceLocation extends SpSpatialLocation implements SpScaleSpacePoint, Cloneable{

    private static final long serialVersionUID = 1L;

    /**
     * the scale
     */
    public float scale;

    /**
     * Construct the ScaleSpaceLocation at 0, 0, 0.
     */
    public SpScaleSpaceLocation() {
        super(0, 0);
    }

    /**
     * Construct the ScaleSpaceLocation with the given x, y and
     * scale coordinates.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param scale the scale coordinate
     */
    public SpScaleSpaceLocation(float x, float y, float scale) {
        super(x, y);
        this.scale = scale;
    }

    @Override
    public void writeBinary(DataOutput out) throws IOException {
        out.writeFloat(this.x);
        out.writeFloat(this.y);
        out.writeFloat(this.scale);
    }

    @Override
    public void writeASCII(PrintWriter out) throws IOException {
        //for legacy reasons ascii format writes y, x, scale
        out.format("%4.2f %4.2f %4.2f", y, x, scale);
        out.println();
    }

    @Override
    public void readBinary(DataInput in) throws IOException {
        x = in.readFloat();
        y = in.readFloat();
        scale = in.readFloat();
    }

    @Override
    public void readASCII(Scanner in) throws IOException {
        y = Float.parseFloat(in.next());
        x = Float.parseFloat(in.next());
        scale = Float.parseFloat(in.next());
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
        float [] pos = {x, y, scale};
        return pos[dimension];
    }

    @Override
    public int getDimensions() {
        return 3;
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
    public SpScaleSpaceLocation clone(){
        SpScaleSpaceLocation l = (SpScaleSpaceLocation) super.clone();
        l.scale = this.scale;
        return l;
    }

}
