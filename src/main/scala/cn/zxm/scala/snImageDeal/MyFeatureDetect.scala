package cn.zxm.scala.snImageDeal

import java.awt.image.BufferedImage
import java.io.{File, ByteArrayInputStream, InputStream}
import javax.imageio.ImageIO

import cn.zxm.sparkSIFT.{BuildGaussPry, SparkImage, My_Mat}
import org.apache.commons.math3.analysis.function.Min

/**
  * Created by root on 17-2-21.
  */
object MyFeatureDetect {

  def main(args: Array[String]) {
    val file = new File("dataset_500k/car6.jpg")
    //val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)

    val radius: Int = 5

    val bimg: BufferedImage = ImageIO.read(file)

    val intvls: Integer = 3

    val nOctaves_d: Double = Math.log(new Min().value(bimg.getWidth, bimg.getHeight)) / Math.log(2.0) - 2
    val nOctaves: Integer = nOctaves_d.intValue

    val img_pry = new Array[My_Mat](nOctaves * (intvls + 3))
    val doggaussian = new Array[My_Mat](nOctaves * (intvls + 2))

    for (i <- 0 to img_pry.length - 1){
      img_pry(i) = new My_Mat(bimg.getType)
    }

    img_pry(0).CreateData(bimg.getWidth, bimg.getHeight, new Array[Integer](bimg.getWidth * bimg.getHeight))
    img_pry(0).CreateDData(bimg.getWidth, bimg.getHeight, new Array[java.lang.Double](bimg.getWidth * bimg.getHeight))

    SparkImage.ToGray(bimg,img_pry(0))

    val sigma: Array[java.lang.Double] = BuildGaussPry.CreateSigma(intvls)
   SparkImage.CreateInitImg(img_pry(0), sigma(0), radius)

   System.out.println("hello,world")


}

}
