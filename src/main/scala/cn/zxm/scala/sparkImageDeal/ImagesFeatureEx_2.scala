package cn.zxm.scala.sparkImageDeal

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File, ByteArrayInputStream, InputStream}
import java.net.URI
import javax.imageio.ImageIO

import cn.zxm.sparkSIFT._
import org.apache.commons.math3.analysis.function.Min
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.poi.hssf.record.formula.functions.T
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.image.ImageUtilities
import org.openimaj.image.feature.local.engine.DoGSIFTEngine
import org.openimaj.io.IOUtils

/**
  * Created by root on 17-2-21.
  */
object ImagesFeatureEx_2 {

  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImageFeatureEx_2")
    conf.set("spark.worker.memory","8g")
    conf.set("spark.executor.memory","8g")
    conf.set("spark.driver.memory","16g")
    conf.set("spark.driver.maxResultSize","16g")
    conf.set("spark.network.timeout","10000000")
    val sc = new SparkContext(conf)

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"

    val imgsqdir = "/user/root/img_sq/"
    val kpslibdir = "/user/root/featureSq/"

    val dataset_0 = "dataset_500k" //数据集大小
    val dataset_1 = "dataset_70m" //数据集大小
    val dataset_2 = "dataset_2g"
    val dataset_3 = "dataset_200m"
    val dataset_test = "dataset_test"

    val imageSEQ_path = hdfs_htname + imgsqdir + dataset_test + "/"
    val kpslib_path = hdfs_htname + kpslibdir + dataset_test + "/" //特征库目录

    rm_hdfs(hdfs_htname,kpslibdir + dataset_test)

    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],1000).map({case (fname,fcontext) => {

      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)

      val radius: Int = 5

      val bimg: BufferedImage = ImageIO.read(datainput)

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

      val re_r: Int = img_pry(0).GetRows * 2
      val re_c: Int = img_pry(0).GetCols * 2

      //img_pry(0).data = BilineInterpolationScale.imgScale(img_pry(0).data, img_pry(0).GetCols, img_pry(0).GetRows, re_c, re_r)

      //val sigma: Array[java.lang.Double] = BuildGaussPry.CreateSigma(intvls)

      //SparkImage.CreateInitImg(img_pry(0), sigma(0), radius)

      val baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, img_pry(0))
      (fname,new BytesWritable(baos.toByteArray))

    }}).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])

    sc.stop()
  }
}
