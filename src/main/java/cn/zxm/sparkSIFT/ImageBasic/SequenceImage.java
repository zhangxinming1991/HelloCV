package cn.zxm.sparkSIFT.ImageBasic;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Created by root on 17-2-27.
 */
public class SequenceImage implements Writable {

    public IntWritable row;
    public IntWritable col;
    public ArrayWritable sePixels;

    public SequenceImage(){
        this.row = new IntWritable(0);
        this.col = new IntWritable(0);
        this.sePixels = new ArrayWritable(FloatWritable.class);
    }

    public SequenceImage(int row,int col,ArrayWritable writeables){
        this.row = new IntWritable(row);
        this.col = new IntWritable(col);
        this.sePixels = writeables;
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        row.readFields(input);
        col.readFields(input);
        sePixels.readFields(input);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        row.write(output);
        col.write(output);
        sePixels.write(output);
    }

    public float[] GetSeqPixel(){
        float[] pixels = new float[row.get()*col.get()];
        Writable[] writeables = sePixels.get();
        for (int i = 0; i < pixels.length; i++) {
            FloatWritable val = (FloatWritable) writeables[i];
            pixels[i] = val.get();
        }

        return pixels;
    }


}
