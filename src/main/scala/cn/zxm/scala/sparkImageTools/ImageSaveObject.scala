package cn.zxm.scala.sparkImageTools

import java.io.ByteArrayInputStream

import cn.zxm.scala.sparkImageTools.ImageLlegalCheck.rm_hdfs
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Try

/**
  * ImageSaveObject:以Object的形式保存图片
  */
object ImageSaveObject {
def main(args: Array[String]): Unit = {
    val dataSet = args(0)
    val task_size = args(1)


    val conf = new SparkConf()
    conf.setAppName("ImageSaveObject")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    val sc = new SparkContext(conf)

    val hdfsHost = "hdfs://hadoop0:9000"   //主机名

    val imagePath = hdfsHost + "/user/root/imgdataset/" + dataSet + "/*" //数据集路径


    val objectFilePath = "/user/root/imgObjectFile/" + dataSet

    rm_hdfs(hdfsHost,objectFilePath)

    val failed_list = sc.binaryFiles(imagePath,task_size.toInt).map(f =>  Try{

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img = SpImageUtilities.readF(sbs)
      (f._1,img)
    }
    ).filter(x => x.isSuccess).map(x => x.get).saveAsObjectFile(hdfsHost+objectFilePath)

    sc.stop()
  }
}
