package cn.zxm.sparkSIFT;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import scala.collection.immutable.Range;
import scala.tools.nsc.doc.html.HtmlPage;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 17-1-23.
 */
public class ResizePic {

    public static void ToGray(BufferedImage bimg,BuildGaussPry.My_Mat gimg){
        //   System.out.println(bimg.getWidth() + ":" + bimg.getHeight());
        for (int i = 0; i < bimg.getHeight(); i++) {
            // System.out.println(i);
            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);
                //System.out.println("rgb:" + color);
                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                final short b =  (short) (color & 0xff);

                double gray =   0.3*r + 0.59*g + 0.11*b;
                int gray_i = (int) Math.round(Double.valueOf(gray));
                gimg.data[i*(bimg.getWidth())+j] = colorToRGB((byte) 255,gray_i,gray_i,gray_i);
            }
            //   System.out.println();
        }
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

    public static void main(String[] args) throws IOException {
        SparkConf sparkConf = new SparkConf()
                .setAppName("BuildGaussPry")
                .set("spark.cores.max","4");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        BilineInterpolationScale bps = new BilineInterpolationScale();
        BiCubicInterpolationScale cps = new BiCubicInterpolationScale();
        BufferedImage bimg = ImageIO.read(new File("testcar2.jpg"));//原始图片
        int w = bimg.getWidth();
        int h = bimg.getHeight();

        int re_w = w*4;
        int re_h = h*4;
        int []rgb = new int[w*h];
        bimg.getRGB(0,0,w,h,rgb,0,w);

        BuildGaussPry.My_Mat grayimg = new BuildGaussPry.My_Mat(bimg.getType());
        Integer[] data = new Integer[w*h];
        grayimg.CreateData(w,h,data);
        ToGray(bimg,grayimg);

        Double gray_d[] = new Double[w*h];
        for (int i = 0; i < w*h; i++) {
            gray_d[i] = (grayimg.data[i]&0x000000ff)/255.0;
        }
        grayimg.CreateDData(w,h,gray_d);
        BuildGaussPry.SavePicPixel("TestResize/gray",grayimg,"FLOAT");
        //Double []regray_d = bps.imgScale_d(grayimg.ddate,w,h,re_w,re_h);

        Integer[] regray = cps.imgScale(grayimg.data,w,h,re_w,re_h);
        Double []regray_d = cps.imgScale_d(grayimg.ddate,w,h,re_w,re_h);
        BuildGaussPry.My_Mat regrayimg = new BuildGaussPry.My_Mat(bimg.getType());
        //Integer[] regray = new Integer[re_w*re_h];
        /*for (int i = 0; i < re_w*re_h; i++) {
            Double temp = (regray_d[i]*255.0);
            regray[i] = temp.intValue();
        }*/
        int []color = new int[regray.length];
        //Double []regray_d = new Double[regray.length];
        for (int i = 0; i < color.length; i++) {
            int temp_color = (int) (regray_d[i]*255.0);
            color[i] = colorToRGB((byte) 255,temp_color,temp_color,temp_color);
        //    regray_d[i] = (regray[i]&0x000000ff)/255.0;
        }

        regrayimg.CreateData(re_w,re_h,regray);
        regrayimg.CreateDData(re_w,re_h,regray_d);

    //    BuildGaussPry.ShowPic(ctx,regrayimg,"2scale");
        BufferedImage gray_temp = new BufferedImage(re_w,re_h,bimg.getType());
        gray_temp.setRGB(0,0,re_w,re_h,color,0,re_w);

        JFrame frame = new JFrame("2scale");
        frame.getContentPane().add(new JLabel(new ImageIcon(gray_temp)));
        frame.pack();
        frame.setVisible(true);

        BuildGaussPry.SavePicPixel("TestResize/2scale",regrayimg,"FLOAT");
        /*BufferedImage reimg = new BufferedImage(re_w,re_h,bimg.getType());
        reimg.setRGB(0,0,re_w,re_h,resize,0,re_w);
        File file = new File("retest2.jpg");
        if (file == null)
            System.out.println("create fail");
        ImageIO.write(reimg,"jpg",file);*/

        ctx.close();
    }


}
