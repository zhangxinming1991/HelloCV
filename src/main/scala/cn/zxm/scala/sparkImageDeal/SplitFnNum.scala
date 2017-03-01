package cn.zxm.scala.sparkImageDeal

/**
  * Created by root on 17-2-28.
  */
object SplitFnNum {

  def main(args: Array[String]) {
    val string = "datatest_200m/05/tesst.jpg#11"

    string.split("#").foreach(x => System.out.println(x))
  }
}
