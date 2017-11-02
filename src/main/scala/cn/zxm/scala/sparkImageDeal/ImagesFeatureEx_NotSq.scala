package cn.zxm.scala.sparkImageDeal

import java.io.{ByteArrayInputStream, ByteArrayOutputStream, InputStream}
import java.net.URI

import org.apache.spark.imageLib.imageKeyPoint.{SpKeypoint, SpMemoryLocalFeatureList}
import org.apache.spark.imageLib.SIFT.SpDoGSIFTEngine

import scala.util.{Failure, Success, Try}
import org.apache.spark.imageLib.ImageBasic.SpImageUtilities
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.openimaj.io.IOUtils

/**
  * Created by root on 17-4-11.
  */
object ImagesFeatureEx_NotSq {
  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureEx_NotSq")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    val sc = new SparkContext(conf)

    val hdfsHt = "hdfs://hadoop0:9000"   //主机名

    val dataset = args(0)
    val task_size = args(1)

    val kpslibdir = "/user/root/featureSq/"
    val kpslib_path = hdfsHt + kpslibdir + dataset + "/" //特征库目录
    val initImgs_path_hdfs = hdfsHt + "/user/root/imgdataset/" + dataset + "/*" //数据集路径

    val prefix_path_hdfs = "hdfs://hadoop0:9000/user/root/imgdataset/" //用于提取特征的key

    var errorReadFileCount = sc.longAccumulator //读错误共享累加器
    var errorExactCount = sc.longAccumulator //提取错误共享累加器

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfsHt,kpslibdir + dataset)

    sc.binaryFiles(initImgs_path_hdfs,task_size.toInt).persist().map(f => Try{

//      val fileName = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))
      val bytes = f._2.toArray()

      val dataInput:InputStream = new ByteArrayInputStream(bytes)
      val img = SpImageUtilities.readF(dataInput)

      (f._1,img)

    }).map(x => Try{
      x match {
        case Success(v) =>{//读取图片文件成功

          val data = x.get
          val engine = new SpDoGSIFTEngine()
          val kps = engine.findFeatures(data._2)

          val baos: ByteArrayOutputStream =new ByteArrayOutputStream()
          IOUtils.writeBinary(baos, kps)

          (new Text(data._1),new BytesWritable(baos.toByteArray))
        }
        case Failure(e) => {//读取图片文件失败

          errorReadFileCount.add(1)
          //val data = x.get  //!!!error:x 的值不能使用

          val kps = new SpMemoryLocalFeatureList[SpKeypoint](0)//返回特征点集合长度为0

          val baos: ByteArrayOutputStream =new ByteArrayOutputStream()
          IOUtils.writeBinary(baos, kps)

          (new Text("error"),new BytesWritable(baos.toByteArray))
        }
      }
    }).map(x => Try{
      x match {
        case Success(v) => x.get
        case Failure(e) => {
          errorExactCount.add(1)
          x.get
        }
      }

    }).filter(x => x.isSuccess).map(x => x.get).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])

    System.out.println("***errorReadFileCount:" + errorReadFileCount.sum)
    System.out.println("***errorExactCount:" + errorExactCount.sum)

    sc.stop()
  }

}
