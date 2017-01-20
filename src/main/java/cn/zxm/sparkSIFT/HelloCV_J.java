/**
 * Created by root on 16-12-19.
 */
package cn.zxm.sparkSIFT;

import org.apache.commons.math3.analysis.function.Gaussian;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.ml.clustering.GaussianMixture;
import org.apache.spark.sql.execution.columnar.NULL;
import org.opencv.core.*;
import org.opencv.features2d.*;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
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

    //Run mode
    public static final int pics_size = 8;

    //Debug mode
    //public static final int pics_size = 2;

    public static void main(String[] args) throws IOException {

        SparkConf sparkConf = new SparkConf()
                .setAppName("HelloCV_J")
                .set("spark.cores.max","16");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        String getPicMat = "/home/simon/Public/Opencv/GetPicMat/build-getPicMat-Desktop-Debug/getPicMat ";
        List<String> filename = new ArrayList<String>();
        for (int i = 0; i < pics_size; i++) {

            filename.add("car" + (i+1) + ".jpg");
        }

        /*run mode*/
    /*    List<Wight_Hight> wh = ctx.parallelize(filename,filename.size()).pipe(getPicMat).map(new Function<String,Wight_Hight>(){
            public Wight_Hight call(String arg0) throws Exception{
                String []row_col = arg0.split(" ");
                return new Wight_Hight(Integer.parseInt(row_col[0]),Integer.parseInt(row_col[1]));
            }
        }).collect();//the getPicMat run two times,why*/
        /*run mode*/

        /*for (int i = 0; i < wh.size(); i++) {
            System.out.println("***" + i + ":" + wh.get(i).rows + ":" + wh.get(i).cols);
        }

        /*Debug mode*/
        List<Wight_Hight> wh = new ArrayList<Wight_Hight>(pics_size);
        wh.add(new Wight_Hight(300,480));
        wh.add(new Wight_Hight(186,302));
        wh.add(new Wight_Hight(300,400));
        wh.add(new Wight_Hight(675,1000));
        wh.add(new Wight_Hight(640,1024));
        wh.add(new Wight_Hight(1080,1920));
        wh.add(new Wight_Hight(370,620));
        wh.add(new Wight_Hight(234,490));
        //   wh.add(new Wight_Hight(600,800));
        /*Debug mode*/

        Mat[] row_cols = new Mat[wh.size()];//can Mat seriable
        for (int i = 0; i < wh.size(); i++) {
            row_cols[i] = new Mat(wh.get(i).rows, wh.get(i).cols, CvType.CV_8UC1);
        }

        /*Get the mat_string_message of pictures*/
        JavaRDD matrdd = ctx.textFile("hdfs://192.168.137.2:9000/user/root/car_mat/",1).cache();

    /*    List<Double>matpics = matrdd.flatMap(new FlatMapFunction<String,String>() {
            @Override
            public Iterator<String> call(String o) throws Exception {
                return Arrays.asList(o.split(" ")).iterator();
            }
        }).map(new Function<String, Double>(){
            public Double call(String arg0) throws Exception{
             //   return java.lang.Double.parseDouble(arg0);
                return Double.valueOf(arg0).doubleValue();
            }
        }).collect();
        System.out.println(matpics.size());*/


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

        for (int i = 0; i < row_cols.length; i++) {
            List<Double> d = matpics.get(i);
            double[] a = new double[matpics.get(i).size()];
            for (int j = 0; j <a.length; j++) {
                a[j] = d.get(j);

            }
            row_cols[i].put(0,0,a);
        }

        /* feature detect & extract*/
        List<Mat> mat_descri = new ArrayList<Mat>(pics_size);
        List<MatOfKeyPoint> mkp = new ArrayList<MatOfKeyPoint>(pics_size);
        FeatureDetector fd = FeatureDetector.create(FeatureDetector.SIFT);
        DescriptorExtractor de = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        for (int i = 0; i < pics_size; i++) {

            MatOfKeyPoint temp_k = new MatOfKeyPoint();
            Mat temp_mat = new Mat();
            fd.detect(row_cols[i],temp_k);
            de.compute(row_cols[i],temp_k,temp_mat);

            mat_descri.add(temp_mat);
            mkp.add(temp_k);
            System.out.println("pic" + i + ":" + temp_mat.rows() + "keypoints");
        }
        /* feature extract & extract*/

        /*write descriptor to file*/
        for (int ps = 0; ps < pics_size; ps++) {
            String des_file_Name = filename.get(ps).substring(0,filename.get(ps).indexOf("."));
            File file = new File(des_file_Name + "_descriptor.txt");
            if (file == null)
            {
                System.out.println("open file failed");
            }
            FileWriter fw = new FileWriter(file);
            List<KeyPoint> lkp = mkp.get(ps).toList();
            for (int i = 0;i< lkp.size();i++) {
                System.out.println("("  + lkp.get(i).pt.y + ", " + lkp.get(i).pt.x + ")" + lkp.get(i).angle + " " + lkp.get(i).octave);
                fw.write("("  + lkp.get(i).pt.y + ", " + lkp.get(i).pt.x + ")" + lkp.get(i).angle + " " + lkp.get(i).octave + "\n");

                for (int j = 0; j < mat_descri.get(ps).cols(); j++) {
                    System.out.print(mat_descri.get(ps).get(i,j)[0] + " ");
                    String pri = new Double(mat_descri.get(ps).get(i,j)[0]).toString();
                    fw.write(pri + " ");
                }
                System.out.println();
                fw.write("\n");
            }
            fw.close();
        }
        /*write descriptor to file*/

        /*match*/
        Mat out_img = new Mat();
        if (pics_size > 1)
        {
            MatOfDMatch matofm = new MatOfDMatch();
            DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
            matcher.match(mat_descri.get(0),mat_descri.get(1),matofm);

            /*find the good matchs*/
            System.out.println("[Before choose]match :" + matofm.toArray().length);
            double max_distance = 0;
            double min_distance = 10000.0;
            DMatch[] matches = matofm.toArray();
            for (int i = 0; i < matches.length; i++) {
                if (matches[i].distance < min_distance) min_distance = matches[i].distance;
                if (matches[i].distance > max_distance) max_distance = matches[i].distance;
            }

            List<DMatch> goodmatches = new ArrayList<DMatch>();
            for (int i = 0; i < matches.length; i++) {
                if (matches[i].distance < (0.31) * max_distance){
                    goodmatches.add(matches[i]);
                }
            }
            System.out.println("[After choose]match :" + goodmatches.size());

            MatOfDMatch goodmatofm = new MatOfDMatch();
            goodmatofm.fromList(goodmatches);
            /*find the good matchs*/

            Features2d.drawMatches(row_cols[0],mkp.get(0),row_cols[1],mkp.get(1),goodmatofm,out_img);
            Imgproc.resize(out_img,out_img,new Size(640,480));
            MatOfByte matOfByte = new MatOfByte();
            Highgui.imencode(".jpg",out_img,matOfByte);
            byte[] byteArray= matOfByte.toArray();

            InputStream in = new ByteArrayInputStream(byteArray);
            BufferedImage bufImage = ImageIO.read(in);
            JFrame frame = new JFrame();
            frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
            frame.pack();
            frame.setVisible(true);
        }

        for (int i = 0; i < filename.size(); i++) {
            Features2d.drawKeypoints(row_cols[i],mkp.get(i),row_cols[i]);
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
         /*match*/

        ctx.stop();
    }
}
