package cn.zxm.sparkSIFT.ImageBasic;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by root on 17-2-23.
 */
public class SpImageUtilities {
    /**
     * Reads an {@link SpFImage} from the given input stream.
     *
     * @param input
     *            The input stream to read the {@link SpFImage} from.
     * @return An {@link SpFImage}
     * @throws IOException
     *             if the stream cannot be read
     */
    public static SpFImage readF(final InputStream input) throws IOException {
        try{
            return SpImageUtilities.createFImage(SpExtendedImageIO.read(input));
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        //return SpImageUtilities.createFImage(SpExtendedImageIO.read(input));
    }

    /**
     * Create an FImage from a buffered image.
     *
     * @param image
     *            the image
     * @return an FImage representation of the input image
     */
    public static SpFImage createFImage(final BufferedImage image) {
        final BufferedImage bimg = SpImageUtilities.createWorkingImage(image);
        final int[] data = bimg.getRGB(0, 0, bimg.getWidth(), bimg.getHeight(), null, 0, bimg.getWidth());

        return new SpFImage(data, bimg.getWidth(), bimg.getHeight());
    }

    /**
     * Returns a ARGB BufferedImage, even if the input BufferedImage is not ARGB
     * format.
     *
     * @param bimg
     *            The {@link BufferedImage} to normalise to ARGB
     * @return An ARGB {@link BufferedImage}
     */
    public static BufferedImage createWorkingImage(final BufferedImage bimg) {
        // to avoid performance complications in the getRGB method, we
        // pre-calculate the RGB rep of the image
        BufferedImage workingImage;
        if (bimg.getType() == BufferedImage.TYPE_INT_ARGB) {
            workingImage = bimg;
        } else {
            workingImage = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_ARGB);
            final Graphics2D g2d = workingImage.createGraphics();
            g2d.drawImage(bimg, null, 0, 0);
        }
        return workingImage;
    }

    /** Lookup table for byte->float conversion */
    public final static float[] BYTE_TO_FLOAT_LUT;

    // Static initialisation
    static {
        BYTE_TO_FLOAT_LUT = new float[256];
        for (int i = 0; i < SpImageUtilities.BYTE_TO_FLOAT_LUT.length; i++)
            SpImageUtilities.BYTE_TO_FLOAT_LUT[i] = i / 255f;
    }


    /**
     * Convert any image to a {@link BufferedImage}.
     *
     * @param img
     *            image to convert
     * @return BufferedImage representation
     */
    public static BufferedImage createBufferedImageForDisplay(final SpImage<?, ?> img) {

            return SpImageUtilities.createBufferedImage((SpFImage) img);
    }

    /**
     * Efficiently create a TYPE_BYTE_GRAY for display. This is typically much
     * faster than to create and display than an ARGB buffered image.
     *
     * @param img
     *            the image to convert
     * @return the converted image
     */
    public static BufferedImage createBufferedImage(final SpFImage img) {
        return SpImageUtilities.createBufferedImage(img, null);
    }

    /**
     * Checks whether the width and height of all the given images match.
     *
     * @param images
     *            The images to compare sizes.
     * @return TRUE if all the images are the same size; FALSE otherwise
     */
    protected static boolean checkSameSize(final SpImage<?, ?>... images) {
        if (images == null || images.length == 0)
            return true;

        final SpImage<?, ?> image = images[0];
        final int w = image.getWidth();
        final int h = image.getHeight();

        return SpImageUtilities.checkSize(h, w, images);
    }

    protected static boolean checkSize(final int h, final int w, final SpImage<?, ?>... images) {
        for (final SpImage<?, ?> image : images)
            if (image.getHeight() != h || image.getWidth() != w)
                return false;
        return true;
    }

    /**
     * Efficiently create a TYPE_BYTE_GRAY for display. This is typically much
     * faster than to create and display than an ARGB buffered image.
     *
     * @param img
     *            the image to convert
     * @param ret
     *            BufferedImage to draw into if possible. Can be null.
     * @return the converted image
     */
    public static BufferedImage createBufferedImage(final SpFImage img, BufferedImage ret) {
        final int width = img.getWidth();
        final int height = img.getHeight();

        if (ret == null || ret.getWidth() != width || ret.getHeight() != height
                || ret.getType() != BufferedImage.TYPE_BYTE_GRAY)
            ret = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

        final WritableRaster raster = ret.getRaster();

        final float[][] p = img.pixels;

        final ComponentSampleModel sm = (ComponentSampleModel) raster.getSampleModel();
        final DataBufferByte db = (DataBufferByte) raster.getDataBuffer();
        final int scanlineStride = sm.getScanlineStride();
        final int pixelStride = sm.getPixelStride();

        final byte[] data = db.getData();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                data[y * scanlineStride + x * pixelStride] = (byte) (Math.max(0, Math.min(255, (int) (p[y][x] * 255))));
            }
        }

        return ret;
    }
}
