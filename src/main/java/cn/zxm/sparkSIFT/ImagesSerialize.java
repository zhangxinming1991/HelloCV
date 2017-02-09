package cn.zxm.sparkSIFT;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.SequenceFileOutputFormat;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by root on 17-2-6.
 */
public class ImagesSerialize {

    public static void Serialize_Image(File outfile,My_ImageMat ser_img){

        ObjectOutputStream oo = null;
        try {
            oo = new ObjectOutputStream(new FileOutputStream(outfile));
            oo.writeObject(ser_img);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void Serialize_ImagesList(File outfile, List<My_ImageMat> imgs){
        ObjectOutputStream oo = null;
        try {
            oo = new ObjectOutputStream(new FileOutputStream(outfile));
            oo.writeObject(imgs);
            oo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<My_ImageMat> Deserialize_ImageList(InputStream inputstream){
        List<My_ImageMat> imgmat = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(inputstream);
            imgmat = (List<My_ImageMat>) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return imgmat;
    }

    public static My_ImageMat Deserialize_Image(File infile) {
        My_ImageMat imgmat = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(infile));
            imgmat = (My_ImageMat) ois.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return imgmat;
    }

    public static void ShowPic(My_ImageMat picMat){

        BufferedImage showimg = new BufferedImage(picMat.GetCols(),picMat.GetRows(),picMat.GetType());
        showimg.setRGB(0,0,showimg.getWidth(),showimg.getHeight(),picMat.data,0,showimg.getWidth());

        JFrame frame = new JFrame("readhdfsimage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JLabel(new ImageIcon(showimg)));
        frame.pack();
        frame.setVisible(true);
    }

    public static List<My_ImageMat> GetMatofPic(int picSize) throws IOException {
        List<My_ImageMat> serImgMatList = new ArrayList<My_ImageMat>();
        BufferedImage[] imageIOs = new BufferedImage[picSize];
        for (int i = 0; i < picSize; i++) {
            String filename = "car" + (i+1) + ".jpg";
            File file = new File(filename);//原始图片
            imageIOs[i] =ImageIO.read(file);

            int rgb[] = new int[imageIOs[i].getHeight()*imageIOs[i].getWidth()];
            imageIOs[i].getRGB(0,0,imageIOs[i].getWidth(),imageIOs[i].getHeight(),rgb,0,imageIOs[i].getWidth());//获取图片像素值

            //创建图片mat
            My_ImageMat my_imageMat = new My_ImageMat(imageIOs[i].getType(),imageIOs[i].getWidth(),imageIOs[i].getHeight());
            my_imageMat.CreateData(imageIOs[i].getWidth(),imageIOs[i].getHeight(),rgb);

            serImgMatList.add(my_imageMat);
        }

        return serImgMatList;
    }

    public static List<BufferedImage> GetBfofPic(int picSize) throws IOException {
        BufferedImage[] imageIOs = new BufferedImage[picSize];
        for (int i = 0; i < picSize; i++) {
            String filename = "car" + (i + 1) + ".jpg";
            File file = new File(filename);//原始图片
            imageIOs[i] = ImageIO.read(file);
        }

        return Arrays.asList(imageIOs);
    }
    public static FileSystem StartSerialize(List<My_ImageMat> serImgMatList,Path pt) throws URISyntaxException, IOException, InterruptedException {
        /*创建hdfs 图片集合序列文件*/
        FileSystem fs = FileSystem.get(new URI("hdfs://192.168.137.2:9000"),new Configuration(),"root");
        FSDataOutputStream out = fs.create(pt);//序列化图片文件
        /*创建hdfs 图片集合序列文件*/

        long startTime = System.currentTimeMillis();
        /*写图片集合序列文件*/
        File serfile = new File("image_serialize.txt");
        Serialize_ImagesList(serfile,serImgMatList);
        FileInputStream is = new FileInputStream(serfile);
        org.apache.commons.io.IOUtils.copy(is,out);//保存序列化的图片集合到hdfs
        out.close();
        /*写图片集合序列文件*/
        long endTime = System.currentTimeMillis();
        System.out.println("use time " + (endTime-startTime));

        return fs;
    }

    public static void rm_hdfs(String hdfs_htname, String pt_s) throws URISyntaxException, IOException, InterruptedException {
        //FileSystem fs = FileSystem.get(new URI("hdfs://192.168.137.2:9000"),new Configuration(),"root");
        Path pt = new Path(hdfs_htname + pt_s);
        FileSystem fs = FileSystem.get(new URI(hdfs_htname),new Configuration(),"root");
        fs.delete(pt,true);
    }

    /*public static class ConvertToWriteableTypes implements PairFunction<Tuple2<>,,>{
        public Tuple2<Text,BytesWritable>
    }*/

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException {
        SparkConf sparkConf = new SparkConf()
                .setAppName("ImagesSerialize")
                .set("spark.cores.max","20");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        //String hdfs_name = "hdfs://localhost:9000";
        String hdfs_name = "hdfs://simon-Vostro-3905:9000";
        String pt_s = "/user/root/seria_img/";

        rm_hdfs(hdfs_name,pt_s);//删除image_serialize.txt

        int imgFileSize = 1;
        /*List<My_ImageMat> serImgMatList = GetMatofPic(imgFileSize);//获取图片集合的mat

        JavaRDD<My_ImageMat> saveimgsrdd = ctx.parallelize(serImgMatList,8);
        saveimgsrdd.saveAsObjectFile(hdfs_name + pt_s);//开启序列化图片集合

        JavaRDD imgsrdd = ctx.objectFile(hdfs_name + pt_s,8);
        List<My_ImageMat> desImgList = imgsrdd.collect();

        for (int i = 0; i < desImgList.size(); i++) {
            ShowPic(desImgList.get(i));
        }*/

        List<BufferedImage> bfimgs = GetBfofPic(imgFileSize);

        JavaRDD<BufferedImage> bfimgs_rdd = ctx.parallelize(bfimgs,imgFileSize);

        /*bfimgs_rdd.mapToPair(new PairFunction<BufferedImage, Object, Object>() {
        }).saveAsHadoopFile(hdfs_name+pt_s,String.class, BytesWritable.class, SequenceFileOutputFormat.class);*/

        ctx.stop();
    }
}
