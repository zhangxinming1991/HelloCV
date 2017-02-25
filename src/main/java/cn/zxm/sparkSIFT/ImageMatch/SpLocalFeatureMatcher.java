package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import org.openimaj.util.pair.Pair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public interface SpLocalFeatureMatcher <T extends SpLocalFeature<?, ?>>{

    /**
     * Set the features that represent the database to match queries against
     *
     * @param modelkeys
     *            database of features
     */
    public void setModelFeatures(List<T> modelkeys);

    /**
     * Attempt to find matches between the model features from the database, and
     * given query features.
     *
     * @param queryfeatures
     *            features from the query
     * @return whether matches were found
     */
    public boolean findMatches(List<T> queryfeatures);

    /**
     * Get the matches detected by the underlying algorithm
     *
     * @return the matches
     */
    public List<Pair<T>> getMatches();

}
