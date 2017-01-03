/**
 * Created by root on 16-12-19.
 */
package cn.zxm.sparkSIFT;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.opencv.core.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


public class HelloCV_J {

    public static  class Wight_Hight implements java.io.Serializable {
        private  int rows;
        private  int cols;

        public Wight_Hight(int rows,int cols){
            this.rows = rows;
            this.cols = cols;
        }

        public int getRows(){
            return this.rows;
        }

        public int getCols(){
            return this.cols;
        }

        public void setRows(int rows){
            this.rows = rows;
        }

        public void setCols(int cols){
            this.cols = cols;
        }
    }

    public static final int pics_size = 8;

    public static void main(String[] args) throws IOException {

        SparkConf sparkConf = new SparkConf().setAppName("HelloCV_J");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String getPicMat = "/home/simon/Public/Opencv/GetPicMat/build-getPicMat-Desktop-Debug/getPicMat ";
        List<String> filename = new ArrayList<String>();
        for (int i = 0; i < pics_size; i++) {
            filename.add("car" + (i+1) + ".jpg");
        }

        List<Wight_Hight> wh = ctx.parallelize(filename,filename.size()).pipe(getPicMat).map(new Function<String,Wight_Hight>(){
            public Wight_Hight call(String arg0) throws Exception{
                String []row_col = arg0.split(" ");
                return new Wight_Hight(Integer.parseInt(row_col[0]),Integer.parseInt(row_col[1]));
            }
        }).collect();//the getPicMat run two times,why

        /*Debug mode*/
    /*    List<Wight_Hight> wh = new ArrayList<Wight_Hight>(2);
        wh.add(new Wight_Hight(675,1000));
        wh.add(new Wight_Hight(640,1024));*/
        /*Debug mode*/

        Mat[] row_cols = new Mat[wh.size()];//can Mat seriable
        for (int i = 0; i < wh.size(); i++) {
            row_cols[i] = new Mat(wh.get(i).rows, wh.get(i).cols, CvType.CV_8UC1);
        }

        /*Get the mat_string_message of pictures*/
        JavaRDD matrdd = ctx.textFile("hdfs://192.168.137.2:9000/user/root/car_mat",1);
    //    JavaRDD filerdd = matrdd.glom();

      //  List<List<String>> matpics_s = filerdd.collect();

     /*   List<List<Double>> matpics = new ArrayList<List<Double>>(2);
        for (int i = 0; i < matpics_s.size(); i++) {
            matpics.add(ctx.parallelize(matpics_s.get(i),1).flatMap(new FlatMapFunction<String,String>() {
                @Override
                public Iterator<String> call(String o) throws Exception {
                    return Arrays.asList(o.split(" ")).iterator();
                }
            }).map(new Function<String,Double>(){
                public Double call(String arg0) throws Exception{
                    return java.lang.Double.parseDouble(arg0);
                }
            }).collect());
        }*/

        List<List<Double>>matpics = matrdd.flatMap(new FlatMapFunction<String,String>() {
            @Override
            public Iterator<String> call(String o) throws Exception {
                return Arrays.asList(o.split(" ")).iterator();
            }
        }).map(new Function<String, Double>(){
            public Double call(String arg0) throws Exception{
             //   return java.lang.Double.parseDouble(arg0);
                return Double.valueOf(arg0).doubleValue();
            }
        }).glom().collect();

    //    System.out.println("***matpics" + matpics.size());
    //    System.out.println("***matpics(1)" + matpics.get(1).size());

        /*Get the mat_string_message of pictures*/

    //    Mat m = Highgui.imread(args[0]);//use mat_file in the car_mat/

    /*    double[][] a_i = new double[2][];
        for (int i = 0;i<2;i++){
            a_i[i] = new double[matpics.get(i).size()];
            for (int j = 0; j < matpics.get(i).size(); j++) {
                a_i[i][j] = matpics.get(i).get(j).doubleValue();
            }
        }*/

        for (int i = 0; i < row_cols.length; i++) {
            //Double[] d = matpics.get(i);
            List<Double> d = matpics.get(i);
            double[] a = new double[matpics.get(i).size()];
            for (int j = 0; j <a.length; j++) {
                a[j] = d.get(j);

            }
            row_cols[i].put(0,0,a);
        }

        /* feature detect & extract*/
    /*    FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIFT);
        MatOfKeyPoint mkp = new MatOfKeyPoint();
        fd.detect(row_cols[0],mkp);

        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        Mat mat_descri = new Mat();
        de.compute(row_cols[0],mkp,mat_descri);*/
        /* feature extract & extract*/

        /*show picture*/
    /*    Imgproc.resize(row_cols[0],row_cols[0],new Size(640,480));
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg",row_cols[0],matOfByte);
        byte[] byteArray= matOfByte.toArray();

        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage bufImage = ImageIO.read(in);
        JFrame frame = new JFrame();
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.pack();
        frame.setVisible(true);

        Imgproc.resize(row_cols[1],row_cols[1],new Size(640,480));
        //MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg",row_cols[1],matOfByte);
        byteArray= matOfByte.toArray();

        in = new ByteArrayInputStream(byteArray);
        bufImage = ImageIO.read(in);
        JFrame frame_1 = new JFrame();
        frame_1.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame_1.pack();
        frame_1.setVisible(true);*/

        for (int i = 0; i < filename.size(); i++) {
            Imgproc.resize(row_cols[i],row_cols[i],new Size(640,480));
            MatOfByte matOfByte = new MatOfByte();
            Highgui.imencode(".jpg",row_cols[i],matOfByte);
            byte[] byteArray= matOfByte.toArray();

            InputStream in = new ByteArrayInputStream(byteArray);
            BufferedImage bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        }
        /*show picture*/

    /*    List<Double> list = new ArrayList<Double>();

        for (i = 0;i<m.rows();i++)
            for (j = 0;j<m.cols();j++)
                list.add(i*m.cols()+j, m.get(i,j)[0]);

        JavaRDD javaRDD = ctx.parallelize(list,10);
        JavaRDD newrdd = javaRDD.map( new Function<Double,Double>(){

            public Double call(Double arg0) throws Exception{
                return arg0 + 1d;
            }
        });*/

        ctx.stop();
    }
}
