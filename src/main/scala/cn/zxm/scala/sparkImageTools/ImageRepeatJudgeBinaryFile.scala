package cn.zxm.scala.sparkImageTools

import org.apache.spark.{SparkConf, SparkContext}

/**
  * ImageRepeatJudgeBinaryFile:检测图片是否有重复的key，读取图片方式为binaryFiles
  */
object ImageRepeatJudgeBinaryFile {
  def main(args: Array[String]) {
    //普通的提取方式对应的匹配
    val conf = new SparkConf()
    conf.setAppName("ImageRepeatJudgeBinaryFile")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)
    val hdfsHost = "hdfs://hadoop0:9000" //主机名

    val dataSet = args(0)
    val taskSize = args(1)


    val imagePath = hdfsHost + "/user/root/imgdataset/" + dataSet + "/*" //数据集路径

    val match_result = sc.binaryFiles(imagePath,taskSize.toInt).map(f => {

      (f._1.toString, 1)
    }).reduceByKey((x,y)=>{x+y}).filter(x => {x._2 > 1}).collect().foreach(x => System.out.println(x))

    sc.stop()
  }
}
