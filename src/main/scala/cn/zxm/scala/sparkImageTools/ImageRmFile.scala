package cn.zxm.scala.sparkImageTools

import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark._

/**
  * Created by root on 17-5-29.
  */
object ImageRmFile {

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]): Unit = {
    val dataSet = args(0)
    val task_size = args(1)


    val conf = new SparkConf()
    conf.setAppName("ImageRmFile")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    val sc = new SparkContext(conf)

    val hdfsHost = "hdfs://hadoop0:9000"   //主机名


    val errorLog = "/user/root/illegalPictures/"

    val failed_list = sc.textFile(hdfsHost+errorLog,task_size.toInt).map(f =>   {

      val fname = f.split(",")(0)

      rm_hdfs(hdfsHost,fname.substring(hdfsHost.length+1))

      fname.substring(1)
    }).collect().foreach(x => System.out.println(x))

    sc.stop()
  }
}

