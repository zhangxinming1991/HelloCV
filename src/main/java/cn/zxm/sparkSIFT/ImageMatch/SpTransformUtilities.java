package cn.zxm.sparkSIFT.ImageMatch;

import Jama.Matrix;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.math.matrix.MatrixUtils;
import org.openimaj.util.pair.IndependentPair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpTransformUtilities {

    /**
     * Construct an affine transform using a least-squares fit of the provided
     * point pairs. There must be at least 3 point pairs for this to work.
     *
     * @param data
     *            Data to calculate affine matrix from.
     * @return an affine transform matrix.
     */
    public static Matrix affineMatrix(List<? extends IndependentPair<SpPoint2d, SpPoint2d>> data) {
        final Matrix transform = new Matrix(3, 3);

        transform.set(2, 0, 0);
        transform.set(2, 1, 0);
        transform.set(2, 2, 1);

        // Solve Ax=0
        final Matrix A = new Matrix(data.size() * 2, 7);

        for (int i = 0, j = 0; i < data.size(); i++, j += 2) {
            final float x1 = data.get(i).firstObject().getX();
            final float y1 = data.get(i).firstObject().getY();
            final float x2 = data.get(i).secondObject().getX();
            final float y2 = data.get(i).secondObject().getY();

            A.set(j, 0, x1);
            A.set(j, 1, y1);
            A.set(j, 2, 1);
            A.set(j, 3, 0);
            A.set(j, 4, 0);
            A.set(j, 5, 0);
            A.set(j, 6, -x2);

            A.set(j + 1, 0, 0);
            A.set(j + 1, 1, 0);
            A.set(j + 1, 2, 0);
            A.set(j + 1, 3, x1);
            A.set(j + 1, 4, y1);
            A.set(j + 1, 5, 1);
            A.set(j + 1, 6, -y2);
        }

        final double[] W = MatrixUtils.solveHomogeneousSystem(A);

        // build matrix
        transform.set(0, 0, W[0] / W[6]);
        transform.set(0, 1, W[1] / W[6]);
        transform.set(0, 2, W[2] / W[6]);

        transform.set(1, 0, W[3] / W[6]);
        transform.set(1, 1, W[4] / W[6]);
        transform.set(1, 2, W[5] / W[6]);

        return transform;
    }

}
