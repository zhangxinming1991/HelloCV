package cn.zxm.sparkSIFT;

import java.awt.image.BufferedImage;

/**
 * Created by root on 17-2-7.
 */
public class SparkImage {

    public static BufferedImage ToGray(BufferedImage bimg){

        My_ImageMat gMat = new My_ImageMat(bimg.getType(),bimg.getWidth(),bimg.getHeight());
        gMat.data = new int[bimg.getWidth()*bimg.getHeight()];

        for (int i = 0; i < bimg.getHeight(); i++) {

            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);

                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                final short b =  (short) (color & 0xff);

                double gray =   0.3*r + 0.59*g + 0.11*b;
                int gray_i = (int) Math.round(Double.valueOf(gray));
                gMat.data[i*(bimg.getWidth())+j] = colorToRGB((byte) 255,gray_i,gray_i,gray_i);
            }
        }

        BufferedImage gimg = new BufferedImage(bimg.getWidth(),bimg.getHeight(),bimg.getType());
        gimg.setRGB(0,0,bimg.getWidth(),bimg.getHeight(),gMat.data,0,bimg.getWidth());

        return gimg;
    }

    private static int colorToRGB(byte alpha,int red,int green,int blue){
        int newPixel = 0;
        newPixel += alpha;
        newPixel = newPixel << 8;
        newPixel += red;
        newPixel = newPixel << 8;
        newPixel += green;
        newPixel = newPixel << 8;
        newPixel += blue;

        return newPixel;
    }
}
