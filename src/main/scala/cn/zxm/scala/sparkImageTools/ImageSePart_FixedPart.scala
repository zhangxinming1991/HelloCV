package cn.zxm.scala.sparkImageTools

import java.io.{DataInputStream, ByteArrayInputStream, DataOutputStream, IOException}
import java.net.URI
import javax.imageio.ImageIO

import cn.zxm.sparkSIFT.ImageBasic.SequenceImage
import cn.zxm.sparkSIFT.SIFT.ImageSegment
import cn.zxm.sparkSIFT.SIFT.ImageSegment.ModelImg
import cn.zxm.sparkSIFT.SparkImage
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text, Writable}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-5-22.
  */
object ImageSePart_FixedPart {
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

  def create_Model(height:Int,width:Int): ImageSegment.ModelImg ={

    var model_Height = 0
    var model_Width = 0
    if (height >= 3000){
      model_Height = height/8
    }
    else if(height >=2000 && height < 3000){
      model_Height = height/6
    }
    else if (height >= 1000 && height < 2000 ){
      model_Height = height/4
    }
    else if(height >= 500 && height < 1000){
      model_Height = height/3
    }
    else{
      model_Height = height
    }

    if (width >= 3000){
      model_Width = width/6
    }
    else if(width >=2000 && width < 3000){
      model_Width = width/4
    }
    else if (width >= 1000 && width < 2000 ){
      model_Width = width/3
    }
    else if(width >= 500 && width < 1000){
      model_Width = width/2
    }
    else{
      model_Width = width
    }

    val modelImg:ImageSegment.ModelImg = new ModelImg(model_Height,model_Width) //创建模板
    modelImg
  }

  def main(args: Array[String]) {

    val dataset = args(0)
    val task_size = args(1)
    val part_size = args(2)

    val conf = new SparkConf()
    conf.setAppName("ImageSePart_FixedPart" + "_" + dataset + "_" + task_size + "_" + part_size)
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    val initImgs_500k_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset + "/*" //数据集路径


    val prefix_path_hdfs = "hdfs://simon-Vostro-3905:9000/user/root/imgdataset/" //用于提取特征的key

    val path = "/user/root/img_sq/" + dataset + "/"
    val tmpImageSEQ_path: String = hdfs_htname + path//序列化数据集在hdfs保存路径

    rm_hdfs(hdfs_htname,path)//删除原先保存的序列化文件

    //获取图片的子图集合
      val keyImgPartsRdd = sc.binaryFiles(initImgs_500k_path_hdfs,task_size.toInt).map(f => {
      val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img_0 = ImageIO.read(sbs)
      val gray_data = SparkImage.GetGrayDate(img_0)//获取图片的BufferImage

      val model:ImageSegment.ModelImg = new ModelImg(part_size.toInt,part_size.toInt)

      val imgParts = ImageSegment.DiveImgByModel(gray_data,img_0.getHeight(),img_0.getWidth(),model) //获取图片的分割子图集合

      val keyImgParts = new ArrayBuffer[(String,Int,Int,Array[Byte])]()
      for (i<- 0 to imgParts.size()-1){
        val img = imgParts.get(i)
        keyImgParts.append((fname+"#"+i,img.row.get(),img.col.get(),img.sePixels.getBytes))
      }

      keyImgParts.toArray  //返回图片分割后的子图集合{keyname,row,col,pixel} keyname的格式为：文件名_子图序号 pixel为子图灰度像素值
    })

    //将所有图片的子图集合序列化保存到hdfs
    keyImgPartsRdd.flatMap(x => {
      x
    }).map(y => {
      val img = new SequenceImage(y._2,y._3,y._4)
      (new Text(y._1),new BytesWritable(serialize(img)))
    }).saveAsHadoopFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]]) //函数里面保存每个图片的一个部分

    /*sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],1).map(f => {

      val img = new SequenceImage()
      deseriable(img,f._2.getBytes)

        val spfimg = new SpFImage(img.sePixels.getBytes,img.col.get(),img.row.get())
        spfimg
      }).collect().foreach(x => {
        SpDisplayUtilities.display(x)
    })*/



    sc.stop()
  }
}
