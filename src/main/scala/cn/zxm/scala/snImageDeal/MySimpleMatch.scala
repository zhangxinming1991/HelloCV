package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream}
import cn.zxm.sparkSIFT.ImageBasic.{SpFImage, SpImage, SpImageUtilities}
import cn.zxm.sparkSIFT.ImageMatch._
import cn.zxm.sparkSIFT.SIFT.ImageSegment.ModelImg
import cn.zxm.sparkSIFT.SIFT.{ImageSegment, SpDoGSIFTEngine}
import cn.zxm.sparkSIFT.imageKeyPoint.{SpLocalFeatureList, SpKeypoint}
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.{MBFImage, DisplayUtilities, ImageUtilities}
import org.openimaj.math.model.fit.RANSAC

/**
  * Created by root on 17-2-23.
  */
object MySimpleMatch {

  def main(args: Array[String]) {
    val  query= SpImageUtilities.readF(new FileInputStream(new File("dataset_500k/car2.jpg")))
    val target  = SpImageUtilities.readF(new FileInputStream(new File("dataset_500k/car1.jpg")))

    //val querymbf = ImageUtilities.readMBF(new FileInputStream(new File("dataset_500k/car2.jpg")))
    //val tgmbf = ImageUtilities.readMBF(new FileInputStream(new File("dataset_500k/car1.jpg")))

    val engine = new SpDoGSIFTEngine()
    val modelFItter = new SpRobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher = new SpConsistentLocalFeatureMatcher2d(new SpFastBasicKeypointMatcher[SpKeypoint](8), modelFItter)

    System.out.println(target.getRows + ":" + target.getCols)
    var match_size = 0;
    var querykps: SpLocalFeatureList[SpKeypoint] = null
    var tgkps: SpLocalFeatureList[SpKeypoint] = null

    val mdrow = target.getRows/4;
    val mdcol = target.getCols/3;

    val modelImg:ImageSegment.ModelImg = new ModelImg(mdrow,mdcol)

    val imgParts = ImageSegment.DiveImgByModel(target,modelImg)

    for (i<- 0 to imgParts.size()-1){
      SpDisplayUtilities.display( imgParts.get(i))

      tgkps = engine.findFeatures(imgParts.get(i))
      querykps = engine.findFeatures(query)

      matcher.setModelFeatures(tgkps)
      matcher.findMatches(querykps)

      match_size = match_size + matcher.getMatches.size()
      System.out.println(i + ":" + matcher.getMatches.size())
    }

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

  }

}
