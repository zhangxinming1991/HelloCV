package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.URI

import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SequenceImage, SpImageUtilities}
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{Writable, BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.storage.StorageLevel
import org.apache.spark.{SparkContext, SparkConf}
import org.openimaj.io.IOUtils

/**
  * Created by root on 17-2-28.
  */
object ImagesFeatureExByPart{

  def main(args: Array[String]) {
    val conf = new SparkConf()
    conf.setAppName("ImagesFeatureExByPart")
    conf.set("spark.worker.memory","12g")
    conf.set("spark.executor.memory","12g")
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

    val imageSEQ_path = hdfs_htname + imgsqdir + dataset_test + "/*"
    val kpslib_path = hdfs_htname + kpslibdir + dataset_test + "/" //特征库目录

    /*提取图片集合的特征点,建立特征库*/
    rm_hdfs(hdfs_htname,kpslibdir + dataset_test)
    val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],500).map({case (fname,fcontext) => {

      val sqimg = new SequenceImage()
      deseriable(sqimg,fcontext.getBytes)
      val sfimg = new SpFImage(sqimg.sePixels.getBytes,sqimg.col.get(),sqimg.row.get())

      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(sfimg)

      val fnkey = fname.toString.split("#")(0)
      (fnkey,kps) //形成新的(key,value)  key:文件名，没有加子图序号   value:特征点集合
    }}).reduceByKey((x,y) => {//将一张图片的所有子图的特征点集合合并
      x.addAll(y)
      x
    }
    ).map(f => {
      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, f._2)

      (new Text(f._1),new BytesWritable(baos.toByteArray))
    }).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点,建立特征库*/

  /*  val fn_rdd = sc.sequenceFile(imageSEQ_path,classOf[Text],classOf[BytesWritable],1).map({case (fname,fcontext) => {

      val sqimg = new SequenceImage()
      deseriable(sqimg,fcontext.getBytes)
      val sfimg = new SpFImage(sqimg.sePixels.getBytes,sqimg.col.get(),sqimg.row.get())

      val engine = new SpDoGSIFTEngine()
      val kps = engine.findFeatures(sfimg)

      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos,kps)

      (fname,new BytesWritable(baos.toByteArray))
    }}).persist(StorageLevel.MEMORY_AND_DISK).saveAsHadoopFile(kpslib_path,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])*/
    sc.stop()
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


}
