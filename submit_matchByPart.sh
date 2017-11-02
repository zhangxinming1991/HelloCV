#!/bin/sh
spark-submit --class cn.zxm.scala.sparkImageDeal.ImagesFeatureMatch_PartWay --master spark://hadoop0:7077 /home/hadoop0/Public/HelloCV/target/spark-SIFT-1.0-SNAPSHOT.jar dataset_200m /home/hadoop0/Pictures/query/206400.jpg
