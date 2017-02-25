package cn.zxm.sparkSIFT.imageKeyPoint;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public interface SpConcatenatable <IN, OUT>{

    /**
     * Concatenate all the inputs with this, returning a new object that is the
     * result of the concatenation.
     *
     * @param ins
     *            the inputs
     * @return the concatenated object
     */
    public OUT concatenate(@SuppressWarnings("unchecked") IN... ins);

    /**
     * Concatenate all the inputs with this, returning a new object that is the
     * result of the concatenation.
     *
     * @param ins
     *            the inputs
     * @return the concatenated object
     */
    public OUT concatenate(List<IN> ins);

}
