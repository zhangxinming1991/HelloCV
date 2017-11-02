package cn.zxm.scala.sparkImageTools

import java.io.{ByteArrayInputStream}
import java.net.URI

import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark._

/**
  * Created by root on 17-5-29.
  */
object ImageLlegalCheck {

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  class MySparkPartition(numParts: Int) extends Partitioner{
    override def numPartitions: Int = numParts

    override def getPartition(key: Any): Int = {
      val code = key.toString.split("#")(0).hashCode % numPartitions
      if(code < 0)
        code + numPartitions
      else
        code
    }

  }

  def main(args: Array[String]): Unit = {
    val dataSet = args(0)
    val task_size = args(1)


    val conf = new SparkConf()
    conf.setAppName("ImageLegalCheck")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    val sc = new SparkContext(conf)

    val hdfsHost = "hdfs://hadoop0:9000"   //主机名

    val imagePath = hdfsHost + "/user/root/imgdataset/" + dataSet + "/*" //数据集路径


    val errorLog = "/user/root/illegalPictures/"
    var errorReadFileCount = sc.longAccumulator

    rm_hdfs(hdfsHost,errorLog)

    val failed_list = sc.binaryFiles(imagePath,task_size.toInt).map(f =>  try {

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img = SpImageUtilities.readF(sbs)

      null
////      if(img == null){
////        val pt: Path = new Path(f._1)
////        val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
////        fs.delete(pt, true)
////        f._1
////      }
//
//      else
//        null
    //  (f._1,"ok")
    //  null
    }catch {case e:Exception=>{
      errorReadFileCount.add(1)
      (f._1.toString,e.getMessage)
    }
    }
    ).filter(x => {x != null}).saveAsTextFile(hdfsHost+errorLog)

    System.out.println("errorReadFileCount:" + errorReadFileCount.sum)

    sc.stop()
  }
}
