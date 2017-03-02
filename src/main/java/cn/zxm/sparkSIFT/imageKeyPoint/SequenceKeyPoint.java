package cn.zxm.sparkSIFT.imageKeyPoint;

import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by root on 17-3-1.
 */
public class SequenceKeyPoint implements Writable{
    public FloatWritable x;
    public FloatWritable y;
    public FloatWritable ori;
    public FloatWritable scale;
    public BytesWritable ivec;

    public SequenceKeyPoint(){
        this.ivec = new BytesWritable();
        this.x = new FloatWritable(0f);
        this.y = new FloatWritable(0f);
        this.ori = new FloatWritable(0f);
        this.scale = new FloatWritable(0f);
    }
    public SequenceKeyPoint(float x,float y,float ori,float scale,byte[] ivec){
        this.ivec = new BytesWritable(ivec);
        this.x = new FloatWritable(x);
        this.y = new FloatWritable(y);
        this.ori = new FloatWritable(ori);
        this.scale = new FloatWritable(scale);
    }
    @Override
    public void write(DataOutput output) throws IOException {
        ivec.write(output);
        x.write(output);
        y.write(output);
        ori.write(output);
        scale.write(output);

    }

    @Override
    public void readFields(DataInput input) throws IOException {
        ivec.readFields(input);
        x.readFields(input);
        y.readFields(input);
        scale.readFields(input);
        ori.readFields(input);
    }
}
