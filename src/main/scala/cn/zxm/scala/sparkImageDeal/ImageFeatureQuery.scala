package cn.zxm.scala.sparkImageDeal

import java.io.{File, ByteArrayInputStream, InputStream}

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.feature.local.list.{LocalFeatureList, MemoryLocalFeatureList}
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.{ImageUtilities, MBFImage}
import org.openimaj.image.feature.local.keypoints.Keypoint

/**
  * Created by root on 17-2-18.
  */
object ImageFeatureQuery {

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureMatch")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val testout = "hdfs://simon-Vostro-3905:9000/user/root/testout/" //特征库目录

    /*设定的图片的特征点集合 ??? 可以使用 map方式或者更加高效的获取方式获取查询的图片的特征点集合*/
    val query_fname = "iaprtc12/00/112.jpg";
    //val kps_car2 = HadoopSerializationUtil.getKPLFromSequence(new Text(query_fname),testout)
    /*设定的图片的特征点集合*/
    sc.sequenceFile(testout,classOf[Text],classOf[BytesWritable],40).map(f => {
      if (f._1.toString.equals(query_fname)){
        val bis:InputStream = new ByteArrayInputStream(f._2.getBytes)
        val keypoints = MemoryLocalFeatureList.read(bis,classOf[Keypoint])
        (f._1.toString,keypoints.size())
      }

      else
        null
    }).collect().iterator.foreach(x => {
      if (x != null)
        System.out.println(x)
    })

    val query_path: String = "/home/simon/Desktop/iaprtc12/images/00/112.jpg"
    val query: MBFImage = ImageUtilities.readMBF(new File(query_path))
    val engine: DoGSIFTEngine = new DoGSIFTEngine
    val queryKeypoints: LocalFeatureList[Keypoint] = engine.findFeatures(query.flatten)

    System.out.println(queryKeypoints.size())
    sc.stop()
  }

}
