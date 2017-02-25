package cn.zxm.scala.snImageDeal

import java.io.{File, FileInputStream}
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities
import cn.zxm.sparkSIFT.SIFT.SpDoGSIFTEngine
import org.openimaj.feature.local.matcher.{MatchingUtilities, FastBasicKeypointMatcher, LocalFeatureMatcher}
import org.openimaj.feature.local.matcher.consistent.ConsistentLocalFeatureMatcher2d
import org.openimaj.image.{DisplayUtilities, ImageUtilities}
import org.openimaj.image.colour.RGBColour
import org.openimaj.image.feature.local.keypoints.Keypoint
import org.openimaj.math.geometry.transforms.estimation.RobustAffineTransformEstimator
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

    val modelFItter: RobustAffineTransformEstimator = new RobustAffineTransformEstimator(5.0, 1500, new RANSAC.PercentageInliersStoppingCondition(0.5))
    val matcher: LocalFeatureMatcher[Keypoint] = new ConsistentLocalFeatureMatcher2d[Keypoint](new FastBasicKeypointMatcher[Keypoint](8), modelFItter)

    matcher.setModelFeatures(querykps)
    matcher.findMatches(tgkps)

    val basicMatches = MatchingUtilities.drawMatches(querymbf,tgmbf,matcher.getMatches,RGBColour.RED)
    DisplayUtilities.display(basicMatches)

  }

}
