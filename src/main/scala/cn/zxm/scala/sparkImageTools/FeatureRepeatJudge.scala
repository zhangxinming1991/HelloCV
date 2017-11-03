package cn.zxm.scala.sparkImageTools

import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.spark.{SparkConf, SparkContext}

/**
  * FeatureRepeatJudge:检测特征点集是否有重复key
  */
object FeatureRepeatJudge {

  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setAppName("FeatureRepeatJudge")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)
    val hdfs_htname = "hdfs://hadoop0:9000" //主机名

    val dataset = args(0)
    val taskSize = args(1)
    val kpslibdir = "/user/root/featureSq/"
    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录


    val match_result = sc.sequenceFile(kpslib_path,classOf[Text],classOf[BytesWritable],taskSize.toInt).map(f => {

      (f._1.toString, 1)
    }).reduceByKey((x,y)=>{x+y}).filter(x => {x._2 > 1}).collect().foreach(x => System.out.println(x))

    sc.stop()
  }
}
