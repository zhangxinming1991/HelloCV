package cn.zxm.sparkSIFT.imageKeyPoint;

import gnu.trove.set.hash.TByteHashSet;
import org.openimaj.math.util.distance.HammingUtils;

/**
 * Created by root on 17-2-25.
 */
public enum SpByteFVComparison implements SpByteFVComparator {
    /**
     * Euclidean distance
     * d(H1,H2) = Math.sqrt( sumI( (H1(I)-H2(I))^2 ) )
     */
    EUCLIDEAN(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double d = 0;

            for (int i=0; i<h1.length; i++) {
                double diff = (h1[i] - h2[i]);
                d += (diff * diff);
            }

            return Math.sqrt(d);
        }
    },
    /**
     * Sum-square distance
     * d(H1,H2) = sumI( (H1(I)-H2(I))^2 )
     */
    SUM_SQUARE(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double d = 0;

            for (int i=0; i<h1.length; i++) {
                double diff = (h1[i] - h2[i]);
                d += (diff * diff);
            }

            return d;
        }
    },
    /**
     * Correlation
     *
     * s(H1,H2) = sumI( H'1(I) * H'2(I) ) / sqrt( sumI[H'1(I)2]^2 * sumI[H'2(I)^2] )
     * where
     * H'k(I) = Hk(I) - (1/N) * sumJ( Hk(J) ); N=number of FeatureVector bins
     *
     */
    CORRELATION(false) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double N = h1.length;
            double SH1=0, SH2=0;

            for (int i=0; i<N; i++) {
                SH1 += h1[i];
                SH2 += h2[i];
            }
            SH1 /= N;
            SH2 /= N;

            double d = 0;
            double SH1S = 0;
            double SH2S = 0;

            for (int i=0; i<N; i++) {
                double h1prime = h1[i] - SH1;
                double h2prime = h2[i] - SH2;

                d += (h1prime * h2prime);
                SH1S += (h1prime * h1prime);
                SH2S += (h2prime * h2prime);
            }

            if (d==0) return 0;

            return d / Math.sqrt(SH1S * SH2S);
        }
    },
    /**
     * Chi-squared distance
     * d(H1,H2) = 0.5 * sumI[(H1(I)-H2(I))^2 / (H1(I)+H2(I))]
     */
    CHI_SQUARE(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double d = 0;

            for (int i=0; i<h1.length; i++) {
                double a = h1[i] - h2[i];
                double b = h1[i] + h2[i];

                if (Math.abs(b) > 0) d += a*a/b;
            }

            return d / 2;
        }
    },
    /**
     * Histogram intersection
     * s(H1,H2) = sumI( min(H1(I), H2(I)) )
     */
    INTERSECTION(false) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double d = 0;

            for (int i=0; i<h1.length; i++) {
                d += Math.min(h1[i], h2[i]);
            }

            return d;
        }
    },
    /**
     * Bhattacharyya distance
     * d(H1,H2) = sqrt( 1 - (1 / sqrt(sumI(H1(I)) * sumI(H2(I))) ) * sumI( sqrt(H1(I) * H2(I)) ) )
     */
    BHATTACHARYYA(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            final int N = h1.length;
            double SH1 = 0;
            double SH2 = 0;
            double d = 0;
            for (int i=0; i<N; i++) {
                SH1 += h1[i];
                SH2 += h2[i];
                d += Math.sqrt(h1[i] * h2[i]);
            }

            double den = SH1 * SH2;
            if (den == 0) return 1;

            d /= Math.sqrt(den);

            return Math.sqrt(1.0 - d);
        }
    },
    /**
     * Hamming Distance
     * d(H1,H2) = sumI(H1(I) == H2(I) ? 1 : 0)
     */
    HAMMING(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            int d = 0;

            for (int i=0; i<h1.length; i++)
                if (h1[i] != h2[i]) d++;

            return d;
        }
    },
    /**
     * Hamming Distance for packed bit strings
     * d(H1,H2) = sumI(H1(I) == H2(I) ? 1 : 0)
     */
    PACKED_HAMMING(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            int d = 0;

            for (int i=0; i<h1.length; i++) {
                d += HammingUtils.packedHamming(h1[i], h2[i]);
            }

            return d;
        }
    },
    /**
     * City-block (L1) distance
     * d(H1,H2) = sumI( abs(H1(I)-H2(I)) )
     */
    CITY_BLOCK(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double d = 0;

            for (int i=0; i<h1.length; i++) {
                d += Math.abs(h1[i] - h2[i]);
            }

            return d;
        }
    },
    /**
     * Cosine similarity (sim of 1 means identical)
     * s(H1,H2)=sumI(H1(I) * H2(I))) / (sumI(H1(I)^2) sumI(H2(I)^2))
     */
    COSINE_SIM(false) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double h12 = 0;
            double h11 = 0;
            double h22 = 0;

            for (int i=0; i<h1.length; i++) {
                h12 += h1[i] * h2[i];
                h11 += h1[i] * h1[i];
                h22 += h2[i] * h2[i];
            }

            return h12 / (Math.sqrt(h11) * Math.sqrt(h22));
        }
    },
    /**
     * Cosine distance (-COSINE_SIM)
     */
    COSINE_DIST(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            return -1 * COSINE_SIM.compare(h1, h2);
        }
    },
    /**
     * The arccosine of the cosine similarity
     */
    ARCCOS(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            return Math.acos( COSINE_SIM.compare(h1, h2) );
        }
    },
    /**
     * The symmetric Kullback-Leibler divergence. Vectors must only contain
     * positive values; internally they will be converted to double arrays
     * and normalised to sum to unit length.
     */
    SYMMETRIC_KL_DIVERGENCE(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double sum1 = 0;
            double sum2 = 0;
            for (int i=0; i<h1.length; i++) {
                sum1 += h1[i];
                sum2 += h2[i];
            }

            double d = 0;
            for (int i=0; i<h1.length; i++) {
                double h1n = h1[i] / sum1;
                double h2n = h2[i] / sum2;

                double q1 = h1n / h2n;
                double q2 = h2n / h1n;

                if (h1n != 0) d += (h1n * Math.log(q1) / Math.log(2));
                if (h2n != 0) d += (h2n * Math.log(q2) / Math.log(2));
            }

            return d / 2.0;
        }
    },
    /**
     * Jaccard distance. Converts each vector to a set
     * for comparison.
     */
    JACCARD_DISTANCE(true) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            TByteHashSet union = new TByteHashSet(h1);
            union.addAll(h2);

            TByteHashSet intersection = new TByteHashSet(h1);
            intersection.retainAll(h2.clone()); //retainAll sorts the input, so we need to make a copy

            return 1.0 - (((double)intersection.size()) / (double)union.size());
        }
    },
    /**
     * Inner product
     * s(H1,H2)=sumI(H1(I) * H2(I))
     */
    INNER_PRODUCT(false) {
        @Override
        public double compare(final byte[] h1, final byte[] h2) {
            if (h1.length != h2.length)
                throw new IllegalArgumentException("Vectors have differing lengths");

            double h12 = 0;

            for (int i=0; i<h1.length; i++) {
                h12 += h1[i] * h2[i];
            }

            return h12;
        }
    }
    ;

    private boolean isDistance;
    SpByteFVComparison(boolean isDistance) {
        this.isDistance = isDistance;
    }

    @Override
    public boolean isDistance() {
        return isDistance;
    }

    @Override
    public double compare(SpByteFV h1, SpByteFV h2) {
        return compare(h1.values, h2.values);
    }

    /**
     * Compare two feature vectors in the form of native arrays,
     * returning a score or distance.
     *
     * @param h1 the first feature array
     * @param h2 the second feature array
     * @return a score or distance
     */
    @Override
    public abstract double compare(byte[] h1, byte[] h2);
}
