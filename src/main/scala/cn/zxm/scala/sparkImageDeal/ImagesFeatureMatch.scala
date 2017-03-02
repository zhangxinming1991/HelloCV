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
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureMatch")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"

    val kpslibdir = "/user/root/featureSq/"

    val kpslib_path = hdfs_htname + kpslibdir + dataset_test + "/" //特征库目录

    //val testout = "hdfs://simon-Vostro-3905:9000/user/root/testout/" //特征库目录

    /*设定的图片的特征点集合 ??? 可以使用 map方式或者更加高效的获取方式获取查询的图片的特征点集合*/
 //   val query_fname = dataset + "/car2.jpg";
 //   val kps_car2 = HadoopSerializationUtil.getKPLFromSequence(new Text(query_fname),kpslib_path)
    val query_path: String = "/home/simon/Public/spark-SIFT/query/205600.jpg"
    val query = SpImageUtilities.readF(new FileInputStream(new File(query_path)))
    val engine = new SpDoGSIFTEngine()
    val queryKeypoints = engine.findFeatures(query)
    /*设定的图片的特征点集合*/

    /*特征点集合间的匹配*/
    val match_result = sc.sequenceFile(kpslib_path,classOf[Text],classOf[BytesWritable],500).map(f => {
      val modelFItter = new SpRobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher= new SpConsistentLocalFeatureMatcher2d(new SpFastBasicKeypointMatcher[SpKeypoint](8), modelFItter)

     // val bis:InputStream = new ByteArrayInputStream(f._2.getBytes)
     // val keypoints = SpMemoryLocalFeatureList.read(bis,classOf[SpKeypoint])

      matcher.setModelFeatures(queryKeypoints)//设置查询模板
      var match_size = 0;

      val wkpsPics = new ArrayWritable(classOf[SequenceKeypointList])
      deseriable(wkpsPics,f._2.getBytes)//反序列化

      val kpsPics = wkpsPics.get()
      //f._2.getClass
      val match_pair = new ArrayBuffer[(SpKeypoint,SpKeypoint)]()
      for (i <- 0 to kpsPics.size-1){
        val kpspic = SequenceKeypointList.changeWriteToSq(kpsPics(i))
        matcher.findMatches(SequenceKeypointList.GetListKps(kpspic.kps.get()))
        match_size = match_size + matcher.getMatches.size()

        //收集匹配的点集合，根据匹配点的坐标相同程度，可以进行排错？是否RANSAC有错误
    /*    val it_pair = matcher.getMatches.iterator()
        while (it_pair.hasNext){
          val pair = it_pair.next()
          val pair_s = (pair.getFirstObject,pair.getSecondObject)
          match_pair += pair_s
        }*/
      }

      if (match_size > 1000 && matcher.getMatches.size() < 1614){
        (f._1.toString,match_pair)
      }
      else
        null
    })

    val kps = match_result.collect()
    /*kps.iterator.foreach(
      x =>{
        if(x != null && !x._1.equals("dataset_test/02/207800.jpg")){
          x._2.foreach(y => System.out.println(y))
          System.out.println("match size:" + x._2.size)
        }


      })*/

    /*kps(0)._2.foreach(x => {
      if (x != null){
        System.out.println(x)
      }
    })*/
    System.out.println(queryKeypoints.size())

    /*特征点集合间的匹配*/

    sc.stop()
  }



}
