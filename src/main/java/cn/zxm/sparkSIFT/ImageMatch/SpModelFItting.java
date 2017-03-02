package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpIndependentPair;
import org.openimaj.math.model.Model;
import org.openimaj.util.pair.IndependentPair;

import java.util.List;

/**
 * Created by root on 17-3-2.
 */
public interface SpModelFItting <I, D, M extends Model<I, D>> {
    /**
     * Attempt to fit the given data to the model.
     *
     * @param data
     *            Data to be fitted
     * @return true on success, false otherwise
     */
    boolean fitData(List<? extends SpIndependentPair<I, D>> data);

    /**
     * @return The minimum number of observations required to estimate the
     *         model.
     */
    public int numItemsToEstimate();

    /**
     * @return the trained model object
     */
    M getModel();
}
