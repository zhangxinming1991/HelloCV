package cn.zxm.scala.sparkImageDeal

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}
import javax.imageio.ImageIO

import cn.zxm.scala.sparkImageDeal.ImagesFeatureExByPart.{rm_hdfs, serialize}
import cn.zxm.scala.sparkImageTools.ImagesSeByPart.create_Model
import cn.zxm.sparkSIFT.SIFT.ImageSegment
import cn.zxm.sparkSIFT.SparkImage
import org.apache.hadoop.io.{ArrayWritable, BytesWritable, Text, Writable}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.imageLib.ImageBasic.SpFImage
import org.apache.spark.imageLib.SIFT.SpDoGSIFTEngine
import org.apache.spark.imageLib.imageKeyPoint.{SequenceKeyPoint, SequenceKeypointList, SpKeypoint, SpLocalFeatureList}
import org.apache.spark.{HashPartitioner, Partitioner, SparkConf, SparkContext}
import org.openimaj.io.IOUtils

import scala.collection.mutable.ArrayBuffer
import scala.util.Try

object ImagesFeatureExByPart2 {

  class MySparkPartition(numParts: Int) extends Partitioner{
    override def numPartitions: Int = numParts

    override def getPartition(key: Any): Int = {
      val code = key.toString.hashCode % numPartitions
      if(code < 0)
        code + numPartitions
      else
        code
    }

  }

  def main(args: Array[String]) {

    val dataset = args(0)
    val task_size = args(1)
    val part_size = args(2)

    val start_time = System.currentTimeMillis()

    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureExByPart" + "_" + dataset + "_" + task_size + "_" + "model_" + part_size)
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://hadoop0:9000"

    val imgsqdir = "/user/root/img_sq/"
    val kpslibdir = "/user/root/featureSq/"

    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录

    val initImgs_500k_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset + "/*" //数据集路径
    val prefix_path_hdfs = "hdfs://hadoop0:9000/user/root/imgdataset/" //用于提取特征的key

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset)

    //获取图片的子图集合
    val keyImgPartsRdd = sc.binaryFiles(initImgs_500k_path_hdfs,task_size.toInt).persist().map(f =>  Try{{
      val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img_0 = ImageIO.read(sbs)
      val gray_data = SparkImage.GetGrayDate(img_0)//获取图片的BufferImage

      val modelImg:ImageSegment.ModelImg = create_Model(img_0.getHeight,img_0.getWidth) //创建模板
      val imgParts = ImageSegment.DiveImgByModel(gray_data,img_0.getHeight(),img_0.getWidth(),modelImg) //获取图片的分割子图集

      val keyImgParts = new ArrayBuffer[(String,Int,Int,Array[Byte])]()
      for (i<- 0 to imgParts.size()-1){
        val img = imgParts.get(i)
        keyImgParts.append((fname+"#"+i,img.row.get(),img.col.get(),img.sePixels.getBytes))
      }

      keyImgParts.toArray  //返回图片分割后的子图集合{keyname,row,col,pixel} keyname的格式为：文件名_子图序号 pixel为子图灰度像素值
    }}
    ).filter(x => {x.isSuccess}).map(x => {x.get}).flatMap(x => {
      x
    }).map{case (name,row,col,bytes) => {
      (name,(row,col,bytes))//可以进行重新分区优化，按照fname
    }}.partitionBy(new HashPartitioner(10000)).map({case (pname,(row,col,bytes)) => Try{{

      val sfimg = new SpFImage(bytes,col,row)

      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(sfimg)
      val kpslist = new ArrayBuffer[SpLocalFeatureList[SpKeypoint]]()

      kpslist += kps

      val fnkey = pname.toString.split("#")(0)
      (fnkey,kpslist.toList) //形成新的(key,value)  key:文件名，没有加子图序号   value:特征点集合
    }}}).filter(x => x.isSuccess).map(x => x.get).reduceByKey((x,y) => {//将一张图片的所有子图的特征点集合合并
      x++y
    }
    ).map(f => {

      val sqkpslist = new ArrayBuffer[Writable]()//保存所有子图的特征点集合

      val it = f._2.iterator
      while(it.hasNext){// 遍历每一张子图
      val kps = it.next()

        val wkps = new ArrayBuffer[Writable]() //store the writable kps
        val it_kp = kps.iterator()
        while (it_kp.hasNext){//遍历每一张子图中特征点集合
        val kp = it_kp.next()

          val ivec = kp.getFeatureVector.getVector
          val x = kp.getX
          val y = kp.getY
          val ori = kp.ori
          val scale = kp.getScale

          val sqkp = new SequenceKeyPoint(x,y,ori,scale,ivec); //hadoop writable kp
          wkps.append(sqkp)
        }
        val skps = new SequenceKeypointList(classOf[SequenceKeyPoint],wkps.toArray) // hadoop writable kps
        sqkpslist += skps
      }

      val kpsArray = new ArrayWritable(classOf[SequenceKeypointList])//store the kpslist of all the pic
      kpsArray.set(sqkpslist.toArray)

      val wBytes = serialize(kpsArray)//序列化图片特征点集
      (new Text(f._1),new BytesWritable(wBytes))
    }).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/
    val end_time = System.currentTimeMillis()

    System.out.println("use time:" + (end_time-start_time)/1000)

    sc.stop()
  }
}
