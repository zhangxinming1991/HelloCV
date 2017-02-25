package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;
import cn.zxm.sparkSIFT.ImageBasic.SpSinglebandImageProcessor;

/**
 * Created by root on 17-2-25.
 */
public class SpFImageConvolveSeparable implements SpSinglebandImageProcessor<Float, SpFImage> {

    float[] hkernel;
    float[] vkernel;

    /**
     * Specify the horizontal kernel and vertical kernel separately.
     *
     * @param hkernel
     *            horizontal kernel
     * @param vkernel
     *            vertical kernel
     */
    public SpFImageConvolveSeparable(float[] hkernel, float[] vkernel) {
        this.hkernel = hkernel;
        this.vkernel = vkernel;
    }

    /**
     * Specify a single kernel to be used as the horizontal and vertical.
     *
     * @param kernel
     *            both kernels
     */
    public SpFImageConvolveSeparable(float[] kernel) {
        this.hkernel = kernel;
        this.vkernel = kernel;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.openimaj.image.processor.ImageProcessor#processImage(org.openimaj
     * .image.Image)
     */
    @Override
    public void processImage(SpFImage image) {
        if (hkernel != null)
            convolveHorizontal(image, hkernel);
        if (vkernel != null)
            convolveVertical(image, vkernel);
    }

    /*
     * Convolve an array of data with a kernel. The data must be padded at each
     * end by half the kernel width (with replicated data or zeros). The output
     * is written back into the data buffer, starting at the beginning and is
     * valid through buffer.length-kernel.length.
     */
    protected static void convolveBuffer(float[] buffer, float[] kernel)
    {
        final int l = buffer.length - kernel.length;
        for (int i = 0; i < l; i++) {
            float sum = 0.0f;

            for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
                sum += buffer[i + j] * kernel[jj];

            buffer[i] = sum;
        }
    }

    /**
     * Convolve the image in the horizontal direction with the kernel. Edge
     * effects are handled by duplicating the edge pixels.
     *
     * @param image
     *            the image to convolve.
     * @param kernel
     *            the convolution kernel.
     */
    public static void convolveHorizontal(SpFImage image, float[] kernel) {
        final int halfsize = kernel.length / 2;

        final float buffer[] = new float[image.width + kernel.length];

        for (int r = 0; r < image.height; r++) {
            for (int i = 0; i < halfsize; i++)
                buffer[i] = image.pixels[r][0];
            for (int i = 0; i < image.width; i++)
                buffer[halfsize + i] = image.pixels[r][i];
            for (int i = 0; i < halfsize; i++)
                buffer[halfsize + image.width + i] = image.pixels[r][image.width - 1];

            // convolveBuffer(buffer, kernel);
            final int l = buffer.length - kernel.length;
            for (int i = 0; i < l; i++) {
                float sum = 0.0f;

                for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
                    sum += buffer[i + j] * kernel[jj];

                buffer[i] = sum;
            }
            // end convolveBuffer(buffer, kernel);

            for (int c = 0; c < image.width; c++)
                image.pixels[r][c] = buffer[c];
        }
    }

    /**
     * Convolve the image in the vertical direction with the kernel. Edge
     * effects are handled by duplicating the edge pixels.
     *
     * @param image
     *            the image to convolve.
     * @param kernel
     *            the convolution kernel.
     */
    public static void convolveVertical(SpFImage image, float[] kernel) {
        final int halfsize = kernel.length / 2;

        final float buffer[] = new float[image.height + kernel.length];

        for (int c = 0; c < image.width; c++) {
            for (int i = 0; i < halfsize; i++)
                buffer[i] = image.pixels[0][c];
            for (int i = 0; i < image.height; i++)
                buffer[halfsize + i] = image.pixels[i][c];
            for (int i = 0; i < halfsize; i++)
                buffer[halfsize + image.height + i] = image.pixels[image.height - 1][c];

            // convolveBuffer(buffer, kernel);
            final int l = buffer.length - kernel.length;
            for (int i = 0; i < l; i++) {
                float sum = 0.0f;

                for (int j = 0, jj = kernel.length - 1; j < kernel.length; j++, jj--)
                    sum += buffer[i + j] * kernel[jj];

                buffer[i] = sum;
            }
            // end convolveBuffer(buffer, kernel);

            for (int r = 0; r < image.height; r++)
                image.pixels[r][c] = buffer[r];
        }
    }

    /**
     * Fast convolution for separated 3x3 kernels. Only valid pixels are
     * considered, so the output image bounds will be two pixels smaller than
     * the input image on all sides (the response of the kernel to the source
     * pixel at 1,1 is stored in the destination image at 0,0)
     *
     * @param source
     *            the source image
     * @param dest
     *            the destination image
     * @param kx
     *            the x-kernel (can be null, implying [0 1 0] )
     * @param ky
     *            the y-kernel (can be null, implying [0 1 0])
     * @param buffer
     *            the working buffer (can be null, but ideally the same width as
     *            the source image)
     */
    public static void fastConvolve3(SpFImage source, SpFImage dest, float[] kx, float[] ky, float[] buffer)
    {
        final int dst_width = source.width - 2;

        if (kx == null)
            kx = new float[] { 0, 1, 0 };
        if (ky == null)
            ky = new float[] { 0, 1, 0 };

        if (buffer == null || buffer.length < source.width)
            buffer = new float[source.width];

        for (int y = 0; y <= source.height - 3; y++) {
            final float[] src = source.pixels[y];
            final float[] src2 = source.pixels[y + 1];
            final float[] src3 = source.pixels[y + 2];

            for (int x = 0; x < source.width; x++)
            {
                buffer[x] = ky[0] * src[x] + ky[1] * src2[x] + ky[2] * src3[x];
            }

            for (int x = 0; x < dst_width; x++)
            {
                dest.pixels[y][x] = kx[0] * buffer[x] + kx[1] * buffer[x + 1] + kx[2] * buffer[x + 2];
            }
        }
    }
}
