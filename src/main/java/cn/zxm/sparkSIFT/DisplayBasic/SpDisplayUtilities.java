package cn.zxm.sparkSIFT.DisplayBasic;

import cn.zxm.sparkSIFT.ImageBasic.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by root on 17-2-23.
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
    public static JFrame display(final cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> image)
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
    public static JFrame display(final BufferedImage image, final String title)
    {
        return SpDisplayUtilities.display(image, title, null);
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
    public static JFrame display(final cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> image, final String title)
    {
        return SpDisplayUtilities.display(
                SpImageUtilities.createBufferedImageForDisplay(image), title, image);
    }

    /**
     * Display an image with the given title
     *
     * @param image
     *            the image
     * @param title
     *            the title
     * @param originalImage
     *            original image
     * @return frame containing the image
     */
    public static JFrame display(final BufferedImage image,
                                 final String title, final cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> originalImage)
    {
        if (GraphicsEnvironment.isHeadless())
            return null;

        return SpDisplayUtilities.makeDisplayFrame(title, image.getWidth(),
                image.getHeight(), image, originalImage);
    }

    /**
     * An image viewer that displays and image and allows zooming and panning of
     * images.
     * <p>
     * When allowZooming is TRUE, clicking in the image will zoom in. CTRL-click
     * in the image to zoom out.
     *
     * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
     * @author David Dupplaw (dpd@ecs.soton.ac.uk)
     */
    public static class ImageComponent extends JComponent implements
            MouseListener, MouseMotionListener
    {
        /** The original image being displayed. Used for pixel interrogation */
        protected cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> originalImage;

        /** The image being displayed */
        protected BufferedImage image;

        /** Whether to allow zooming */
        private boolean allowZooming = true;

        /** Gives the image scale */
        private double scaleFactorX = 1;

        /** Gives the image scale */
        private double scaleFactorY = 1;

        /** Gives the image-coord point in the centre of the image */
        private double drawX = 0;

        /** Gives the image-coord point in the centre of the image */
        private double drawY = 0;

        /** Whether to allow dragging */
        private boolean allowDragging = true;

        /** Whether to draw the mouse over pixel colour on the next paint */
        private boolean drawPixelColour = false;

        /** The last location of the drag - x-coordinate */
        private int dragStartX = 0;

        /** The last location of the drag - y-coordinate */
        private int dragStartY = 0;


        /** The x-coordinate of the pixel being displayed */
        private int pixelX = 0;

        /** The y-coordinate of the pixel being displayed */
        private int pixelY = 0;

        /** The current mouse coordinate */
        private int mouseX = 0;

        /** The current mouse coordinate */
        private int mouseY = 0;

        /** Whether to show the XY coordinate of the mouse */
        private boolean showXY = true;

        /** The current pixel colour */
        private Float[] currentPixelColour = null;

        /** Whether to size the image to fit within the component's given size */
        private boolean autoFit = false;

        /** Whether to show pixel colours on mouse over */
        private boolean showPixelColours = true;

        /** Whether to auto resize the component to the content size */
        private boolean autoResize = false;

        /** When using autoFit, whether to keep the aspect ratio constant */
        private boolean keepAspect = true;

        /** Whether to pack the component on resize */
        private boolean autoPack = false;

        /** The last displayed image */
        private BufferedImage displayedImage = null;

        /** Draw a grid where there is no image */
        private boolean drawTransparencyGrid = false;



        /**
         * Default constructor
         */
        public ImageComponent()
        {
            this(false, false);
        }

        /**
         * Construct with given image
         *
         * @param image
         *            the image
         */
        public ImageComponent(final BufferedImage image)
        {
            this(true, true);
            this.setImage(image);
        }

        /**
         * Default constructor. Allows setting of the autoResize parameter which
         * if true changes the size of the component to fit the contents, and
         * the autoPack parameter which automatically packs the containers root
         * (if its a JFrame) whenever it is resized.
         *
         * @param autoResize
         *            automatically resize the component to the content size
         * @param autoPack
         *            automatically pack the root component on resize
         */
        public ImageComponent(final boolean autoResize, final boolean autoPack)
        {
            this(1f, autoResize, autoPack);
        }

        /**
         * Default constructor. Allows setting of the autoResize parameter which
         * if true changes the size of the component to fit the contents, and
         * the autoPack parameter which automatically packs the containers root
         * (if its a JFrame) whenever it is resized.
         *
         * @param initialScale
         *            initial scale of the image
         * @param autoResize
         *            automatically resize the component to the content size
         * @param autoPack
         *            automatically pack the root component on resize
         */
        public ImageComponent(final float initialScale,
                              final boolean autoResize, final boolean autoPack)
        {
            this.autoPack = autoPack;
            this.autoResize = autoResize;
            this.scaleFactorX = initialScale;
            this.scaleFactorY = initialScale;

            this.addMouseListener(this);
            this.addMouseMotionListener(this);

            // Add a component listener so that we can detect when the
            // component has been resized so that we can update
            this.addComponentListener(new ComponentAdapter()
            {
                @Override
                public void componentResized(final ComponentEvent e)
                {
                    ImageComponent.this.calculateScaleFactorsToFit(
                            ImageComponent.this.image, ImageComponent.this.getBounds());
                };
            });
        }

        /** List of listeners */
        private final ArrayList<ImageComponentListener> listeners =
                new ArrayList<ImageComponentListener>();

        /**
         * Listener for zoom and pan events
         *
         * @author David Dupplaw (dpd@ecs.soton.ac.uk)
         * @created 25 Jul 2012
         * @version $Author$, $Revision$, $Date$
         */
        public static interface ImageComponentListener
        {
            /**
             * Called when the image has been zoomed to the new zoom factor.
             *
             * @param newScaleFactor
             *            The new zoom factor
             */
            public void imageZoomed(double newScaleFactor);

            /**
             * Called when the image has been panned to a new position.
             *
             * @param newX
             *            The new X position
             * @param newY
             *            The new Y position
             */
            public void imagePanned(double newX, double newY);
        }

        /**
         * Move the image to the given position (image coordinates)
         *
         * @param x
         *            The x image coordinate
         * @param y
         *            The y image coordinate
         */
        public void moveTo(final double x, final double y)
        {
            if (this.drawX != x || this.drawY != y)
            {
                this.drawX = x;
                this.drawY = y;
                this.repaint();

                for (final ImageComponentListener l : this.listeners)
                    l.imagePanned(x, y);
            }
        }


        /**
         * {@inheritDoc}
         *
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(final MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON1 && this.allowZooming)
            {
                if (e.isControlDown())
                {
                    // Scale the scalars down
                    this.scaleFactorX /= 2;
                    this.scaleFactorY /= 2;

                    final double moveX = this.drawX - e.getX() / this.scaleFactorX / 2;
                    final double moveY = this.drawY - e.getY() / this.scaleFactorY / 2;
                    if (this.allowDragging)
                        this.moveTo(moveX, moveY);
                    else
                        this.moveTo(0, 0);
                }
                else
                {
                    // Scale the scalars up
                    this.scaleFactorX *= 2;
                    this.scaleFactorY *= 2;

                    // Make sure we zoom in on the bit the user clicked on
                    if (this.allowDragging)
                        this.moveTo(
                                this.drawX + e.getX() / this.scaleFactorX,
                                this.drawY + e.getY() / this.scaleFactorY);
                    else
                        this.moveTo(0, 0);
                }

                // Make sure we're not going to draw out of bounds.
                this.sanitiseVars();

                this.repaint();
            }
        }

        /**
         * Make sure the x and y position we're drawing the image in is not
         * going mad.
         */
        private void sanitiseVars()
        {
            // Make sure we're not going out of the space
            // this.moveTo(
            // Math.max(
            // this.image.getWidth() / this.scaleFactorX / 2,
            // Math.min(
            // this.drawX,
            // this.image.getWidth()
            // - (this.getWidth() / 2 / this.scaleFactorX) ) ),
            // Math.max( this.image.getHeight() / this.scaleFactorY / 2,
            // Math.min(
            // this.drawY,
            // this.image.getHeight()
            // - (this.getHeight() / 2 / this.scaleFactorY) ) ) );
        }

        /**
         * {@inheritDoc}
         *
         * @see javax.swing.JComponent#paint(java.awt.Graphics)
         */
        @Override
        public void paint(final Graphics gfx)
        {
            // Create a double buffer into which we'll draw first.
            final BufferedImage img = new BufferedImage(this.getWidth(), this.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            final Graphics2D g = (Graphics2D) img.getGraphics();

            if (this.drawTransparencyGrid)
            {
                final BufferedImage transparencyGrid = new BufferedImage(
                        this.getWidth(), this.getHeight(),
                        BufferedImage.TYPE_3BYTE_BGR);
                final Graphics tg = transparencyGrid.getGraphics();

                final int gridSizeX = (int) (20 * this.scaleFactorX);
                final int gridSizeY = (int) (20 * this.scaleFactorY);
                for (int y = 0; y < this.getHeight(); y += gridSizeY)
                {
                    for (int x = 0; x < this.getWidth(); x += gridSizeX)
                    {
                        final int c = (x / gridSizeX + y / gridSizeY) % 2;
                        if (c == 0)
                            tg.setColor(new Color(220, 220, 220));
                        else
                            tg.setColor(Color.white);

                        tg.fillRect(x, y, gridSizeX, gridSizeY);
                    }
                }

                g.drawImage(transparencyGrid, 0, 0, null);
            }

            // Draw the image
            if (this.image != null)
            {
                // Scale and translate to the image drawing coordinates
                g.scale(this.scaleFactorX, this.scaleFactorY);
                g.translate(-this.drawX, -this.drawY);

                // Blat the image to the screen
                g.drawImage(this.image, 0, 0, this.image.getWidth(),
                        this.image.getHeight(), null);

                // Reset the graphics back to the original pixel-based coords
                g.translate(this.drawX, this.drawY);
                g.scale(1 / this.scaleFactorX, 1 / this.scaleFactorY);

                // If we're to show pixel colours and we're supposed to do it
                // on this time around...
                if ((this.showPixelColours || this.showXY)
                        && this.drawPixelColour)
                {
                    final StringBuffer pixelColourStrB = new StringBuffer();

                    if (this.showXY)
                        pixelColourStrB.append("[" + this.pixelX + ","
                                + this.pixelY + "] ");

                    if (this.showPixelColours)
                        pixelColourStrB.append(Arrays
                                .toString(this.currentPixelColour));

                    // Calculate the size to draw
                    final FontMetrics fm = g.getFontMetrics();
                    final int fw = fm.stringWidth(pixelColourStrB.toString());
                    final int fh = fm.getHeight() + fm.getDescent();
                    final int p = 4; // padding
                    final int dx = 0;
                    int dy = this.getHeight() - (fh + p);

                    // If the mouse is over where we want to put the box,
                    // we'll move the box to another corner
                    if (this.mouseX <= dx + fw + p && this.mouseX >= dx &&
                            this.mouseY >= dy && this.mouseY <= dy + fh + p)
                        dy = 0;

                    // Draw a box
                    g.setColor(new Color(0, 0, 0, 0.5f));
                    g.fillRect(dx, dy, fw + p, fh + p);

                    // Draw the text
                    g.setColor(Color.white);
                    g.drawString(pixelColourStrB.toString(), dx + p / 2, dy
                            + fm.getHeight() + p / 2);
                }
            }

            // Blat our offscreen image to the screen
            gfx.drawImage(img, 0, 0, null);

            // Store this displayed image
            this.displayedImage = img;
        }

        @Override
        public void mousePressed(final MouseEvent e)
        {
            if (this.allowDragging)
            {
                this.dragStartX = e.getX();
                this.dragStartY = e.getY();
            }
        }

        @Override
        public void mouseReleased(final MouseEvent e)
        {
        }

        @Override
        public void mouseEntered(final MouseEvent e)
        {
        }

        @Override
        public void mouseExited(final MouseEvent e)
        {
            this.drawPixelColour = false;
            this.repaint();
        }

        @Override
        public void mouseDragged(final MouseEvent e)
        {
            if (!this.allowDragging)
                return;

            final int diffx = e.getX() - this.dragStartX;
            final int diffy = e.getY() - this.dragStartY;

            if (diffx == 0 && diffy == 0)
                return;

            // Update the draw position
            this.moveTo(this.drawX - diffx / this.scaleFactorX,
                    this.drawY - diffy / this.scaleFactorY);

            // Reset the draggers
            this.dragStartX = e.getX();
            this.dragStartY = e.getY();

            // Make sure the drag stays within the bounds
            this.sanitiseVars();

            // Redraw the component
            this.repaint();
        }

        @Override
        public void mouseMoved(final MouseEvent e)
        {
            if (this.image == null)
                return;

            // Convert the screen coords into image coords
            final double x = e.getX() / this.scaleFactorX + this.drawX;
            final double y = e.getY() / this.scaleFactorY + this.drawY;

            // If we're outside the image we don't print anything
            if (x >= this.image.getWidth() || y >= this.image.getHeight() ||
                    x < 0 || y < 0)
            {
                this.drawPixelColour = false;
                this.repaint();
                return;
            }

            // Pixel coordinates in the image
            this.pixelX = (int) x;
            this.pixelY = (int) y;

            this.mouseX = e.getX();
            this.mouseY = e.getY();

            this.updatePixelColours();
        }

        /**
         * Update the display of pixel colours
         */
        protected void updatePixelColours()
        {
            if (this.showPixelColours && this.image != null)
            {
                // If we don't have the original image, we'll just use the
                // colours from the BufferedImage
                if (this.originalImage == null)
                {
                    final int colour = this.image.getRGB(this.pixelX, this.pixelY);
                    this.currentPixelColour = new Float[3];
                    this.currentPixelColour[0] = (float) ((colour & 0x00ff0000) >> 16);
                    this.currentPixelColour[1] = (float) ((colour & 0x0000ff00) >> 8);
                    this.currentPixelColour[2] = (float) ((colour & 0x000000ff));
                }
                else
                {
                    // If we're outside of the original image's coordinates,
                    // we don't need to do anything else..
                    if (this.pixelX >= this.originalImage.getWidth() || this.pixelX < 0 ||
                            this.pixelY >= this.originalImage.getHeight() || this.pixelY < 0)
                        return;

                    // If we have the original image we get each of the bands
                    // from it and update the current pixel colour member
                    if (this.originalImage instanceof SpFImage)
                    {
                        final Object o = this.originalImage.getPixel(this.pixelX, this.pixelY);
                        this.currentPixelColour = new Float[1];
                        this.currentPixelColour[0] = (Float) o;
                    }
                    /*else if (this.originalImage instanceof MBFImage)
                    {
                        final MBFImage i = (MBFImage) this.originalImage;
                        this.currentPixelColour = new Float[i.numBands()];
                        for (int b = 0; b < i.numBands(); b++)
                            this.currentPixelColour[b] = i.getBand(b)
                                    .getPixel(this.pixelX, this.pixelY);
                    }*/
                }

                this.drawPixelColour = true;
                this.repaint();
            }

            if (this.showXY)
            {
                this.drawPixelColour = true;
                this.repaint();
            }
        }

        /**
         * Given an image, will calculate two scale factors for the X and Y
         * dimensions of the image, such that the image will fit within the
         * bounds.
         *
         * @param image
         *            The image to fit
         * @param bounds
         *            The bounds to fit within
         */
        private void calculateScaleFactorsToFit(final BufferedImage image,
                                                final java.awt.Rectangle bounds)
        {
            if (image == null || bounds == null)
                return;

            if (this.autoFit)
            {
                // If we can stretch the image it's pretty simple.
                if (!this.keepAspect)
                {
                    this.scaleFactorX = bounds.width / (double) image.getWidth();
                    this.scaleFactorY = bounds.height / (double) image.getHeight();
                }
                // Otherwise we need to find the ratios to fit while keeping
                // aspect
                else
                {
                    this.scaleFactorX = this.scaleFactorY = Math.min(
                            bounds.width / (double) image.getWidth(),
                            bounds.height / (double) image.getHeight());
                }
            }
        }

        /**
         * Set the image to draw
         *
         * @param image
         *            the image
         */
        public void setImage(final BufferedImage image)
        {
            this.image = image;

            if (this.autoFit)
            {
                this.calculateScaleFactorsToFit(image, this.getBounds());
            }
            else if (this.autoResize)
            {
                // If the component isn't the right shape, we'll resize the
                // component.
                if (image.getWidth() != this.getWidth() ||
                        image.getHeight() != this.getHeight())
                {
                    this.setPreferredSize(new Dimension(
                            (int) (image.getWidth() * this.scaleFactorX),
                            (int) (image.getHeight() * this.scaleFactorY)));
                    this.setSize(new Dimension(
                            (int) (image.getWidth() * this.scaleFactorX),
                            (int) (image.getHeight() * this.scaleFactorY)));
                }

                final Component c = SwingUtilities.getRoot(this);
                if (c == null)
                    return;
                c.validate();

                if (c instanceof JFrame && this.autoPack)
                {
                    final JFrame f = (JFrame) c;
                    f.pack();
                }
            }

            if (this.showPixelColours)
                // This forces a repaint if showPixelColours is true
                this.updatePixelColours();
            else
                this.repaint();
        }

        /**
         * If you want to be able to inspect the original image's pixel values
         * (rather than the generated BufferedImage) set the original image
         * here. Use null to enforce showing the BufferedImage pixel values.
         * This does not set the BufferedImage that is being used for the
         * display.
         *
         * @param image
         *            The original image.
         */
        public void setOriginalImage(final cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> image)
        {
            this.originalImage = image;
        }
    }


    /**
     * Get a frame that will display an image.
     *
     * @param title
     *            the frame title
     * @param width
     *            the frame width
     * @param height
     *            the frame height
     * @param img
     *            the image to display
     * @param originalImage
     *            the original image
     * @return A {@link JFrame} that allows images to be displayed.
     */
    public static JFrame makeDisplayFrame(final String title, final int width,
                                          final int height, final BufferedImage img,
                                          final cn.zxm.sparkSIFT.ImageBasic.SpImage<?, ?> originalImage)
    {
        final JFrame f = SpDisplayUtilities.makeFrame(title);

        final ImageComponent c = new ImageComponent();
        if (img != null){
            c.setImage(img);
        }
        c.setOriginalImage(originalImage);
        c.setSize(width, height);
        c.setPreferredSize(new Dimension(c.getWidth(), c.getHeight()));

        f.add(c);
        f.pack();
        f.setVisible(img != null);

        SpDisplayUtilities.windowCount++;

        return f;
    }

    /**
     * Make a frame with the given title.
     *
     * @param title
     *            the title
     * @return the frame
     */
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
