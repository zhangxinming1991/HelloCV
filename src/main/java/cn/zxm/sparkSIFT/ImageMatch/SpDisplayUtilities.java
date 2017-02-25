package cn.zxm.sparkSIFT.ImageMatch;

import cn.zxm.sparkSIFT.ImageBasic.SpImage;
import cn.zxm.sparkSIFT.ImageBasic.SpImageUtilities;
import org.openimaj.image.ImageUtilities;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

/**
 * Created by root on 17-2-25.
 */
public class SpDisplayUtilities {
    private static int windowCount = 0;
    private static int windowOpenCount = 0;

    /**
     * Display an image with the default name
     *
     * @param image
     *            the image
     * @return frame containing the image
     */
    public static JFrame display(final SpImage<?, ?> image)
    {
        return SpDisplayUtilities.display(image, "Image: "
                + SpDisplayUtilities.windowCount);
    }


    /**
     * Display an image with the given title
     *
     * @param image
     *            the image
     * @param title
     *            the title
     * @return frame containing the image
     */
    public static JFrame display(final SpImage<?, ?> image, final String title)
    {
        return SpDisplayUtilities.display(
                SpImageUtilities.createBufferedImageForDisplay(image), title,
                image);
    }

    public static JFrame display(final BufferedImage image,
                                 final String title, final SpImage<?, ?> originalImage)
    {
        if (GraphicsEnvironment.isHeadless())
            return null;

        return SpDisplayUtilities.makeDisplayFrame(title, image.getWidth(),
                image.getHeight(), image, originalImage);
    }

    public static JFrame makeDisplayFrame(final String title, final int width,
                                          final int height)
    {
        return SpDisplayUtilities.makeDisplayFrame(title, width, height, null);
    }

    public static JFrame makeDisplayFrame(final String title, final int width,
                                          final int height, final BufferedImage img)
    {
        return SpDisplayUtilities.makeDisplayFrame(title, width, height, img,
                null);
    }

    public static JFrame makeDisplayFrame(final String title, final int width,
                                          final int height, final BufferedImage img,
                                          final SpImage<?, ?> originalImage)
    {
        final JFrame f = SpDisplayUtilities.makeFrame(title);

        final cn.zxm.sparkSIFT.DisplayBasic.SpDisplayUtilities.ImageComponent c = new cn.zxm.sparkSIFT.DisplayBasic.SpDisplayUtilities.ImageComponent();
        if (img != null)
            c.setImage(img);
        c.setOriginalImage(originalImage);
        c.setSize(width, height);
        c.setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));

        f.add(c);
        f.pack();
        f.setVisible(img != null);

        SpDisplayUtilities.windowCount++;

        return f;
    }

    public static JFrame makeFrame(final String title)
    {
        final JFrame f = new JFrame(title);
        f.setResizable(false);
        f.setUndecorated(false);

        f.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent evt)
            {
                SpDisplayUtilities.windowOpenCount = SpDisplayUtilities.windowCount - 1;
                f.dispose();
            }
        });
        return f;
    }

}
