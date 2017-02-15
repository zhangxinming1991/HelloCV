package cn.zxm.scala.sparkImageDeal

import java.io._
import java.net.URI

import cn.zxm.sparkSIFT.HadoopSerializationUtil
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.hadoop.io.{SequenceFile, IntWritable, BytesWritable, Text}
import org.apache.hadoop.mapred.SequenceFileOutputFormat
import org.apache.spark.sql.SparkSession
import org.openimaj.image.feature.local.engine.DoGSIFTEngine

import org.openimaj.image.{ImageUtilities, MBFImage}
import org.openimaj.io.IOUtils

/**
  * Created by root on 17-2-13.
  */
object SequenceKeypoints {
  def rm_hdfs(hdfs_htname: String, pt_s: String): Unit ={
    val pt: Path = new Path(hdfs_htname + pt_s)
    val fs: FileSystem = FileSystem.get(new URI(hdfs_htname), new Configuration, "root")
    fs.delete(pt, true)
  }
  def main(args: Array[String]) {
    /*val query: MBFImage = ImageUtilities.readMBF(new File("car2.jpg"))
    val engine = new DoGSIFTEngine()
    val keypoints = engine.findFeatures(query.flatten())

    val int_ser = new IntWritable(4)
    val ser_result = HadoopSerializationUtil.serialize(int_ser)
    System.out.println(keypoints.size())*/

    val spark = SparkSession
      .builder
      .appName("SequenceKeypoints")
      .getOrCreate()

    val sc = spark.sparkContext

    val hdfs_htname = "hdfs://simon-Vostro-3905:9000"
    val tmpImageSEQ_path: String = "hdfs://simon-Vostro-3905:9000/user/root/img_sq/"
    val kpsSEQ_path = "hdfs://simon-Vostro-3905:9000/user/root/featureSq"
    val testout = "hdfs://simon-Vostro-3905:9000/user/root/testout/"

    rm_hdfs(hdfs_htname,"/user/root/testout/")

    /*提取图片集合的特征点*/
    val fn_rdd = sc.sequenceFile(tmpImageSEQ_path,classOf[Text],classOf[BytesWritable],2).map({case (fname,fcontext) => {
      val datainput:InputStream = new ByteArrayInputStream(fcontext.getBytes)
      val img = ImageUtilities.readF(datainput)
      val engine = new DoGSIFTEngine()
      val kps = engine.findFeatures(img)

      var baos: ByteArrayOutputStream =new ByteArrayOutputStream()
      IOUtils.writeBinary(baos, kps)

      (new Text(fname.toString),new BytesWritable(baos.toByteArray))
    }}).saveAsHadoopFile(testout,classOf[Text],classOf[BytesWritable],classOf[SequenceFileOutputFormat[Text,BytesWritable]])
    /*提取图片集合的特征点*/

    //读取特征点集合序列文件
    val kps = HadoopSerializationUtil.getKPLFromSequence(new Text("car2.jpg"),testout)

    val kpsIterator = kps.iterator()
    while(kpsIterator.hasNext){
      val kp = kpsIterator.next()
      System.out.println(kp)
    }

    sc.stop()
  }

}
