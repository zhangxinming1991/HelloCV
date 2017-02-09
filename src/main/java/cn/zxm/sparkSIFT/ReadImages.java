package cn.zxm.sparkSIFT;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.util.ByteBufferOutputStream;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by root on 17-2-5.
 */
public class ReadImages {

    public static void main(String[] args) throws IOException {
        SparkConf sparkConf = new SparkConf()
                .setAppName("ReadImages")
                .set("spark.cores.max","16");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        Path pt = new Path("hdfs://192.168.137.2:9000/user/root/car_pic/car1.jpg");
        FileSystem fs = FileSystem.get(new Configuration());
        FSDataInputStream in = fs.open(pt);
        byte[] Buffer = new byte[1024*1024];

        BufferedInputStream inputStream = new BufferedInputStream(in);

        /*ByteArrayOutputStream bos = new ByteBufferOutputStream();
        int len = 0;
        while (-1 != (len = inputStream.read(Buffer))){
            bos.write(Buffer,0,len);
        }
        /*byte[] outbuffer = bos.toByteArray();
        System.out.println(outbuffer.length);*/

        BufferedImage imageIO = ImageIO.read(inputStream);

        JFrame frame = new JFrame("readhdfsimage");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JLabel(new ImageIcon(imageIO)));
        frame.pack();
        frame.setVisible(true);

        inputStream.close();
        in.close();

        ctx.stop();


    }
}
