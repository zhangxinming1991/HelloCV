package cn.zxm.sparkSIFT.imageKeyPoint;

/**
 * Created by root on 17-2-25.
 */
public interface SpDistanceComparator<T> {

    /**
     * Compare two objects, returning a score
     * or distance.
     *
     * @param o1 the first object
     * @param o2 the second object
     * @return a score or distance
     */
    public abstract double compare(T o1, T o2);

    /**
     * @return true if the comparison is a distance; false if similarity.
     */
    public abstract boolean isDistance();

}
