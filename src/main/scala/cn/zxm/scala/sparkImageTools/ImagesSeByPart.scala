package cn.zxm.scala.sparkImageTools

import java.io._
import java.net.URI
import javax.imageio.ImageIO

import cn.zxm.sparkSIFT.ImageBasic.{SequenceImage, SpFImage, SpImageUtilities}
import cn.zxm.sparkSIFT.ImageMatch.SpDisplayUtilities
import cn.zxm.sparkSIFT.SIFT.ImageSegment
import cn.zxm.sparkSIFT.SIFT.ImageSegment.ModelImg
import cn.zxm.sparkSIFT.SparkImage
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io
import org.apache.hadoop.io._
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.io.IOUtils

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-27.
  */
object ImagesSeByPart {

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

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesSeByPart")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"

    val initImgs_500k_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset_3 + "/*" //数据集路径
    val inttImgs_path_local = dataset_3 + "/05/*"

    val prefix_path_hdfs = "hdfs://simon-Vostro-3905:9000/user/root/imgdataset/" //用于提取特征的key

    val path = "/user/root/img_sq/" + dataset_3 + "/"
    val tmpImageSEQ_path: String = hdfs_htname + path

    rm_hdfs(hdfs_htname,path)

    val keyImgParts = sc.binaryFiles(inttImgs_path_local,100).map(f => {
      val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key

      val bytes = f._2.toArray()

      val sbs = new ByteArrayInputStream(bytes)

      //val img_0 = ImageIO.read(sbs)
      //val gray_data = SparkImage.GetGrayDate(img_0)

      val img = SpImageUtilities.readF(sbs)

      val mdrow = img.getRows/4;
      val mdcol = img.getCols/3;

      val modelImg:ImageSegment.ModelImg = new ModelImg(mdrow,mdcol)
      val imgParts = ImageSegment.DiveImgByModel(img,modelImg)

      //val m  //构成一个(key,value),其中key为文件名+part_num,value图片每一部分的内容
      var i = 0;
      val keyImgParts = new ArrayBuffer[(String,SpFImage)]()
      val test = new ArrayBuffer[Int]()
      for (i<- 0 to imgParts.size()-1){
        val img = imgParts.get(i)
        keyImgParts.append((fname+"_"+i,img))
      }

      keyImgParts.toArray
    }).collect()

    sc.parallelize(keyImgParts,20).flatMap(x => {
      x
    }).map(y => {
      val pixel_onediem = new ArrayBuffer[Writable]()
      val pixels = y._2.pixels
      pixels.foreach(x => {
        x.foreach(y => {
          pixel_onediem += new FloatWritable(y)
        })
      })

      val onediem = new ArrayWritable(classOf[FloatWritable],pixel_onediem.toArray)
      val imgsq = new SequenceImage(y._2.getRows,y._2.getCols,onediem)

      (new Text(y._1),new BytesWritable(serialize(imgsq)))
    }).saveAsHadoopFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]]) //函数里面保存每个图片的一个部分

    sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],20).map(f => {

      val img = new SequenceImage()
      deseriable(img,f._2.getBytes)
      //(img.row.get(),img.col.get())
      if (f._1.toString.indexOf("200000.jpg") != -1){
        val spfimg = new SpFImage(img.GetSeqPixel(),img.col.get(),img.row.get())
        spfimg
      }
      else {
        null
      }
    }).collect().foreach(x => {
      if (x != null)
        SpDisplayUtilities.display(x)
    })

    sc.stop()
  }

}
