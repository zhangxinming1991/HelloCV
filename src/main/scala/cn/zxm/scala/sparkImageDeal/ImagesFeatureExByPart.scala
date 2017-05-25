package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.URI

import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SequenceImage, SpImageUtilities}
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import cn.zxm.sparkSIFT.imageKeyPoint._
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io
import org.apache.hadoop.io.{ArrayWritable, Writable, BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.io.IOUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-28.
  */
object ImagesFeatureExByPart{
  @throws(classOf[IOException])
  def serialize(writable: Writable): Array[Byte] = {
    val out: ByteArrayOutputStream = new ByteArrayOutputStream
    val dataout: DataOutputStream = new DataOutputStream(out)
    writable.write(dataout)
    dataout.close
    return out.toByteArray
  }

  @throws(classOf[IOException])
  def deseriable(write: Writable, bytes: Array[Byte]) {
    val in: ByteArrayInputStream = new ByteArrayInputStream(bytes)
    val datain: DataInputStream = new DataInputStream(in)
    write.readFields(datain)
    datain.close
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

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"

    val imgsqdir = "/user/root/img_sq/"
    val kpslibdir = "/user/root/featureSq/"

    val imageSEQ_path = hdfs_htname + imgsqdir + dataset + "/*"
    val kpslib_path = hdfs_htname + kpslibdir + dataset + "/" //特征库目录

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset)
    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],task_size.toInt).map({case (fname,fcontext) => {

      val sqimg = new SequenceImage()
      deseriable(sqimg,fcontext.getBytes)
      val sfimg = new SpFImage(sqimg.sePixels.getBytes,sqimg.col.get(),sqimg.row.get())

      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(sfimg)
      val kpslist = new ArrayBuffer[SpLocalFeatureList[SpKeypoint]]()

      kpslist += kps

      val fnkey = fname.toString.split("#")(0)
      (fnkey,kpslist.toList) //形成新的(key,value)  key:文件名，没有加子图序号   value:特征点集合
    }}).reduceByKey((x,y) => {//将一张图片的所有子图的特征点集合合并
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
    }).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/
    val end_time = System.currentTimeMillis()

    System.out.println("use time:" + (end_time-start_time)/1000)

    sc.stop()
  }

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }


}
