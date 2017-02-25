package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.math.model.fit.LMedS;
import org.openimaj.math.model.fit.RANSAC;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpRobustAffineTransformEstimator implements RobustModelFitting<SpPoint2d, SpPoint2d, SpAffineTransformModel> {

    private RobustModelFitting<SpPoint2d, SpPoint2d, SpAffineTransformModel> robustFitter;

    /**
     * Construct using the {@link LMedS} algorithm with the given expected
     * outlier percentage
     *
     * @param outlierProportion
     *            expected proportion of outliers (between 0 and 1)
     */
    public SpRobustAffineTransformEstimator(double outlierProportion) {
        robustFitter = new LMedS<SpPoint2d, SpPoint2d, SpAffineTransformModel>(
                new SpAffineTransformModel(),
                new SpAlgebraicResidual2d<SpAffineTransformModel>(),
                outlierProportion, true, new SpBucketingSampler2d());
    }

    /**
     * Construct using the {@link RANSAC} algorithm with the given options.
     *
     * @param threshold
     *            the threshold on the {@link SpAlgebraicResidual2d} at which to
     *            consider a point as an inlier
     * @param nIterations
     *            the maximum number of iterations
     * @param stoppingCondition
     *            the {@link RANSAC.StoppingCondition} for RANSAC
     */
    public SpRobustAffineTransformEstimator(double threshold, int nIterations, RANSAC.StoppingCondition stoppingCondition)
    {
        robustFitter = new RANSAC<SpPoint2d, SpPoint2d, SpAffineTransformModel>(new SpAffineTransformModel(),
                new SpAlgebraicResidual2d<SpAffineTransformModel>(), threshold, nIterations, stoppingCondition, true,
                new SpBucketingSampler2d());
    }

    /**
     * Construct using the {@link LMedS} algorithm with the given expected
     * outlier percentage
     *
     * @param outlierProportion
     *            expected proportion of outliers (between 0 and 1)
     * @param modelCheck
     *            the predicate to test whether an estimated model is sane
     */
    public SpRobustAffineTransformEstimator(double outlierProportion, Predicate<SpAffineTransformModel> modelCheck) {
        robustFitter = new LMedS<SpPoint2d, SpPoint2d, SpAffineTransformModel>(
                new SpAffineTransformModel(modelCheck),
                new SpAlgebraicResidual2d<SpAffineTransformModel>(),
                outlierProportion, true, new SpBucketingSampler2d());
    }

    /**
     * Construct using the {@link RANSAC} algorithm with the given options.
     *
     * @param threshold
     *            the threshold on the {@link SpAlgebraicResidual2d} at which to
     *            consider a point as an inlier
     * @param nIterations
     *            the maximum number of iterations
     * @param stoppingCondition
     *            the {@link RANSAC.StoppingCondition} for RANSAC
     * @param modelCheck
     *            the predicate to test whether an estimated model is sane
     */
    public SpRobustAffineTransformEstimator(double threshold, int nIterations, RANSAC.StoppingCondition stoppingCondition,
                                          Predicate<SpAffineTransformModel> modelCheck)
    {
        robustFitter = new RANSAC<SpPoint2d, SpPoint2d, SpAffineTransformModel>(new SpAffineTransformModel(modelCheck),
                new SpAlgebraicResidual2d<SpAffineTransformModel>(), threshold, nIterations, stoppingCondition, true,
                new SpBucketingSampler2d());
    }

    @Override
    public boolean fitData(List<? extends IndependentPair<SpPoint2d, SpPoint2d>> data) {
        // Use a robust fitting technique to find the inliers and estimate a
        // model using DLT
        if (!robustFitter.fitData(data)) {
            // just go with full-on DLT estimate rather than a robust one
            robustFitter.getModel().estimate(data);

            return false;
        }

        return true;
    }

    @Override
    public int numItemsToEstimate() {
        return robustFitter.numItemsToEstimate();
    }

    @Override
    public SpAffineTransformModel getModel() {
        return robustFitter.getModel();
    }

    @Override
    public List<? extends IndependentPair<SpPoint2d, SpPoint2d>> getInliers() {
        return robustFitter.getInliers();
    }

    @Override
    public List<? extends IndependentPair<SpPoint2d, SpPoint2d>> getOutliers() {
        return robustFitter.getOutliers();
    }

}
