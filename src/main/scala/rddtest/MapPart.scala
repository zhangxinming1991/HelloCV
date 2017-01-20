package rddtest

import org.apache.spark.sql.SparkSession

/**
  * Created by root on 17-1-11.
  */
object MapPart {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("MapPart")
      .getOrCreate()

    val sc = spark.sparkContext

    var sig =  Array(1,2,3,4,5)

    val rdd_sig = sc.parallelize(sig,1)
    val map_sig = rdd_sig.mapPartitions(x => {
      var result = List[Double]()
      var i = 0
      var count = 0;
      while(x.hasNext){
        if (i == 0)
          System.out.println(i + ":" + x.next())
        else {
          System.out.println(">0")
          count += x.next()
        }
        i = i + 1
      }
      result.::(count).iterator
    })

    System.out.println(map_sig.collect().iterator.next())
  }


}
