package cn.zxm.scala.sparkImageTools

import java.awt.image.BufferedImage
import java.io.{DataInputStream, ByteArrayInputStream, IOException}
import javax.swing.{ImageIcon, JLabel, JFrame}

import cn.zxm.sparkSIFT.DisplayBasic.SpDisplayUtilities
import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SequenceImage}
import cn.zxm.sparkSIFT.SparkImage
import org.apache.hadoop.io.{Writable, BytesWritable, Text}
import org.apache.spark.{SparkContext, SparkConf}

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-28.
  */
object ImageDesqByPart {

  def showPic(showimg:BufferedImage): Unit ={
    val frame = new JFrame("hello")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane.add(new JLabel(new ImageIcon(showimg)))
    frame.pack
    frame.setVisible(true)
  }

  @throws(classOf[IOException])
  def deseriable(write: Writable, bytes: Array[Byte]) {
    val in: ByteArrayInputStream = new ByteArrayInputStream(bytes)
    val datain: DataInputStream = new DataInputStream(in)
    write.readFields(datain)
    datain.close
  }

  def main(args: Array[String]) {

    val conf = new SparkConf()
    conf.setAppName("ImageDesqByPart")
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

    val path = "/user/root/img_sq/" + dataset_3 + "/"
    val tmpImageSEQ_path: String = hdfs_htname + path

    sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],20).map(f => {

    val img = new SequenceImage()
    deseriable(img,f._2.getBytes)
    if (f._1.toString.indexOf("207300.jpg") != -1){
      val spfimg = new SparkImage(img.row.get(),img.col.get(),img.sePixels.getBytes)
      //spfimg.setRGB(0,0,spfimg.getWidth,spfimg.getHeight,,0,spfimg.getWidth)
      spfimg
    }
    else {
      null
    }
  }).collect().foreach(x => {
    if (x != null) {
      //SpDisplayUtilities.display(x)
      val pixels = new ArrayBuffer[Int]()
      for (i <- 0 to x.sePixels.size-1) {
        val pixel: Byte = x.sePixels(i)
        pixels += SparkImage.colorToRGB(pixel, pixel, pixel)
      }

     /* val img = new BufferedImage(x.col,x.row,1)
      img.setRGB(0,0,x.col,x.row,pixels.toArray,0,x.col)

      showPic(img)*/

      val img = new SpFImage(pixels.toArray,x.col,x.row)
      SpDisplayUtilities.display(img)
    }
  })
}
}
