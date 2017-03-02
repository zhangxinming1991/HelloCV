package cn.zxm.scala.snImageDeal

import java.io._
import javax.imageio.ImageIO
import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SpImage, SpImageUtilities}
import cn.zxm.sparkSIFT.ImageMatch._
import cn.zxm.sparkSIFT.SIFT.ImageSegment.ModelImg
import cn.zxm.sparkSIFT.SIFT.{ImageSegment, SpDoGSIFTEngine}
import cn.zxm.sparkSIFT.SparkImage
import cn.zxm.sparkSIFT.imageKeyPoint._
import org.apache.axis.utils.ByteArrayOutputStream
import org.apache.hadoop.io.{Writable, ArrayWritable}
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.{MBFImage, DisplayUtilities, ImageUtilities}
import org.openimaj.math.model.fit.RANSAC

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-23.
  */
object MySimpleMatch {

  def getBytes(filePath:String): Array[Byte] ={
    val file = new File(filePath)
    val fis = new FileInputStream(file)
    val bos = new ByteArrayOutputStream()
    val b = new Array[Byte](10*1024*1024)

    var n = 0;
    //breakable
    while (n != -1){
      n = fis.read(b)
      if (n == -1)

      bos.write(b,0,n)
    }

    fis.close()
    bos.close()

    bos.toByteArray
  }

  def getFile(bfile:Array[Byte],filePath:String,fileName:String): Unit ={

    val dir = new File(filePath)
    if (!dir.exists() && dir.isDirectory){
      dir.mkdir()
    }

    val file = new File(filePath + "/" + fileName)
    val fos = new FileOutputStream(file)
    val bos = new BufferedOutputStream(fos)
    bos.write(bfile)

    bos.close()
    fos.close()
  }

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
    //val modelImg:ImageSegment.ModelImg = new ModelImg(1000,1000) //创建模板
    modelImg
  }

  def main(args: Array[String]) {
    val   query = SpImageUtilities.readF(new FileInputStream(new File("/home/simon/Public/spark-SIFT/query/205600.jpg")))
    val  target = SpImageUtilities.readF(new FileInputStream(new File("dataset_200m/01/205600.jpg")))

    val engine = new SpDoGSIFTEngine()
    val modelFItter = new SpRobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher = new SpConsistentLocalFeatureMatcher2d(new SpFastBasicKeypointMatcher[SpKeypoint](8), modelFItter)

    System.out.println(target.getRows + ":" + target.getCols)
    var match_size = 0;
    var querykps: SpLocalFeatureList[SpKeypoint] = null
    var tgkps: SpLocalFeatureList[SpKeypoint] = null

    val modelImg:ImageSegment.ModelImg = create_Model(target.getRows,target.getCols)

    val imgParts = ImageSegment.DiveImgByModel(target,modelImg)

    querykps = engine.findFeatures(query)
    matcher.setModelFeatures(querykps)

    val kpsArray = new ArrayWritable(classOf[SequenceKeypointList])//store the kpslist of all the pic
    val sqkpslist = new ArrayBuffer[Writable]()  //store the writable kps of a pic

    for (i<- 0 to imgParts.size()-1){

      SpDisplayUtilities.display( imgParts.get(i))
      tgkps = engine.findFeatures(imgParts.get(i))  //get the kps of pic
      matcher.findMatches(tgkps)
      match_size = match_size + matcher.getMatches.size()
      System.out.println(i + ":" + matcher.getMatches.size())
      val wkps = new ArrayBuffer[Writable]() //store the writable kps
      val it = tgkps.iterator()
      while(it.hasNext){// change kp in the kps to hadoop writable
        val kp = it.next()

        val ivec = kp.getFeatureVector.getVector
        val x = kp.getX
        val y = kp.getY
        val ori = kp.ori
        val scale = kp.getScale

        val sqkp = new SequenceKeyPoint(x,y,ori,scale,ivec); //hadoop writable kp
        wkps.append(sqkp)
      }

      val skps = new SequenceKeypointList(classOf[SequenceKeyPoint],wkps.toArray) // hadoop writable kps
      sqkpslist += skps
    }

    System.out.println("total match1:" + match_size)

    kpsArray.set(sqkpslist.toArray)//make the writable kps of all pic

    val wBytes = serialize(kpsArray)//序列化图片特征点集
    getFile(wBytes,"/home/simon/Public/spark-SIFT/","kps_serialize")

    val deswritables = new ArrayBuffer[Writable]()
    for (i<- 0 to imgParts.size()-1){
      deswritables += new SequenceKeypointList()
    }

    val wkpsPics = new ArrayWritable(classOf[SequenceKeypointList])

    val bis = new BufferedInputStream(new FileInputStream("kps_serialize"))
    val desBytes = Stream.continually(bis.read).takeWhile(-1 !=).map(_.toByte).toArray

    //val desBytes = getBytes("kps_serialize")

    deseriable(wkpsPics,desBytes)//反序列化

    val kpsPics = wkpsPics.get()

    match_size = 0
    for (i <- 0 to kpsPics.size-1){
      val kpspic = SequenceKeypointList.changeWriteToSq(kpsPics(i))
      matcher.findMatches(SequenceKeypointList.GetListKps(kpspic.kps.get()))
      match_size = match_size + matcher.getMatches.size()
      System.out.println(i + ":" + matcher.getMatches.size())
    }

    System.out.println("total match2:" + match_size)

    tgkps = engine.findFeatures(target)
    matcher.findMatches(tgkps)
    System.out.println("total match3:" + matcher.getMatches.size())
    //val basicMatches = SpMatchingUtilities.drawMatches(querymbf,tgmbf,matcher.getMatches,RGBColour.RED)
    //DisplayUtilities.display(basicMatches)
  }

}
