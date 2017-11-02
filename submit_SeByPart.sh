#!/bin/sh
spark-submit --class cn.zxm.scala.sparkImageTools.ImagesSeByPart --master spark://hadoop0:7077 /home/hadoop0/Public/HelloCV/target/spark-SIFT-1.0-SNAPSHOT.jar dataset_280m 50
