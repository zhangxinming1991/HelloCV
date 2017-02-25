package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.util.pair.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpBasicMatcher <T extends SpLocalFeature<?, ?>> implements SpLocalFeatureMatcher<T>{

    protected List<T> modelKeypoints;
    protected List<Pair<T>> matches;
    protected int thresh = 8;

    /**
     * Initialise the matcher setting the threshold which the difference between
     * the scores of the top two best matches must differ in order to count the
     * first as a good match.
     *
     * @param threshold
     */
    public SpBasicMatcher(int threshold)
    {
        matches = new ArrayList<Pair<T>>();
        thresh = threshold;
    }

    /**
     * @return List of pairs of matching keypoints
     */
    @Override
    public List<Pair<T>> getMatches() {
        return matches;
    }

    @Override
    public boolean findMatches(List<T> keys1)
    {
        matches = new ArrayList<Pair<T>>();

		/*
		 * Match the keys in list keys1 to their best matches in keys2.
		 */
        for (final T k : keys1) {
            final T match = checkForMatch(k, modelKeypoints);

            if (match != null) {
                matches.add(new Pair<T>(k, match));
            }
        }

        return true;
    }

    /**
     * This searches through the keypoints in klist for the two closest matches
     * to key. If the closest is less than <code>threshold</code> times distance
     * to second closest, then return the closest match. Otherwise, return NULL.
     */
    protected T checkForMatch(T query, List<T> features)
    {
        double distsq1 = Double.MAX_VALUE, distsq2 = Double.MAX_VALUE;
        T minkey = null;

        // find two closest matches
        for (final T target : features) {
            final double dsq = target.getFeatureVector().asDoubleFV()
                    .compare(query.getFeatureVector().asDoubleFV(), DoubleFVComparison.EUCLIDEAN);

            if (dsq < distsq1) {
                distsq2 = distsq1;
                distsq1 = dsq;
                minkey = target;
            } else if (dsq < distsq2) {
                distsq2 = dsq;
            }
        }

        // check the distance against the threshold
        if (10 * 10 * distsq1 < thresh * thresh * distsq2) {
            return minkey;
        }
        else
            return null;
    }

    @Override
    public void setModelFeatures(List<T> modelkeys) {
        modelKeypoints = modelkeys;
    }

    /**
     * Set the matching threshold
     *
     * @param thresh
     *            the threshold
     */
    public void setThreshold(int thresh) {
        this.thresh = thresh;
    }

}
