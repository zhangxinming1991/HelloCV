package cn.zxm.scala.sparkImageDeal

import org.apache.spark.sql.SparkSession
import org.openimaj.image.ImageUtilities
import org.openimaj.image.feature.local.engine.DoGSIFTEngine


/**
  * Created by root on 17-2-8.
  */
object ImagesFeatureEx {

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("ImagesFeatureEx")
      .getOrCreate()

    val sc = spark.sparkContext

    val hdfs_name = "hdfs://simon-Vostro-3905:9000"
    val pt_s = "/user/root/car_small_scale/"

    val imgs = sc.binaryFiles(hdfs_name+pt_s,1).map({case (x,y) => {//以二进制方式读取图片集合？如果采用objectfile方式或者是sequencefile方式是否会处理更快
    /*将所有图片灰度化处理*/
    val targetImg = ImageUtilities.readMBF(y.open())
      val engine = new DoGSIFTEngine()
      val queryKeypoints = engine.findFeatures(targetImg.flatten())
      queryKeypoints
}})

    val x = imgs.collect()
    System.out.println("***" + x.length)
    x.iterator.foreach(x => System.out.println(x))

    sc.stop()
}

}
