package cn.zxm.scala.sparkImageTools

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkContext, SparkConf}

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

    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"

    val hdfsHt = "hdfs://hadoop0:9000"   //主机名

    val dataset = args(0)
    val task_size = args(1)

    /*val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_4 = "dataset_4g"
    val dataset_test = "dataset_test"*/

    val initImgs_path_hdfs = hdfsHt + "/user/root/imgdataset/" + dataset + "/*" //数据集路径

    val prefix_path_hdfs = "hdfs://hadoop0:9000/user/root/imgdataset/" //用于提取特征的key

    val path = "/user/root/imgSq/" + dataset + "/"
    val tmpImageSEQ_path: String = hdfsHt + path

    rm_hdfs(hdfsHt,path)

    //二进制形式读取图片文件
    sc.binaryFiles(initImgs_path_hdfs,task_size.toInt).map(f => {
      //val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key

      val bytes = f._2.toArray()
      val fcontext = new BytesWritable(bytes)//将图片的内容转化成byte字节形式

      (new Text(f._1),fcontext) //以序列化方式保存图片文件到hdfs
    }).saveAsHadoopFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])

    sc.stop()
  }

}
