package cn.zxm.scala.sparkImageDeal

import java.io._


import org.apache.spark.imageLib.ImageBasic.SpImageUtilities
import org.apache.spark.imageLib.ImageMatch.{SpFastBasicKeypointMatcher, SpConsistentLocalFeatureMatcher2d, SpRobustAffineTransformEstimator}
import org.apache.spark.imageLib.SIFT.SpDoGSIFTEngine
import org.apache.spark.imageLib.imageKeyPoint.{SequenceKeypointList, SpKeypoint}
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.io.{ArrayWritable, BytesWritable, Text, Writable}
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.math.model.fit.RANSAC

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-4-4.
  * 分割方式的匹配
  */
object ImagesFeatureMatch_PartWay {
  @throws(classOf[IOException])
  def serialize(writable: Writable): Array[Byte] = {
    val out: ByteArrayOutputStream = new ByteArrayOutputStream
    val dataout: DataOutputStream = new DataOutputStream(out)
    writable.write(dataout)
    dataout.close
    return out.toByteArray
  }

  @throws(classOf[IOException])
  def deseriable(write: Writable, bytes: Array[Byte]) {
    val in: ByteArrayInputStream = new ByteArrayInputStream(bytes)
    val datain: DataInputStream = new DataInputStream(in)
    write.readFields(datain)
    datain.close
  }

  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureMatch_PartWay")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val dataset = args(0)

    val query_path = args(1)

    val hdfs_htname = "hdfs://hadoop0:9000"   //主机名

    val kpslibdir = "/user/root/featureSq/"

    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录

    System.out.println("***query_file:" + query_path)
    val query = SpImageUtilities.readF(new FileInputStream(new File(query_path)))
    val engine = new SpDoGSIFTEngine()
    val queryKeypoints = engine.findFeatures(query)
    /*设定的图片的特征点集合*/

    /*特征点集合间的匹配*/
    val match_result = sc.sequenceFile(kpslib_path,classOf[Text],classOf[BytesWritable],100).map(f => try {
      val modelFItter = new SpRobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher= new SpConsistentLocalFeatureMatcher2d(new SpFastBasicKeypointMatcher[SpKeypoint](8), modelFItter)

      matcher.setModelFeatures(queryKeypoints)//设置查询模板
      var match_size = 0;

      val wkpsPics = new ArrayWritable(classOf[SequenceKeypointList])
      deseriable(wkpsPics,f._2.getBytes)//反序列化

      val kpsPics = wkpsPics.get()

      val match_pair = new ArrayBuffer[(SpKeypoint,SpKeypoint)]()
      for (i <- 0 to kpsPics.size-1){
        val kpspic = SequenceKeypointList.changeWriteToSq(kpsPics(i))
        matcher.findMatches(SequenceKeypointList.GetListKps(kpspic.kps.get()))
        match_size = match_size + matcher.getMatches.size()
      }

      if (match_size > (queryKeypoints.size()- queryKeypoints.size()/2) && match_size < (queryKeypoints.size())){
        (f._1.toString,match_size)
      }
      else{
        null
      }

    }catch {case e:Exception=>{(f._1.toString,e.getMessage)}
    }).filter(x => {x != null})

    var counter = 0

    val kps = match_result.collect()
    kps.iterator.foreach(
      x =>{
          System.out.println(x._1+ "|" + "match size:" + x._2)
          counter = counter + 1
      })
    System.out.println("fit_num:" + counter)
    System.out.println(queryKeypoints.size())

    /*特征点集合间的匹配*/
    sc.stop()
  }

}
