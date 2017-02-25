package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.imageKeyPoint.SpKeypoint;
import org.openimaj.knn.approximate.ByteNearestNeighboursKDTree;
import org.openimaj.util.pair.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpFastBasicKeypointMatcher <T extends SpKeypoint> extends SpBasicMatcher<T> {
    protected ByteNearestNeighboursKDTree modelKeypointsKNN;
    /**
     * Construct with a threshold of 8, corresponding to the 0.8 in Lowe's IJCV
     * paper
     */
    public SpFastBasicKeypointMatcher()
    {
        super(8);
    }

    /**
     *
     * @param threshold
     *            threshold for determining matching keypoints
     */
    public SpFastBasicKeypointMatcher(int threshold)
    {
        super(threshold);
    }

    /**
     * Given a pair of images and their keypoints, pick the first keypoint from
     * one image and find its closest match in the second set of keypoints. Then
     * write the result to a file.
     */
    @Override
    public boolean findMatches(List<T> keys1)
    {
        matches = new ArrayList<Pair<T>>();

        final byte[][] data = new byte[keys1.size()][];
        for (int i = 0; i < keys1.size(); i++)
            data[i] = keys1.get(i).ivec;

        final int[][] argmins = new int[keys1.size()][2];
        final float[][] mins = new float[keys1.size()][2];
        modelKeypointsKNN.searchKNN(data, 2, argmins, mins);

        for (int i = 0; i < keys1.size(); i++) {
            final float distsq1 = mins[i][0];
            final float distsq2 = mins[i][1];

            if (10 * 10 * distsq1 < thresh * thresh * distsq2) {
                matches.add(new Pair<T>(keys1.get(i), modelKeypoints.get(argmins[i][0])));
            }
        }

        return true;
    }

    @Override
    public void setModelFeatures(List<T> modelkeys) {
        modelKeypoints = modelkeys;

        final byte[][] data = new byte[modelkeys.size()][];
        for (int i = 0; i < modelkeys.size(); i++)
            data[i] = modelkeys.get(i).ivec;

        modelKeypointsKNN = new ByteNearestNeighboursKDTree(data, 1, 100);
    }

}
