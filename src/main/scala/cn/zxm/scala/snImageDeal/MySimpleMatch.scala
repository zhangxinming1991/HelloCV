package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream}

import cn.zxm.sparkSIFT.{DisplayUtilities, ImageUtilities}

/**
  * Created by root on 17-2-23.
  */
object MySimpleMatch {

  def main(args: Array[String]) {
    val query = ImageUtilities.readF(new FileInputStream(new File("dataset_500k/car2.jpg")))

    DisplayUtilities.display(query)
  }

}
