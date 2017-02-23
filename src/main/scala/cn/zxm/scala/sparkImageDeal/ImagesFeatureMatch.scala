package cn.zxm.scala.sparkImageDeal

import java.io.{File, InputStream, ByteArrayInputStream}

import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.feature.local.list.{LocalFeatureList, MemoryLocalFeatureList}
import org.openimaj.feature.local.matcher.{LocalFeatureMatcher, FastBasicKeypointMatcher}
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.image.{ImageUtilities, MBFImage}
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
    val query_path: String = "car2.jpg"
    val query: MBFImage = ImageUtilities.readMBF(new File(query_path))
    val engine: DoGSIFTEngine = new DoGSIFTEngine
    val queryKeypoints: LocalFeatureList[Keypoint] = engine.findFeatures(query.flatten)
    /*设定的图片的特征点集合*/

    /*特征点集合间的匹配*/
    val match_result = sc.sequenceFile(kpslib_path,classOf[Text],classOf[BytesWritable],5000).map(f => {
      val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
      val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)

      val bis:InputStream = new ByteArrayInputStream(f._2.getBytes)
      val keypoints = MemoryLocalFeatureList.read(bis,classOf[Keypoint])

      matcher.setModelFeatures(queryKeypoints)//设置查询模板

      matcher.findMatches(keypoints)

      if (matcher.getMatches.size() > 150 && matcher.getMatches.size() <= 222)
        (f._1.toString,matcher.getMatches.size())
      else
        null
    })

    val kps = match_result.collect().iterator.foreach(x => {
      if (x != null)
        System.out.println(x)})
    /*特征点集合间的匹配*/

    sc.stop()
  }



}
