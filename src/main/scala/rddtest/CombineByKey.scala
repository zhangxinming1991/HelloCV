package rddtest

import java.util

import org.apache.spark.sql.SparkSession

/**
  * Created by root on 17-1-9.
  */
object CombineByKey {
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("Aggregate")
      .getOrCreate()

    val sc = spark.sparkContext
    val z = sc.parallelize(List("a","b","c","a"),2)
    val gz = z.map(x => (x,1)).groupByKey()
    val cgz = gz.collect()
    //val result = z.map(x => (x,1)).combineByKey(List(_),)
  }
}
