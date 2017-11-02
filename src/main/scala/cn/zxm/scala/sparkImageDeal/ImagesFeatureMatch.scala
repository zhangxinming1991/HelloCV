package cn.zxm.scala.sparkImageDeal

import java.io._

import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.ImageMatch.{SpFastBasicKeypointMatcher, SpConsistentLocalFeatureMatcher2d, SpRobustAffineTransformEstimator}
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import cn.zxm.sparkSIFT.imageKeyPoint.{SpPair, SequenceKeypointList, SpKeypoint, SpMemoryLocalFeatureList}
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.io.{Writable, ArrayWritable, BytesWritable, Text}
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.feature.local.list.{LocalFeatureList, MemoryLocalFeatureList}
import org.openimaj.feature.local.matcher.{LocalFeatureMatcher, FastBasicKeypointMatcher}
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.{ImageUtilities, MBFImage}
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-17.
  * 普通方式下的匹配
  */
object ImagesFeatureMatch {
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
    //普通的提取方式对应的匹配
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureMatch")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)
    val hdfs_htname = "hdfs://hadoop0:9000" //主机名

    val dataset = args(0)
    val query_path = args(1)

    val kpslibdir = "/user/root/featureSq/"
    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录

    val query: MBFImage = ImageUtilities.readMBF(new File(query_path))
    val engine: DoGSIFTEngine = new DoGSIFTEngine
    val queryKeypoints: LocalFeatureList[Keypoint] = engine.findFeatures(query.flatten)
    /*设定的图片的特征点集合*/
    /*特征点集合间的匹配*/
    val match_result = sc.sequenceFile(kpslib_path,classOf[Text],classOf[BytesWritable],100).map(f => try {


      val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)
      val bis:InputStream = new ByteArrayInputStream(f._2.getBytes)
      val keypoints = MemoryLocalFeatureList.read(bis,classOf[Keypoint])
      matcher.setModelFeatures(queryKeypoints)//设置查询模板
      matcher.findMatches(keypoints)

      val match_size = matcher.getMatches.size()
      if (match_size > (queryKeypoints.size()- queryKeypoints.size()/2) && match_size < (queryKeypoints.size()))
        (f._1.toString,matcher.getMatches.size())
      else
        null
      }catch {case e:Exception=>{(f._1.toString,e.getMessage)}
      }
    ).filter(x => {x != null})

    var counter = 0

    match_result.collect().iterator.foreach(x => {
      System.out.println(x)
      counter += 1
    })

    /*特征点集合间的匹配*/
    System.out.println("fit_num:" + counter)
    System.out.println("query:" + queryKeypoints.size())
    sc.stop()
  }
}
