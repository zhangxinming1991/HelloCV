package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpLocalFeature;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.feature.local.matcher.consistent.ModelFittingLocalFeatureMatcher;
import org.openimaj.math.model.Model;
import org.openimaj.math.model.fit.RobustModelFitting;
import org.openimaj.util.pair.IndependentPair;
import org.openimaj.util.pair.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpConsistentLocalFeatureMatcher2d <T extends SpLocalFeature<?, ?> & SpPoint2d>
        implements
        SpModelFittingLocalFeatureMatcher<T> {

    protected SpLocalFeatureMatcher<T> innerMatcher;
    protected RobustModelFitting<SpPoint2d, SpPoint2d, ?> modelfit;
    protected List<Pair<T>> consistentMatches;

    /**
     * Default constructor
     *
     * @param innerMatcher
     *            the internal matcher for getting seed matches
     */
    public SpConsistentLocalFeatureMatcher2d(SpLocalFeatureMatcher<T> innerMatcher) {
        this.innerMatcher = innerMatcher;

        modelfit = null;
        consistentMatches = new ArrayList<Pair<T>>();
    }

    /**
     * Default constructor
     *
     * @param innerMatcher
     *            the internal matcher for getting seed matches
     * @param fit
     *            the points against which to test consistency
     */
    public SpConsistentLocalFeatureMatcher2d(SpLocalFeatureMatcher<T> innerMatcher,
                                           RobustModelFitting<SpPoint2d, SpPoint2d, ?> fit)
    {
        this(innerMatcher);

        modelfit = fit;
    }

    /**
     * @return a list of consistent matching keypoints according to the
     *         estimated model parameters.
     */
    @Override
    public List<Pair<T>> getMatches() {
        return consistentMatches;
    }

    /**
     * @return a list of all matches irrespective of whether they fit the model
     */
    public List<Pair<T>> getAllMatches() {
        return innerMatcher.getMatches();
    }

    @Override
    public Model<SpPoint2d, SpPoint2d> getModel() {
        return modelfit.getModel();
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean findMatches(List<T> keys1)
    {
        // if we're gonna re-use the object, we need to reset everything!
        consistentMatches = new ArrayList<Pair<T>>();

        // find the initial matches using the inner matcher
        innerMatcher.findMatches(keys1);
        final List<Pair<T>> matches = innerMatcher.getMatches();

        if (matches.size() < modelfit.numItemsToEstimate()) {
            consistentMatches.clear();
            consistentMatches.addAll(matches);
            return false;
        }

        final List<Pair<SpPoint2d>> li_p2d = new ArrayList<Pair<SpPoint2d>>();
        for (final Pair<T> m : matches) {
            li_p2d.add(new Pair<SpPoint2d>(m.firstObject(), m.secondObject()));
        }

        // fit the model
        final boolean didfit = modelfit.fitData(li_p2d);

        // get the inliers and build the list of consistent matches
        for (final IndependentPair<SpPoint2d, SpPoint2d> p : modelfit.getInliers()) {
            final Object op = p;
            consistentMatches.add((Pair<T>) op);
        }

        return didfit;
    }

    @Override
    public void setFittingModel(RobustModelFitting<SpPoint2d, SpPoint2d, ?> mf) {
        modelfit = mf;
    }

    @Override
    public void setModelFeatures(List<T> modelkeys) {
        innerMatcher.setModelFeatures(modelkeys);
    }

}
