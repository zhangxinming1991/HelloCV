package cn.zxm.sparkSIFT;

import org.apache.hadoop.io.Writable;
import org.openimaj.io.WriteableBinary;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Created by root on 17-2-21.
 */
public class My_Mat implements WriteableBinary {

    private  int rows;
    private  int cols;
    public int picType;
    public  Integer data[];
    public Double ddate[];

    public static final byte[] BINARY_HEADER = "KPT".getBytes();

    public My_Mat(int picType){
        this.picType = picType;
        //data = new Integer[w*h];
    }

    public void SetType(int picType){
        this.picType = picType;
    }

    @Override
    public  byte[] binaryHeader(){
        return BINARY_HEADER;
    }

    @Override
    public void writeBinary(DataOutput output){
    }

    public int GetRows(){
        return this.rows;
    }

    public int GetCols(){
        return this.cols;
    }

    public void SetRows(int rows){
        this.rows = rows;
    }

    public void SetCols(int cols){
        this.cols = cols;
    }

    public void CreateData(int w,int h,Integer []data){
        if (data.length != w*h)
        {
            System.out.println("the length of data is wrong!");
            return;
        }
        this.cols = w;
        this.rows = h;
        this.data = data;
    }

    public void CreateDData(int w,int h,Double []ddata){
        if (ddata.length != w*h)
        {
            //            System.out.println("the length of data is wrong!");
            return;
        }
        this.cols = w;
        this.rows = h;
        this.ddate = ddata;
    }

    public short GetAlpha(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>24) & 0xff);
    }

    public short GetRed(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>16) & 0xff);
    }

    public short GetGreen(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>8) & 0xff);
    }

    public short GetBlue(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color) & 0xff);
    }
}
