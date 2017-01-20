package rddtest

import org.apache.spark.HashPartitioner
import org.apache.spark.sql.SparkSession
;

object RddTest{
  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("RddTest")
      .getOrCreate()

    val sc = spark.sparkContext
    val rdd1 = sc.parallelize(Array((1,2),(1,3),(3,1),(5,2)),4)
    val hrdd = rdd1.partitionBy(new HashPartitioner(3)).persist()
    //hrdd.get
    val cr = hrdd.reduce((a,b) => {(a._1,a._2 + b._2)})
    System.out.println(cr)
    //hrdd.glom()


  }
}