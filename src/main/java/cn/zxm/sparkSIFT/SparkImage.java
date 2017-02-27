package cn.zxm.sparkSIFT;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Writable;
import org.apache.sanselan.formats.tiff.TiffImageData;

import java.awt.image.BufferedImage;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

/**
 * Created by root on 17-2-7.
 */
public class SparkImage implements Writable,Serializable{

    public IntWritable row;
    public IntWritable col;
    public ArrayWritable sePixels;

    public SparkImage(){
        this.row = new IntWritable(0);
        this.col = new IntWritable(0);
        this.sePixels = new ArrayWritable(FloatWritable.class);
    }

    public SparkImage(int row,int col,ArrayWritable writeables){
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

    public static BufferedImage ToGray(BufferedImage bimg){

        My_ImageMat gMat = new My_ImageMat(bimg.getType(),bimg.getWidth(),bimg.getHeight());
        gMat.data = new int[bimg.getWidth()*bimg.getHeight()];

        for (int i = 0; i < bimg.getHeight(); i++) {

            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);

                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                final short b =  (short) (color & 0xff);

                double gray =   0.3*r + 0.59*g + 0.11*b;
                int gray_i = (int) Math.round(Double.valueOf(gray));
                gMat.data[i*(bimg.getWidth())+j] = colorToRGB((byte) 255,gray_i,gray_i,gray_i);
            }
        }

        BufferedImage gimg = new BufferedImage(bimg.getWidth(),bimg.getHeight(),bimg.getType());
        gimg.setRGB(0,0,bimg.getWidth(),bimg.getHeight(),gMat.data,0,bimg.getWidth());

        return gimg;
    }

    public static void ToGray(BufferedImage bimg,My_Mat gimg){

        for (int i = 0; i < bimg.getHeight(); i++) {

            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);

                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                final short b =  (short) (color & 0xff);

                double gray =   0.3*r + 0.59*g + 0.11*b;

                gimg.ddate[i*(bimg.getWidth())+j] = gray/255.0;

                int gray_i = (int) Math.round(Double.valueOf(gray));
                gimg.data[i*(bimg.getWidth())+j] = colorToRGB((byte) 255,gray_i,gray_i,gray_i);
            }
        }
    }

    public static byte[]GetGrayDate(BufferedImage bimg){
        byte[] gray_data = new byte[bimg.getHeight() * bimg.getWidth()];
        for (int i = 0; i < bimg.getHeight(); i++) {

            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);

                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                gray_data[i*bimg.getWidth() + i] =  (byte) (color & 0xff);
            }
        }

        return gray_data;
    }


    private static int colorToRGB(byte alpha,int red,int green,int blue){
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }

    public static void CreateGaussFilter1D(int n,Double sigma,Double[] gaussfilter){
        int size = 2*n + 1;
        Double sigma22 = 2*sigma*sigma;

        Double SQRT2PI = Math.sqrt(2.0*Math.PI);
        for (int num = 0, i = -n; i <= n; i++,num++) {
            gaussfilter[num] = Math.exp(-(i*i)/sigma22)/(SQRT2PI*sigma);
        }

        Double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum = sum + gaussfilter[i];
        }

        if (sum.equals(1.0)) return;
        else {
            for (int i = 0;i<size;i++){//归一化
                gaussfilter[i] = gaussfilter[i]/sum;
            }
        }
    }

    private static int Edge(int i,int x,int w){
        int i_k = x + i;

        if (i_k < 0) i_k = -x;
        else if (i_k >= w) i_k = w - x - 1;
        else i_k = i;
        return i_k;
    }

    private static Double Clamp(Double t){
        if (t > 255.0){
            return 255.0;
        }
        else{
            return  t;
        }
    }

    public static void Blur1D_d(My_Mat img_src,My_Mat img_dst,Double []gaussfilter,int radius){

        int rows = img_src.GetRows();
        int cols = img_src.GetCols();
        Integer[] data = new Integer[rows*cols];
        Double[] ddata = new Double[rows*cols];

        Double []temp_r = new Double[rows*cols];

        for (int inx = 0,i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++,inx++) {
                temp_r[inx] = 0.0;
                for (int n = 0,l = -radius; l <= radius; l++,n++) {
                    int l_edge = Edge(l,j,cols);
                    int inx_k = inx + l_edge;
                    temp_r[inx] += img_src.ddate[inx_k]*gaussfilter[n];
                }
            }
        }

        for (int i = 0; i < cols; i++) {
            for (int j = 0; j < rows; j++) {
                double r = 0.0;
                int inx = j*cols + i;
                for (int n = 0,k = -radius; k <=radius; k++,n++) {
                    int k_edge = Edge(k,j,rows);
                    int inx_k = inx + cols*k_edge;
                    r += temp_r[inx_k]*gaussfilter[n];
                }
                if (r > 1.0)
                    r = 1.0;
                ddata[inx] = r;
                data[inx] = (int) Math.round(Clamp(r*255));
            }
        }

        img_dst.CreateData(cols,rows,data);
        img_dst.CreateDData(cols,rows,ddata);
    }


    public static void CreateInitImg(My_Mat srcImg,Double sigma,int radius) throws IOException {

       // BiCubicInterpolationScale cps = new BiCubicInterpolationScale();

        int re_r = srcImg.GetRows()*2;
        int re_c = srcImg.GetCols()*2;

        srcImg.data = BilineInterpolationScale.imgScale(srcImg.data,srcImg.GetCols(),srcImg.GetRows(),re_c,re_r);
        srcImg.SetRows(re_r);
        srcImg.SetCols(re_c);

        Double []ddata = new Double[srcImg.GetRows()*srcImg.GetCols()];

        for (int i = 0; i < srcImg.GetRows()*srcImg.GetCols(); i++) {
            ddata[i] = (srcImg.data[i] & 0x000000ff) / 255.0;
        }

        srcImg.ddate = ddata;

        Double sig_diff = Math.sqrt( sigma * sigma - 0.5 * 0.5 * 4 );
        Double []gaussfilter1D = new Double[(2*radius + 1)];
        CreateGaussFilter1D(radius,sig_diff,gaussfilter1D);

        Blur1D_d(srcImg,srcImg,gaussfilter1D,radius);
    }
}
