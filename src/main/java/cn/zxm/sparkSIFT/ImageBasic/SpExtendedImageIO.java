package cn.zxm.sparkSIFT.ImageBasic;

import com.sun.media.jai.codec.SeekableStream;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageInfo;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageInputStream;
import javax.media.jai.JAI;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Created by root on 17-2-23.
 */
public class SpExtendedImageIO {

    static class NonClosableInputStream extends BufferedInputStream {
        public NonClosableInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
        }

        /**
         * @throws IOException
         */
        public void reallyClose() throws IOException {
            super.close();
        }
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding a supplied
     * <code>InputStream</code> with an <code>ImageReader</code> chosen
     * automatically from among those currently registered. The
     * <code>InputStream</code> is wrapped in an <code>ImageInputStream</code>.
     * If no registered <code>ImageReader</code> claims to be able to read the
     * resulting stream, <code>null</code> is returned.
     *
     * <p>
     * The current cache settings from <code>getUseCache</code>and
     * <code>getCacheDirectory</code> will be used to control caching in the
     * <code>ImageInputStream</code> that is created.
     *
     * <p>
     * This method does not attempt to locate <code>ImageReader</code>s that can
     * read directly from an <code>InputStream</code>; that may be accomplished
     * using <code>IIORegistry</code> and <code>ImageReaderSpi</code>.
     *
     * <p>
     * This method <em>does not</em> close the provided <code>InputStream</code>
     * after the read operation has completed; it is the responsibility of the
     * caller to close the stream, if desired.
     *
     * @param input
     *            an <code>InputStream</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded contents of
     *         the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException
     *                if <code>input</code> is <code>null</code>.
     * @exception IOException
     *                if an error occurs during reading.
     */
    public static BufferedImage read(InputStream input) throws IOException {
        if (input == null) {
            throw new IllegalArgumentException("input == null!");
        }

        final NonClosableInputStream buffer = new NonClosableInputStream(input);
        buffer.mark(10 * 1024 * 1024); // 10mb is big enough?

        BufferedImage bi;
        try {
            bi = readInternal(buffer);
        } catch (final Exception ex) {
            bi = null;
        }

        if (bi == null) {
            buffer.reset();
            try {
                bi = Sanselan.getBufferedImage(buffer);
            } catch (final Throwable e) {
                throw new IOException(e);
            }
        }

        return bi;
    }

    /**
     * Returns a <code>BufferedImage</code> as the result of decoding a supplied
     * <code>ImageInputStream</code> with an <code>ImageReader</code> chosen
     * automatically from among those currently registered. If no registered
     * <code>ImageReader</code> claims to be able to read the stream,
     * <code>null</code> is returned.
     *
     * @param input
     *            an <code>ImageInputStream</code> to read from.
     *
     * @return a <code>BufferedImage</code> containing the decoded contents of
     *         the input, or <code>null</code>.
     *
     * @exception IllegalArgumentException
     *                if <code>stream</code> is <code>null</code>.
     * @exception IOException
     *                if an error occurs during reading.
     * @throws ImageReadException
     */
    private static BufferedImage readInternal(BufferedInputStream binput) throws IOException, ImageReadException {
        if (binput == null) {
            throw new IllegalArgumentException("stream == null!");
        }

        ImageInfo info;
        try {
            info = Sanselan.getImageInfo(binput, null);
        } catch (final ImageReadException ire) {
            info = null;
        } finally {
            binput.reset();
        }

        if (info != null && info.getFormat() == ImageFormat.IMAGE_FORMAT_JPEG) {
            if (info.getColorType() == ImageInfo.COLOR_TYPE_CMYK) {
                final ImageReader reader = getMonkeyReader();

                if (reader == null) {
                    // fallback to the ImageIO reader... one day it might be
                    // fixed
                    return ImageIO.read(binput);
                } else {
                    return loadWithReader(reader, binput);
                }
            } else {
                // first try JAI if it's available
                try {
                    // OpenJDK7 doesn't work properly with JAI as some of the
                    // classes are missing!! This next line will throw in such
                    // cases:
                    Class.forName("com.sun.image.codec.jpeg.ImageFormatException");

                    synchronized (JAI.class) {
                        return JAI.create("stream", SeekableStream.wrapInputStream(binput, false)).getAsBufferedImage();
                    }
                } catch (final Exception e) {
                    // JAI didn't work... we'll fall back to ImageIO, but try
                    // the monkey first
                    binput.reset();

                    // First try the Monkey reader
                    final ImageReader reader = getMonkeyReader();

                    if (reader == null) {
                        // fallback to the ImageIO reader... one day it might be
                        // fixed
                        return ImageIO.read(binput);
                    } else {
                        try {
                            return loadWithReader(reader, binput);
                        } catch (final Exception ee) {
                            // fallback to the ImageIO reader... one day it
                            // might be
                            // fixed
                            return ImageIO.read(binput);
                        }
                    }
                }
            }
        } else {
            return ImageIO.read(binput);
        }
    }

    /**
     * Load an image with the given reader
     *
     * @param reader
     * @param binput
     * @return
     * @throws IOException
     */
    private static BufferedImage loadWithReader(ImageReader reader, BufferedInputStream binput) throws IOException {
        final ImageInputStream stream = ImageIO.createImageInputStream(binput);

        final ImageReadParam param = reader.getDefaultReadParam();
        reader.setInput(stream, true, true);

        return reader.read(0, param);
    }

    /**
     * Get the TwelveMonkeys reader if its present and attempt to load it if
     * necessary.
     *
     * @return the TwelveMonkeys JPEG Reader or null if it can't be loaded.
     */
    private static ImageReader getMonkeyReader() {
        Iterator<ImageReader> iter = ImageIO.getImageReadersByFormatName("jpeg");
        while (iter.hasNext()) {
            final ImageReader reader = iter.next();
            if (reader instanceof com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader)
                return reader;
        }

        IIORegistry.getDefaultInstance().registerServiceProvider(
                new com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReaderSpi());

        iter = ImageIO.getImageReadersByFormatName("jpeg");
        while (iter.hasNext()) {
            final ImageReader reader = iter.next();
            if (reader instanceof com.twelvemonkeys.imageio.plugins.jpeg.JPEGImageReader)
                return reader;
        }

        return null;
    }

}
