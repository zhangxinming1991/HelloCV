package cn.zxm.scala.sparkImageTools

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkContext, SparkConf}
import org.apache.spark.sql.SparkSession
import org.openimaj.hadoop.tools.sequencefile.SequenceFileTool

/**
  * Created by root on 17-2-16.
  * ImagesSequence 图片文件的序列化
  */
object ImagesSequence {
  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesSequence")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"
    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"
    val tmpImageSEQ_path: String = "hdfs://simon-Vostro-3905:9000/user/root/img_sq/"

    /*val cmdArgs: Array[String] = Array[String]("-m", "CREATE", "-kns", "FILENAME", "-o", tmpImageSEQ_path, "imgdataset/car1.jpg","imgdataset/car2.jpg",
    "imgdataset/car3.jpg","imgdataset/car4.jpg")

    SequenceFileTool.main(cmdArgs)*/

    rm_hdfs(hdfs_htname,"/user/root/img_sq/")

    sc.binaryFiles(initImgs_path,50).map(f => {
      System.out.println(f._1.substring(prefix_path.length,f._1.length))
      val fname = new Text(f._1.substring(prefix_path.length,f._1.length))
      val bytes = f._2.toArray()

      val fcontext = new BytesWritable(bytes)
      (fname,fcontext)
    }).saveAsHadoopFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])

    sc.stop()
  }

}
