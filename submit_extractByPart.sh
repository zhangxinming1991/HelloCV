#!/bin/sh
spark-submit --class cn.zxm.scala.sparkImageDeal.ImagesFeatureExByPart --master spark://hadoop0:7077 /home/hadoop0/Public/HelloCV/target/spark-SIFT-1.0-SNAPSHOT.jar dataset_480m 100 rand
