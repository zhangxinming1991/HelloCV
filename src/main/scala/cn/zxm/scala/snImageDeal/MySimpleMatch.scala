package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream}
import javax.imageio.ImageIO
import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SpImage, SpImageUtilities}
import cn.zxm.sparkSIFT.ImageMatch._
import cn.zxm.sparkSIFT.SIFT.ImageSegment.ModelImg
import cn.zxm.sparkSIFT.SIFT.{ImageSegment, SpDoGSIFTEngine}
import cn.zxm.sparkSIFT.SparkImage
import cn.zxm.sparkSIFT.imageKeyPoint.{SpMemoryLocalFeatureList, SpLocalFeatureList, SpKeypoint}
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.{MBFImage, DisplayUtilities, ImageUtilities}
import org.openimaj.math.model.fit.RANSAC

import scala.collection.mutable.ArrayBuffer

/**
  * Created by root on 17-2-23.
  */
object MySimpleMatch {

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
    val tgkpslist = new SpMemoryLocalFeatureList[SpKeypoint]()
    for (i<- 0 to imgParts.size()-1){
      SpDisplayUtilities.display( imgParts.get(i))
      tgkps = engine.findFeatures(imgParts.get(i))

      matcher.findMatches(tgkps)
      System.out.println(i + ":" + matcher.getMatches.size())
      tgkpslist.addAll(i,tgkps)
    }

    System.out.println("list size:" + tgkpslist.size())

    matcher.findMatches(tgkpslist)

    match_size = match_size + matcher.getMatches.size()
    System.out.println(matcher.getMatches.size())

    /*val partImgs = Array.ofDim[SpFImage](2,2)
    for (i<- 0 to 1){
      for (j<- 0 to 1){
        partImgs(i)(j) = ImageSegment.GetOnePart(target,i*150,j*240,149,239)
        SpDisplayUtilities.display( partImgs(i)(j))

        tgkps = engine.findFeatures(partImgs(i)(j))
        querykps = engine.findFeatures(query)

        matcher.setModelFeatures(tgkps)
        matcher.findMatches(querykps)

        match_size = match_size + matcher.getMatches.size()
        System.out.println( "(" + i + "," + j +  ")" + ":" + matcher.getMatches.size())
      }
    }*/

    System.out.println("total match:" + match_size)

    querykps = engine.findFeatures(query)
    tgkps = engine.findFeatures(target)

    matcher.setModelFeatures(querykps)
    matcher.findMatches(tgkps)
    System.out.println("total match:" + matcher.getMatches.size())
    //val basicMatches = SpMatchingUtilities.drawMatches(querymbf,tgmbf,matcher.getMatches,RGBColour.RED)
    //DisplayUtilities.display(basicMatches)


  /*  val img_0 = ImageIO.read(new FileInputStream(new File("dataset_500k/car1.jpg")))
    val gray_datas = SparkImage.GetGrayDate(img_0)

    System.out.println(img_0.getHeight + ":" + img_0.getWidth)



    val modelImg:ImageSegment.ModelImg = create_Model(img_0.getHeight,img_0.getWidth) //创建模板
    System.out.println(modelImg.row + ":" + modelImg.col)
    val imgParts = ImageSegment.DiveImgByModel(gray_datas,img_0.getHeight(),img_0.getWidth(),modelImg) //获取图片的分割子图集合
    System.out.println(imgParts.get(0).row.get() + ":" + imgParts.get(0).col.get())

    //System.out.println(gray_data. + ":" + gray_data.getWidth)
    val row = gray_datas.length
    val col = gray_datas(0).length
    val rgb = new ArrayBuffer[Int]()
    val test = imgParts.get(0).sePixels.getBytes
    for (i <- 0 to row-1)
      for (j <- 0 to col-1) {
        //if (test(i*(col) + j) != gray_datas(i)(j)){
          //System.out.println(test(i*(col) + j))
        //}
        val gray_data = test(i*(col) + j)
        rgb += SparkImage.colorToRGB(gray_data, gray_data, gray_data)
      }


    val img = new SpFImage(imgParts.get(0).sePixels.getBytes,img_0.getWidth,img_0.getHeight)

    SpDisplayUtilities.display(img)

    val gray_datas_1 = SparkImage.ToGray(img_0)
    val rgb_1 = new ArrayBuffer[Int]()
    for (i <- 0 to row-1)
      for (j <- 0 to col-1)
        rgb_1 += gray_datas_1.getRGB(j,i)

    val img_1 = new SpFImage(rgb_1.toArray,img_0.getWidth,img_0.getHeight)


    for (i <- 0 to row-1)
      for (j <- 0 to col-1) {
        System.out.println(img.pixels(i)(j) + ":" + img_1.pixels(i)(j))
      }
    SpDisplayUtilities.display(img_1)*/
  }

}
