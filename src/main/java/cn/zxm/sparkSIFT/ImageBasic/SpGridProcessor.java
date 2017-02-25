package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-25.
 */
public interface SpGridProcessor<T,I extends SpImage<T,I>> extends SpProcessor<I> {

    /**
     * 	Returns the number of columns in the grid.
     *  @return the number of columns in the grid.
     */
    public abstract int getHorizontalGridElements();

    /**
     * 	Returns the number of rows in the grid.
     *  @return the number of rows in the grid.
     */
    public abstract int getVerticalGridElements();

    /**
     * 	Process the given grid element (<code>patch</code>) and returns
     * 	a single pixel value for that element.
     *
     *  @param patch The patch of the grid to process
     *  @return A single pixel value
     */
    public abstract T processGridElement( I patch );
}
