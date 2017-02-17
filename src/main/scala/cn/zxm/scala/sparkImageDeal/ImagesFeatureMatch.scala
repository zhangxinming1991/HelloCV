package cn.zxm.scala.sparkImageDeal

import java.io.{InputStream, ByteArrayInputStream}

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.feature.local.list.MemoryLocalFeatureList
import org.openimaj.feature.local.matcher.{LocalFeatureMatcher, FastBasicKeypointMatcher}
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC

/**
  * Created by root on 17-2-17.
  */
object ImagesFeatureMatch {

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureMatch")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val testout = "hdfs://simon-Vostro-3905:9000/user/root/testout/" //特征库目录

    /*设定的图片的特征点集合 ??? 可以使用 map方式或者更加高效的获取方式获取查询的图片的特征点集合*/
    val query_fname = "car2.jpg";
    val kps_car2 = HadoopSerializationUtil.getKPLFromSequence(new Text(query_fname),testout)
    /*设定的图片的特征点集合*/

    /*特征点集合间的匹配*/
    val match_result = sc.sequenceFile(testout,classOf[Text],classOf[BytesWritable],50).map(f => {
      val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)

      val bis:InputStream = new ByteArrayInputStream(f._2.getBytes)
      val keypoints = MemoryLocalFeatureList.read(bis,classOf[Keypoint])

      matcher.setModelFeatures(kps_car2)//设置查询模板

      if (!f._1.toString.equals(query_fname)){//查询模板之外的所有图片
        matcher.findMatches(keypoints)
      }

      (f._1.toString,matcher.getMatches.size())
    })
    val kps = match_result.collect().iterator.foreach(x => System.out.println(x))
    /*特征点集合间的匹配*/
  }


}
