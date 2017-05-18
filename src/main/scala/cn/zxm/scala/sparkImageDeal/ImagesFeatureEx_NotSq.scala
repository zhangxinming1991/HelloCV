package cn.zxm.scala.sparkImageDeal

import java.io.{ByteArrayOutputStream, ByteArrayInputStream, InputStream}
import java.net.URI

import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkConf}
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
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    /*val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"*/

    val dataset = args(0)
    System.out.println("***hello:" + dataset)
    val kpslibdir = "/user/root/featureSq/"
    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录
    val initImgs_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset + "/*" //数据集路径

    val prefix_path_hdfs = "hdfs://simon-Vostro-3905:9000/user/root/imgdataset/" //用于提取特征的key

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset)

    sc.binaryFiles(initImgs_path_hdfs,500).map(f => {
      val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key
      val bytes = f._2.toArray()

      val datainput:InputStream = new ByteArrayInputStream(bytes)
      val img = SpImageUtilities.readF(datainput)
      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(img)

      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, kps)

      (new Text(fname.toString),new BytesWritable(baos.toByteArray))
    }).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])

    sc.stop()
  }

}
