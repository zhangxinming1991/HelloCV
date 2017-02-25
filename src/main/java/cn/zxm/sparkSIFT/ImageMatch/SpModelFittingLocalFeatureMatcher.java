package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.RobustModelFitting;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public interface SpModelFittingLocalFeatureMatcher <T extends SpLocalFeature<?, ?> /*
																			 * &
																			 * Point2d
																			 */> extends SpLocalFeatureMatcher<T>{
    /**
     * Set the object which robustly attempts to fit matches to the model
     *
     * @param mf
     *            fitting model
     */
    public void setFittingModel(RobustModelFitting<SpPoint2d, SpPoint2d, ?> mf);

    /**
     * Attempt to find matches between the model features from the database, and
     * given query features and learn the parameters of the underlying model
     * that links the two sets of features.
     *
     * @param queryfeatures
     *            features from the query
     */
    @Override
    public boolean findMatches(List<T> queryfeatures);

    /**
     * Get the model that has been learned. Do this after finding matches!
     *
     * @return the model found between the model features and object features
     *         after a findMatches operation.
     */
    public Model<SpPoint2d, SpPoint2d> getModel();

}
