package cn.zxm.scala.sparkImageDeal

import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.{ImageIcon, JFrame, JLabel}

import cn.zxm.sparkSIFT.{My_ImageMat, SparkImage}
import org.apache.spark.sql.SparkSession

/**
  * Created by root on 17-2-7.
  */
object SequenceImages {

  def showPic(showimg:BufferedImage): Unit ={
    val frame = new JFrame("hello")
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.getContentPane.add(new JLabel(new ImageIcon(showimg)))
    frame.pack
    frame.setVisible(true)
  }

  def main(args: Array[String]) {
    val spark = SparkSession
      .builder
      .appName("SequenceImages")
      .getOrCreate()

    val sc = spark.sparkContext

    val hdfs_name = "hdfs://simon-Vostro-3905:9000"
    val pt_s = "/user/root/car_pic/"

    var startTime = System.currentTimeMillis()
    val imgs = sc.binaryFiles(hdfs_name+pt_s,10).map({case (x,y) => {//以二进制方式读取图片集合？如果采用objectfile方式或者是sequencefile方式是否会处理更快
      /*将所有图片灰度化处理*/
      val bimg = ImageIO.read(y.open())

      val gimg = SparkImage.ToGray(bimg)
      val w = gimg.getWidth()
      val h = gimg.getHeight()
      val x = new Array[Int](w*h)
      gimg.getRGB(0,0,w,h,x,0,w)
      val my_Mat = new My_ImageMat(gimg.getType,w,h)
      my_Mat.CreateData(w,h,x)
      my_Mat

    }})

    val colimgs = imgs.collect()
    var endTime = System.currentTimeMillis()
    System.out.println("***pa use time:" + (endTime-startTime))

    /*串行灰度化处理*/
    startTime = System.currentTimeMillis()
    colimgs.iterator.foreach(x => {
      val img = new BufferedImage(x.GetCols(),x.GetRows(),x.GetType())
      img.setRGB(0,0,x.GetCols(),x.GetRows(),x.data,0,x.GetCols())
      SparkImage.ToGray(img)
      //showPic(img)
    }
    )
    endTime = System.currentTimeMillis()
    System.out.println("***se use time:" + (endTime-startTime))
    sc.stop()
  }

}
