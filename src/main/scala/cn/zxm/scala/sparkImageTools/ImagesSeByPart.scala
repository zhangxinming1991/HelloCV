package cn.zxm.scala.sparkImageTools

import java.awt.image.BufferedImage
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
  * ImagesSeByPart 图片文件分割子图再序列化
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
    val conf = new SparkConf()
    conf.setAppName("ImagesSeByPart")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.driver.memory","10g")
    conf.set("spark.driver.maxResultSize","10g")
    val sc = new SparkContext(conf)

    val dataset = args(0)

    val initImgs_path = "/home/simon/Public/spark-SIFT/imgdataset/"
    val prefix_path = "file:/home/simon/Public/spark-SIFT/imgdataset/"

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"   //主机名

    val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"
    //val inttImgs_path_local =  + "/05"  //本地数据集


    val initImgs_500k_path_hdfs = hdfs_htname + "/user/root/imgdataset/" + dataset + "/*" //数据集路径


    val prefix_path_hdfs = "hdfs://simon-Vostro-3905:9000/user/root/imgdataset/" //用于提取特征的key

    val path = "/user/root/img_sq/" + dataset + "/"
    val tmpImageSEQ_path: String = hdfs_htname + path//序列化数据集在hdfs保存路径

    rm_hdfs(hdfs_htname,path)//删除原先保存的序列化文件

    //获取图片的子图集合
    val keyImgPartsRdd = sc.binaryFiles(initImgs_500k_path_hdfs,500).map(f => {
      val fname = new Text(f._1.substring(prefix_path_hdfs.length,f._1.length))//获取features key

      val bytes = f._2.toArray()
      val sbs = new ByteArrayInputStream(bytes)

      val img_0 = ImageIO.read(sbs)
      val gray_data = SparkImage.GetGrayDate(img_0)//获取图片的BufferImage

      val modelImg:ImageSegment.ModelImg = create_Model(img_0.getHeight,img_0.getWidth) //创建模板

      /*val model_10_10:ImageSegment.ModelImg = new ModelImg(10,10)
      val model_50_50:ImageSegment.ModelImg = new ModelImg(50,50)
      val model_100_100:ImageSegment.ModelImg = new ModelImg(100,100)
      val model_200_200:ImageSegment.ModelImg = new ModelImg(200,200)
      val model_300_300:ImageSegment.ModelImg = new ModelImg(300,300)
      val model_400_400:ImageSegment.ModelImg = new ModelImg(400,400)
      val model_500_500:ImageSegment.ModelImg = new ModelImg(500,500)
      val model_600_600:ImageSegment.ModelImg = new ModelImg(600,600)
      val model_700_700:ImageSegment.ModelImg = new ModelImg(700,700)
      val model_800_800:ImageSegment.ModelImg = new ModelImg(800,800)
      val model_900_900:ImageSegment.ModelImg = new ModelImg(900,900)
      val model_1000_1000:ImageSegment.ModelImg = new ModelImg(1000,1000)*/

      val imgParts = ImageSegment.DiveImgByModel(gray_data,img_0.getHeight(),img_0.getWidth(),modelImg) //获取图片的分割子图集合

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
