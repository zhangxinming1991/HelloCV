package rddtest

import org.apache.spark.sql.SparkSession

/**
  * Created by root on 17-1-8.
  */
object Aggregate {

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("Aggregate")
      .getOrCreate()

    def myfunc(index:Int,iter:Iterator[(Int)]):Iterator[String] = {
      iter.toList.map(x => "partID:" + index + ",val:" + x).iterator
    }

    val sc = spark.sparkContext

    val z = sc.parallelize(List(1,2,3,4,5,6),2)
    val cz = z.mapPartitionsWithIndex(myfunc).collect()
    val az = z.aggregate(0)(math.max(_,_),_ + _)
    System.out.println(az)
  }
}
