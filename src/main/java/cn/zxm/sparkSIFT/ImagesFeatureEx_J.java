package cn.zxm.sparkSIFT;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.highgui.Highgui;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.matcher.BasicMatcher;
import org.openimaj.feature.local.matcher.FastBasicKeypointMatcher;
import org.openimaj.feature.local.matcher.LocalFeatureMatcher;
import org.openimaj.feature.local.matcher.MatchingUtilities;
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d;
import org.openimaj.image.DisplayUtilities;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.image.colour.RGBColour;
import org.openimaj.image.feature.local.engine.DoGSIFTEngine;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator;
import org.openimaj.math.model.fit.RANSAC;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;

/**
 * Created by root on 17-2-8.
 */
public class ImagesFeatureEx_J {
    public static byte[] readStream(InputStream stream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024*1024*5];
        while ((nRead = stream.read(data,0,data.length)) != -1){
            buffer.write(data,0,nRead);
        }

        return data;
    }
    public static MatOfByte makeBytesToMat(byte[] mat){
        return new MatOfByte(mat);
    }

    public static void main(String[] args) throws IOException {
        SparkConf sparkConf = new SparkConf()
                .setAppName("HelloCV_J")
                .set("spark.cores.max","16");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        /*System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        FileInputStream inputStream = new FileInputStream("car2.jpg");
        byte[] imgData = readStream(inputStream);

        Mat outimg = Highgui.imdecode(new MatOfByte(imgData),Highgui.IMREAD_COLOR);
        MatOfByte matOfByte = new MatOfByte();
        Highgui.imencode(".jpg",outimg,matOfByte);
        byte[] byteArray= matOfByte.toArray();

        InputStream in = new ByteArrayInputStream(byteArray);
        BufferedImage bufImage = ImageIO.read(in);*/


        MBFImage query = ImageUtilities.readMBF(new File("car2.jpg"));
        MBFImage target = ImageUtilities.readMBF(new File("car1.jpg"));

        long startTime = System.currentTimeMillis();
        DoGSIFTEngine engine = new DoGSIFTEngine();
        LocalFeatureList<Keypoint> queryKeypoints = engine.findFeatures(query.flatten());
        long endTime = System.currentTimeMillis();
        System.out.println("sift findFeature time:" + (endTime-startTime) + "ms");

        LocalFeatureList<Keypoint> targetKeypoints = engine.findFeatures(target.flatten());

        //LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);

        RobustAffineTransformEstimator modelFItter = new RobustAffineTransformEstimator(5.0,1500,new RANSAC.PercentageInliersStoppingCondition(0.5));
        LocalFeatureMatcher<Keypoint> matcher = new ConsistentLocalFeatureMatcher2d<Keypoint>(new FastBasicKeypointMatcher<Keypoint>(8), modelFItter);

        matcher.setModelFeatures(queryKeypoints);
        matcher.findMatches(targetKeypoints);

        MBFImage basicMatches = MatchingUtilities.drawMatches(query,target,matcher.getMatches(), RGBColour.RED);
        DisplayUtilities.display(basicMatches);

        ctx.stop();
        /*JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JLabel(new ImageIcon(bufImage)));
        frame.pack();
        frame.setVisible(true);
        System.out.println(outimg.cols() + ":" + outimg.rows());*/
    }
}
