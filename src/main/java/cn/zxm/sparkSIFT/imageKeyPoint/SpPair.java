package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.util.pair.IndependentPair;

import java.io.Serializable;

/**
 * Created by root on 17-3-2.
 */
public class SpPair <T> extends SpIndependentPair<T, T> implements Serializable{

    /**
     * Constructs a Pair object with two objects obj1 and obj2
     *
     * @param obj1
     *            first object in pair
     * @param obj2
     *            second objec in pair
     */
    public SpPair(T obj1, T obj2) {
        super(obj1, obj2);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return o1 + " -> " + o2;
    }

}
