package cn.zxm.scala.sparkImageDeal

import java.io.File

import org.apache.spark.sql.SparkSession
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.feature.local.matcher.{FastBasicKeypointMatcher, LocalFeatureMatcher, MatchingUtilities}
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.image.{DisplayUtilities, MBFImage, ImageUtilities}
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
import org.openimaj.math.model.fit.RANSAC


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
    //System.loadLibrary("lapack")
    //System.load("/usr/lib/liblapack.so.3");

    val hdfs_name = "hdfs://simon-Vostro-3905:9000"
    val pt_s = "/user/root/car_small_scale/"

    val query: MBFImage = ImageUtilities.readMBF(new File("car2.jpg"))
    val target: MBFImage = ImageUtilities.readMBF(new File("car1.jpg"))

    /*获取每张图片的特征点集合*/
    val keypoints = sc.binaryFiles(hdfs_name+pt_s,1).map({case (x,y) => {//以二进制方式读取图片集合？如果采用objectfile方式或者是sequencefile方式是否会处理更快
    /*将所有图片灰度化处理*/
    val targetImg = ImageUtilities.readMBF(y.open())
      val engine = new DoGSIFTEngine()
      (x,engine.findFeatures(targetImg.flatten()))
}}).collect()
    /*获取每张图片的特征点集合*/

    val querykeypoint = keypoints(1)._2
    System.out.println("querykeypoint size:" + querykeypoint.size())

    /*特征点集合间匹配*/
    val match_result = sc.parallelize(keypoints,10).map({case(name,point) => {

      //matcher可以定义成静态变量，但是需要序列
      val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)
      matcher.setModelFeatures(querykeypoint)

      matcher.findMatches(point)
      matcher.getMatches.size()
    }}).collect()

    //打印匹配结果
    match_result.iterator.foreach(x => System.out.println(x))

    /*val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)

    matcher.setModelFeatures(keypoints(1)._2)
    matcher.findMatches(keypoints(0)._2)

    val basicMatches: MBFImage = MatchingUtilities.drawMatches(query, target, matcher.getMatches, RGBColour.RED)
    DisplayUtilities.display(basicMatches)*/


    sc.stop()
}

}
