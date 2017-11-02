package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream, InputStream}

import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities

object ImageLegalTest {

  def main(args: Array[String]): Unit = {

    try {
      val path = "/home/hadoop0/Pictures/exception/car2.jpg"
      val input:InputStream = new FileInputStream(new File(path))

      val img = SpImageUtilities.readF(input)
      if (img == null)
        println("error")
      else
        println(img.getCols + ":" + img.getRows)
    }catch {case e:Exception=>{
      e.printStackTrace()
      //println("hello,world")
    }}
  }
}
