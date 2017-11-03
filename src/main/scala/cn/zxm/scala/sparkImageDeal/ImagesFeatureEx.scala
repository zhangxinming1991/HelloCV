package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.URI

import org.apache.spark.imageLib.ImageBasic.SpImageUtilities
import org.apache.spark.imageLib.SIFT.SpDoGSIFTEngine
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.openimaj.io.IOUtils

import scala.util.{Failure, Success, Try}

/**
  * Created by root on 17-2-13.
  */
object ImagesFeatureEx {
  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
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

    val hdfsHt = "hdfs://hadoop0:9000"   //主机名

    val imgsqdir = "/user/root/imgSq/"
    val kpslibdir = "/user/root/featureSq/"

    val imageSEQ_path = hdfsHt + imgsqdir + dataset + "/*"
    val kpslib_path = hdfsHt + kpslibdir + dataset + "/" //特征库目录

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfsHt,kpslibdir + dataset)

    //读取hdfs中图像的序列化文件
    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],task_size.toInt).map(f => {

      var datainput:InputStream = new ByteArrayInputStream(f._2.getBytes)
      var img = SpImageUtilities.readF(datainput)//读取图片的像素矩阵

      var engine = new SpDoGSIFTEngine()//构建高斯差分金子塔
      var kps = engine.findFeatures(img)//查找特征点

      val baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, kps)//将提取到的特征点转化成二进制模式

      (new Text(f._1.toString),new BytesWritable(baos.toByteArray))//以序列化的方式保存提取到的图片的特征点集合

    }).persist().saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/

    sc.stop()
  }

}
