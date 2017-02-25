package cn.zxm.sparkSIFT.ImageMatch;

import Jama.Matrix;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.EstimatableModel;
import org.openimaj.util.function.Predicate;
import org.openimaj.util.pair.IndependentPair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpAffineTransformModel implements EstimatableModel<SpPoint2d, SpPoint2d>, MatrixTransformProvider {

    protected Predicate<SpAffineTransformModel> modelCheck;
    protected Matrix transform;

    /**
     * Create an {@link SpAffineTransformModel}
     */
    public SpAffineTransformModel()
    {
        transform = new Matrix(3, 3);

        transform.set(2, 0, 0);
        transform.set(2, 1, 0);
        transform.set(2, 2, 1);
    }

    /**
     * Create an {@link SpAffineTransformModel}. The given {@link Predicate} is
     * used by the {@link #estimate(List)} method to test whether the estimated
     * affine transform is sensible.
     *
     * @param mc
     *            the test function for sensible affine transforms
     */
    public SpAffineTransformModel(Predicate<SpAffineTransformModel> mc)
    {
        this.modelCheck = mc;
        transform = new Matrix(3, 3);

        transform.set(2, 0, 0);
        transform.set(2, 1, 0);
        transform.set(2, 2, 1);
    }

    @Override
    public SpAffineTransformModel clone() {
        final SpAffineTransformModel atm = new SpAffineTransformModel();
        atm.modelCheck = modelCheck;
        atm.transform = transform.copy();
        return atm;
    }

    @Override
    public Matrix getTransform() {
        return transform;
    }

    /*
     * SVD least-squares estimation of affine transform matrix for a set of 2d
     * points
     */
    @Override
    public boolean estimate(List<? extends IndependentPair<SpPoint2d, SpPoint2d>> data) {
        if (data.size() < numItemsToEstimate())
            return false;

        this.transform = SpTransformUtilities.affineMatrix(data);

        if (modelCheck == null)
            return true;

        return modelCheck.test(this);
    }

    @Override
    public SpPoint2d predict(SpPoint2d p) {
        return p.transform(transform);
    }

    @Override
    public int numItemsToEstimate() {
        return 3;
    }

    @Override
    public String toString() {
        String str = "";
        final double[][] mat = transform.getArray();
        for (final double[] r : mat) {
            for (final double v : r) {
                str += " " + v;
            }
            str += "\n";
        }
        return str;
    }
}
