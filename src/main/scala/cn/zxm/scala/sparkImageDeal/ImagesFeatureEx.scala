package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.{URL, URI}

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
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
object ImagesFeatureEx {
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

    val conf = new SparkConf()
    conf.setAppName("SequenceKeypoints")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"

    val imgsqdir = "/user/root/img_sq/"
    val kpslibdir = "/user/root/featureSq/"

    val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"

    val imageSEQ_path = hdfs_htname + imgsqdir + dataset_2 + "/*"
    val kpslib_path = hdfs_htname + kpslibdir + dataset_2 + "/" //特征库目录

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset_2)
    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],1000).map({case (fname,fcontext) => {

      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      val img = SpImageUtilities.readF(datainput)
      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(img)

      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, kps)

      (new Text(fname.toString),new BytesWritable(baos.toByteArray))
      //(new Text(fname.toString),new IntWritable(kps.size()))

    }}).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/

    sc.stop()
  }

}
