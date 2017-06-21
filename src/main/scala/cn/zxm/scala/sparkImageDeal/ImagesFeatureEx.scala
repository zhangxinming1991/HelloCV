package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.{URL, URI}

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io
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

    val dataset = args(0)
    val task_size = args(1)

    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureEx" + "_" + dataset + "_" + task_size)
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"

    val imgsqdir = "/user/root/img_sq/"
    val kpslibdir = "/user/root/featureSq/"

    val imageSEQ_path = hdfs_htname + imgsqdir + dataset + "/*"
    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset)

    //读取hdfs中图像的序列化文件
    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],task_size.toInt).map({case (fname,fcontext) => {

      var datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      var img = SpImageUtilities.readF(datainput)//读取图片的像素矩阵
      if(img != null){
        var engine = new SpDoGSIFTEngine()//构建高斯差分金子塔
        var kps = engine.findFeatures(img)//查找特征点

        val baos: ByteArrayOutputStream =new ByteArrayOutputStream()
        IOUtils.writeBinary(baos, kps)//将提取到的特征点转化成二进制模式

        img = null
        datainput = null
        engine = null
        kps = null

        (new Text(fname.toString),new BytesWritable(baos.toByteArray))//以序列化的方式保存提取到的图片的特征点集合
      }
      else{
        datainput = null
        img = null

        val test = "null"
        //val pt: Path = new Path(fname.toString)
        //val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
        //fs.delete(pt, true)
        (new Text(fname.toString),new BytesWritable(test.getBytes()))
      }
    }}).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/

    /*val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],task_size.toInt).map({case (fname,fcontext) => {

      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      val img = SpImageUtilities.readF(datainput)
      if(img != null){
        val engine = new SpDoGSIFTEngine()
        val kps = engine.findFeatures(img)
        kps.size()
      }
      else{
        val failed = 0
        failed
      }
    }}).collect()*/

    sc.stop()
  }

}
