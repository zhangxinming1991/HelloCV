package cn.zxm.sparkSIFT.ImageMatch;

import Jama.Matrix;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.math.geometry.transforms.MatrixTransformProvider;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.residuals.ResidualCalculator;
import org.openimaj.util.pair.IndependentPair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpAlgebraicResidual2d <M extends Model<SpPoint2d, SpPoint2d> & MatrixTransformProvider>
        implements
        ResidualCalculator<SpPoint2d, SpPoint2d, M> {

    private Matrix transform;

    @Override
    public void setModel(M model) {
        this.transform = model.getTransform();

        if (transform.getRowDimension() != 3 || transform.getColumnDimension() != 3)
            throw new IllegalArgumentException("Transform matrix must be 3x3");
    }

    @Override
    public double computeResidual(IndependentPair<SpPoint2d, SpPoint2d> data) {
        final SpPoint2d p1 = data.getFirstObject();
        final SpPoint2d p2 = data.getSecondObject();

        final float x = p1.getX();
        final float y = p1.getY();
        final float X = p2.getX();
        final float Y = p2.getY();

        final double h11 = transform.get(0, 0);
        final double h12 = transform.get(0, 1);
        final double h13 = transform.get(0, 2);
        final double h21 = transform.get(1, 0);
        final double h22 = transform.get(1, 1);
        final double h23 = transform.get(1, 2);
        final double h31 = transform.get(2, 0);
        final double h32 = transform.get(2, 1);
        final double h33 = transform.get(2, 2);

        final double s1 = x * h11 + y * h12 + h13 - x * X * h31 - y * X * h32 - X * h33;
        final double s2 = x * h21 + y * h22 + h23 - x * Y * h31 - y * Y * h32 - Y * h33;

        return s1 * s1 + s2 * s2;
    }

    @Override
    public void computeResiduals(List<? extends IndependentPair<SpPoint2d, SpPoint2d>> data, double[] residuals) {
        for (int i = 0; i < data.size(); i++) {
            residuals[i] = computeResidual(data.get(i));
        }
    }

}
