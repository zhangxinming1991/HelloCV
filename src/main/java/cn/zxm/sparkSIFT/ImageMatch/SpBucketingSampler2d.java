package cn.zxm.sparkSIFT.ImageMatch;


import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.util.CollectionSampler;
import org.openimaj.util.pair.IndependentPair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by root on 17-2-25.
 */
public class SpBucketingSampler2d implements CollectionSampler<IndependentPair<SpPoint2d, SpPoint2d>> {

    private class Bucket {
        private List<IndependentPair<SpPoint2d, SpPoint2d>> buckets = new ArrayList<IndependentPair<SpPoint2d, SpPoint2d>>();
        private double interval;
    }

    /**
     * Default number of buckets per dimension
     */
    public final static int DEFAULT_N_BUCKETS_PER_DIM = 8;

    /**
     * Maximum allowed number of trials in picking a bucket that has not been
     * previously picked.
     */
    public static int NUM_TRIALS = 100;

    private Random rng;
    private Bucket[] bucketList;

    private int nBucketsX;

    private int nBucketsY;

    /**
     * Construct the sampler with the default number of buckets in the x and y
     * dimensions (8).
     */
    public SpBucketingSampler2d() {
        this(DEFAULT_N_BUCKETS_PER_DIM, DEFAULT_N_BUCKETS_PER_DIM);
    }

    /**
     * Construct the sampler with the given number of buckets in each dimension.
     *
     * @param nBucketsX
     *            number of buckets in the x dimension
     * @param nBucketsY
     *            number of buckets in the y dimension
     */
    public SpBucketingSampler2d(int nBucketsX, int nBucketsY) {
        this.nBucketsX = nBucketsX;
        this.nBucketsY = nBucketsY;
        this.rng = new Random();
    }

    @Override
    public void setCollection(Collection<? extends IndependentPair<SpPoint2d, SpPoint2d>> collection) {
        // find max, max
        float minx = Float.MAX_VALUE;
        float maxx = -Float.MAX_VALUE;
        float miny = Float.MAX_VALUE;
        float maxy = -Float.MAX_VALUE;

        for (final IndependentPair<SpPoint2d, SpPoint2d> pair : collection) {
            final SpPoint2d first = pair.firstObject();
            final float x = first.getX();
            final float y = first.getY();

            if (x < minx)
                minx = x;
            if (x > maxx)
                maxx = x;
            if (y < miny)
                miny = y;
            if (y > maxy)
                maxy = y;
        }

        minx -= 0.001;
        maxx += 0.001;
        miny -= 0.001;
        maxy += 0.001;

        // reset buckets
        final Bucket[][] buckets = new Bucket[nBucketsY][nBucketsX];

        // build buckets
        final double bucketWidth = (maxx - minx) / (double) (buckets[0].length);
        final double bucketHeight = (maxy - miny) / (double) (buckets.length);
        int numNonEmptyBuckets = 0;

        for (final IndependentPair<SpPoint2d, SpPoint2d> pair : collection) {
            final SpPoint2d first = pair.firstObject();
            final float x = first.getX();
            final float y = first.getY();

            final int bx = (int) ((x - minx) / bucketWidth);
            final int by = (int) ((y - miny) / bucketHeight);

            if (buckets[by][bx] == null) {
                buckets[by][bx] = new Bucket();
                numNonEmptyBuckets++;
            }

            buckets[by][bx].buckets.add(pair);
        }

        // compute intervals and assign buckets to the list
        bucketList = new Bucket[numNonEmptyBuckets];
        for (int y = 0, i = 0; y < buckets.length; y++) {
            for (int x = 0; x < buckets.length; x++) {
                if (buckets[y][x] != null) {
                    buckets[y][x].interval = (double) buckets[y][x].buckets.size() / (double) collection.size();
                    bucketList[i++] = buckets[y][x];
                }
            }
        }
    }

    @Override
    public List<IndependentPair<SpPoint2d, SpPoint2d>> sample(int nItems) {
        final List<IndependentPair<SpPoint2d, SpPoint2d>> sample =
                new ArrayList<IndependentPair<SpPoint2d, SpPoint2d>>(nItems);
        final boolean[] selected = new boolean[bucketList.length];
        int nSelectedBuckets = 0;

        for (int i = 0; i < nItems; i++) {
            // attempt to pick a bucket that hasn't been picked already
            int selectedBucketIdx = 0;
            for (int j = 0; j < NUM_TRIALS; j++) {
                final double r = rng.nextDouble();
                double sum = 0;
                selectedBucketIdx = -1;

                do {
                    sum += bucketList[++selectedBucketIdx].interval;
                } while (sum < r);

                if (!selected[j] || nSelectedBuckets >= selected.length) {
                    nSelectedBuckets++;
                    break;
                }
            }

            // now pick a value from that bucket
            selected[selectedBucketIdx] = true;
            final int selectedPairIdx = rng.nextInt(bucketList[selectedBucketIdx].buckets.size());
            sample.add(bucketList[selectedBucketIdx].buckets.get(selectedPairIdx));
        }

        return sample;
    }

}
