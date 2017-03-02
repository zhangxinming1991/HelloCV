package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.util.function.Function;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by root on 17-3-2.
 */
public class SpIndependentPair <A, B> {


    A o1;
    B o2;

    /**
     * Constructs a Pair object with two objects obj1 and obj2
     *
     * @param obj1
     *            first object in pair
     * @param obj2
     *            second objec in pair
     */
    public SpIndependentPair(final A obj1, final B obj2)
    {
        this.o1 = obj1;
        this.o2 = obj2;
    }

    /**
     * @return first object in pair
     */
    public A firstObject()
    {
        return this.o1;
    }

    /**
     * @return second object in pair
     */
    public B secondObject()
    {
        return this.o2;
    }

    /**
     * @return first object in pair
     */
    public A getFirstObject()
    {
        return this.o1;
    }

    /**
     * @return second object in pair
     */
    public B getSecondObject()
    {
        return this.o2;
    }

    /**
     * Set first object in pair to obj
     *
     * @param obj
     *            the object
     */
    public void setFirstObject(final A obj)
    {
        this.o1 = obj;
    }

    /**
     * Set second object in pair to obj
     *
     * @param obj
     *            the object
     */
    public void setSecondObject(final B obj)
    {
        this.o2 = obj;
    }

    @Override
    public String toString() {
        return "[" + this.o1 + "," + this.o2 + "]";
    }

    @Override
    public boolean equals(final Object thatObject) {
        if (!(thatObject instanceof SpIndependentPair))
            return false;
        @SuppressWarnings("rawtypes")
        final SpIndependentPair that = (SpIndependentPair) thatObject;
        return this.o1 == that.o1 && this.o2 == that.o2;
    }

    /**
     * Create a pair from the given objects.
     *
     * @param <T>
     *            Type of first object.
     * @param <Q>
     *            Type of second object.
     * @param t
     *            The first object.
     * @param q
     *            The second object.
     * @return The pair.
     */
    public static <T, Q> SpIndependentPair<T, Q> pair(final T t, final Q q) {
        return new SpIndependentPair<T, Q>(t, q);
    }

    /**
     * Extract the first objects from a list of pairs.
     *
     * @param <T>
     *            type of first object
     * @param <Q>
     *            type of second object
     * @param data
     *            the data
     * @return extracted first objects
     */
    public static <T, Q> List<T> getFirst(final Iterable<? extends SpIndependentPair<T, Q>> data) {
        final List<T> extracted = new ArrayList<T>();

        for (final SpIndependentPair<T, Q> item : data)
            extracted.add(item.o1);

        return extracted;
    }

    /**
     * Extract the second objects from a list of pairs.
     *
     * @param <T>
     *            type of first object
     * @param <Q>
     *            type of second object
     * @param data
     *            the data
     * @return extracted second objects
     */
    public static <T, Q> List<Q> getSecond(final Iterable<? extends SpIndependentPair<T, Q>> data) {
        final List<Q> extracted = new ArrayList<Q>();

        for (final SpIndependentPair<T, Q> item : data)
            extracted.add(item.o2);

        return extracted;
    }

    /**
     * Get the function that returns the first object from the pair
     *
     * @return the function that returns the first object from the pair
     */
    public static <T, Q> Function<SpIndependentPair<T, Q>, T> getFirstFunction() {
        return new Function<SpIndependentPair<T, Q>, T>() {
            @Override
            public T apply(SpIndependentPair<T, Q> in) {
                return in.o1;
            }

        };
    }

    /**
     * Get the function that returns the second object from the pair
     *
     * @return the function that returns the second object from the pair
     */
    public static <T, Q> Function<SpIndependentPair<T, Q>, Q> getSecondFunction() {
        return new Function<SpIndependentPair<T, Q>, Q>() {
            @Override
            public Q apply(SpIndependentPair<T, Q> in) {
                return in.o2;
            }
        };
    }

    /**
     * Create a pair list from the given objects.
     *
     * @param <T>
     *            Type of objects.
     * @param t
     *            The list of first objects.
     * @param q
     *            The list of second objects.
     * @return The list of pairs.
     */
    public static <T, Q> List<SpIndependentPair<T, Q>> pairList(final List<T> t, final List<Q> q) {
        final List<SpIndependentPair<T, Q>> list = new ArrayList<SpIndependentPair<T, Q>>(t.size());

        for (int i = 0; i < t.size(); i++) {
            list.add(new SpIndependentPair<T, Q>(t.get(i), q.get(i)));
        }

        return list;
    }

    /**
     * Create a new {@link SpIndependentPair} from this one with the elements
     * swapped
     *
     * @return the swapped pair
     */
    public SpIndependentPair<B, A> swap() {
        return new SpIndependentPair<B, A>(o2, o1);
    }

    /**
     * Swap the order of the pairs
     *
     * @param data
     *            the input
     * @return the swapped data
     */
    public static <T, Q> List<SpIndependentPair<? extends Q, ? extends T>> swapList(
            List<? extends SpIndependentPair<? extends T, ? extends Q>> data)
    {
        final List<SpIndependentPair<? extends Q, ? extends T>> list = new ArrayList<SpIndependentPair<? extends Q, ? extends T>>(
                data.size());

        for (int i = 0; i < data.size(); i++) {
            list.add(data.get(i).swap());
        }

        return list;
    }

}
