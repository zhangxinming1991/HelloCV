#!/bin/sh
spark-submit --class cn.zxm.scala.sparkImageDeal.ImagesFeatureMatch --master spark://hadoop0:7077 /home/hadoop0/Public/HelloCV/target/spark-SIFT-1.0-SNAPSHOT.jar dataset_280m /home/hadoop0/Pictures/query/ILSVRC2012_val_00021324.JPEG
