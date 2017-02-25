package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImageRenderer;
import cn.zxm.sparkSIFT.imageKeyPoint.SpPoint2d;
import org.openimaj.image.Image;
import org.openimaj.image.renderer.ImageRenderer;
import org.openimaj.util.pair.Pair;

import java.util.List;

/**
 * Created by root on 17-2-25.
 */
public class SpMatchingUtilities {

    /**
     * Draw matches between two images in the given colour.
     * Places the images side-by-side and draws a line
     * for each match.
     *
     * @param <T> Pixel type
     * @param <I> Image type
     * @param im1 first image
     * @param im2 second image
     * @param matches matched features between images
     * @param col the colour to draw in
     * @return image drawn on
     */
    public static <T, I extends Image<T,I>> I drawMatches(I im1, I im2, List<? extends Pair<? extends SpPoint2d>> matches, T col) {
        int newwidth = im1.getWidth() + im2.getWidth();
        int newheight = Math.max(im1.getHeight(), im2.getHeight());

        I out = im1.newInstance(newwidth, newheight);
        ImageRenderer<T, I> renderer = out.createRenderer();
        renderer.drawImage(im1, 0, 0);
        renderer.drawImage(im2, im1.getWidth(), 0);

        if (matches!=null) {
            for (Pair<? extends SpPoint2d> p : matches) {
                renderer.drawLine(	(int)p.firstObject().getX() + im1.getWidth(),
                        (int)p.firstObject().getY(),
                        (int)p.secondObject().getX(),
                        (int)p.secondObject().getY(),
                        1,col);
            }
        }

        return out;
    }
}
