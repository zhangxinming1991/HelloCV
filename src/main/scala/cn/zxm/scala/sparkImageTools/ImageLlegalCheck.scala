package cn.zxm.scala.sparkImageTools

import java.io.ByteArrayInputStream
import java.net.URI
import javax.imageio.ImageIO

import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.Text
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by root on 17-5-29.
  */
object ImageLlegalCheck {

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]): Unit = {
    val dataset = args(0)
    val task_size = args(1)
    val part_size = args(2)

    val conf = new SparkConf()
    conf.setAppName("ImageDesqByPart")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    val initImgs_500k_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset + "/*" //数据集路径

    val prefix_path_hdfs = "hdfs://simon-Vostro-3905:9000/user/root/imgdataset/" //用于提取特征的key

    val failed_list = sc.binaryFiles(initImgs_500k_path_hdfs,task_size.toInt).map(f => {

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img = SpImageUtilities.readF(sbs)
      if(img == null){
        val pt: Path = new Path(f._1)
        val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
        fs.delete(pt, true)
        f._1
      }

      else
        null
    }).collect()

    System.out.println("***bad picture***")
    failed_list.iterator.foreach(x => {
      if(x != null)
        System.out.println(x)
    })
    System.out.println("***bad picture***")

    sc.stop()
  }
}
