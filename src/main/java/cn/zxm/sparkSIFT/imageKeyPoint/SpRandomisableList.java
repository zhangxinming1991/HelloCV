package cn.zxm.sparkSIFT.imageKeyPoint;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public interface SpRandomisableList <T> extends List<T> {

    /***
     * Extract a sublist made up of nelem elements from the this list.
     *
     * @param nelem number of elements to extract
     * @return another randomisable list
     */
    public SpRandomisableList<T> randomSubList(int nelem);

}
