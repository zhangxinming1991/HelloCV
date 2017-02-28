package cn.zxm.sparkSIFT.ImageBasic;

import org.apache.hadoop.io.*;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by root on 17-2-27.
 */
public class SequenceImage implements Writable,Serializable{

    public IntWritable row;
    public IntWritable col;
    public BytesWritable sePixels;

    public SequenceImage(){
        this.row = new IntWritable(0);
        this.col = new IntWritable(0);
        this.sePixels = new BytesWritable();
    }

    public SequenceImage(int row,int col,byte[] writeables){
        this.row = new IntWritable(row);
        this.col = new IntWritable(col);
        this.sePixels = new BytesWritable(writeables);
    }

    public SequenceImage(int row,int col,byte[][] writeables){
        this.row = new IntWritable(row);
        this.col = new IntWritable(col);

        byte[] onediem = new byte[this.row.get()*this.col.get()];
        for (int i = 0; i < this.row.get(); i++) {
            for (int j = 0; j < this.col.get(); j++) {
                onediem[i*this.col.get() + j] = writeables[i][j];
            }
        }
        this.sePixels = new BytesWritable(onediem);
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


    public byte[] GetSeqPixel(){
        byte[] pixels = sePixels.getBytes();
        return pixels;
    }


}
