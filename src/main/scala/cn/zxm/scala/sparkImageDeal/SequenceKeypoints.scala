package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.{URL, URI}

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{SequenceFile, IntWritable, BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SparkSession
import org.apache.spark.storage.StorageLevel
import org.openimaj.feature.local.list.{MemoryLocalFeatureList, LocalFeatureListUtils}
import org.openimaj.feature.local.matcher.{MatchingUtilities, FastBasicKeypointMatcher, LocalFeatureMatcher}
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.feature.local.keypoints.Keypoint

import org.openimaj.image.{DisplayUtilities, ImageUtilities, MBFImage}
import org.openimaj.io.IOUtils
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC

/**
  * Created by root on 17-2-13.
  */
object SequenceKeypoints {
  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  /**
    * 匹配两张图片
 *
    * @param targetImg
    * @param queryImg
    * @param targetKps
    * @param queryKps
    */
  def match_two_img(targetImg:MBFImage,queryImg:MBFImage,targetKps:MemoryLocalFeatureList[Keypoint],queryKps:MemoryLocalFeatureList[Keypoint]): Unit ={

    val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)

    matcher.setModelFeatures(queryKps)
    matcher.findMatches(targetKps)

    System.out.println("find match size:" + matcher.getMatches.size())

    val basicMatches: MBFImage = MatchingUtilities.drawMatches(queryImg, targetImg, matcher.getMatches, RGBColour.RED)
    DisplayUtilities.display(basicMatches)
  }

  def main(args: Array[String]) {
    /*val query: MBFImage = ImageUtilities.readMBF(new File("car2.jpg"))
    val engine = new DoGSIFTEngine()
    val keypoints = engine.findFeatures(query.flatten())

    val int_ser = new IntWritable(4)
    val ser_result = HadoopSerializationUtil.serialize(int_ser)
    System.out.println(keypoints.size())*/

    val conf = new SparkConf()
    conf.setAppName("SequenceKeypoints")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"
    val tmpImageSEQ_path: String = "hdfs://simon-Vostro-3905:9000/user/root/img_sq/"
    val kpsSEQ_path = "hdfs://simon-Vostro-3905:9000/user/root/featureSq"
    val testout = "hdfs://simon-Vostro-3905:9000/user/root/testout/" //特征库目录



    /*提取图片集合的特征点,建立特征库*/

    /*val fn_rdd = sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],9).map({case (fname,fcontext) => {
      System.out.println("extract " + fname + "feature start");

      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      val img = ImageUtilities.readMBF(datainput)

      System.out.println("extract " + fname + "feature end");
      img
    }}).persist().collect()*/

    rm_hdfs(hdfs_htname,"/user/root/testout/")
    val fn_rdd = sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],40).map({case (fname,fcontext) => {
      System.out.println("extract " + fname + "feature start");

      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      val img = ImageUtilities.readMBF(datainput)
      val engine = new DoGSIFTEngine()
      val kps = engine.findFeatures(img.flatten())

      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, kps)

      System.out.println("extract " + fname + "feature end");

      (new Text(fname.toString),new BytesWritable(baos.toByteArray))

    }}).saveAsHadoopFile(testout,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/

    sc.stop()
  }

}
