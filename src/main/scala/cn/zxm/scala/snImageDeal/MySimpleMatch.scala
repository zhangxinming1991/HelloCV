package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream}
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.ImageMatch._
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import cn.zxm.sparkSIFT.imageKeyPoint.SpKeypoint
import org.openimaj.feature.local.matcher.MatchingUtilities
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.{DisplayUtilities, ImageUtilities}
import org.openimaj.math.model.fit.RANSAC

/**
  * Created by root on 17-2-23.
  */
object MySimpleMatch {

  def main(args: Array[String]) {
    val query = SpImageUtilities.readF(new FileInputStream(new File("dataset_500k/car2.jpg")))
    val target = SpImageUtilities.readF(new FileInputStream(new File("dataset_500k/car1.jpg")))

    val querymbf = ImageUtilities.readMBF(new FileInputStream(new File("dataset_500k/car2.jpg")))
    val tgmbf = ImageUtilities.readMBF(new FileInputStream(new File("dataset_500k/car1.jpg")))

    //val partImg = ImageSegment.GetOnePart(query,0,0,93,151)

    //SpDisplayUtilities.display(query)

    val engine = new SpDoGSIFTEngine()
    val querykps = engine.findFeatures(query)
    val tgkps = engine.findFeatures(target)

    System.out.println(querykps.size() + "：" + tgkps.size())

    val modelFItter = new SpRobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher = new SpConsistentLocalFeatureMatcher2d(new SpFastBasicKeypointMatcher[SpKeypoint](8), modelFItter)

    matcher.setModelFeatures(querykps)
    matcher.findMatches(tgkps)

    //val basicMatches = SpMatchingUtilities.drawMatches(query,target,matcher.getMatches,RGBColour.RED)
    val basicMatches = SpMatchingUtilities.drawMatches(querymbf,tgmbf,matcher.getMatches,RGBColour.RED)
    DisplayUtilities.display(basicMatches)

  }

}
