package cn.zxm.sparkSIFT.ImageBasic;

/**
 * Created by root on 17-2-22.
 */
public class SpRenderHints {
    /**
     * Different approaches to drawing
     */
    public static enum DrawingAlgorithm {
        /**
         * Fast drawing, no anti-aliasing
         */
        FAST,
        /**
         * Anti-aliased drawing
         */
        ANTI_ALIASED
    }

    /**
     * Fast drawing
     */
    public static final SpRenderHints FAST = new SpRenderHints(DrawingAlgorithm.FAST);

    /**
     * Anti-aliased drawing
     */
    public static final SpRenderHints ANTI_ALIASED = new SpRenderHints(DrawingAlgorithm.ANTI_ALIASED);

    protected DrawingAlgorithm drawingAlgorithm = DrawingAlgorithm.FAST;

    /**
     * Default constructor. Uses fastest drawing algorithm.
     */
    public SpRenderHints() {

    }

    /**
     * Construct with the given drawing algorithm
     * @param drawingAlgorithm the drawing algorithm
     */
    public SpRenderHints(DrawingAlgorithm drawingAlgorithm) {
        this.drawingAlgorithm = drawingAlgorithm;
    }

}
