package cn.zxm.sparkSIFT;

/**
 * Created by root on 17-1-14.
 */
import java.awt.geom.Arc2D;
import java.awt.image.BufferedImage;
import javax.imageio.*;
import java.io.File;
import java.io.IOException;

public class BilineInterpolationScale {
    public BufferedImage image;

    /*public BilineInterpolationScale(String filename) throws IOException {
        File f = new File(filename);
        image = ImageIO.read(f);
    }*/

    public static  Integer[] imgScale(Integer[] inPixelsData, int srcW, int srcH, int destW, int destH) {
        double[][][] input3DData = processOneToThreeDeminsion(inPixelsData, srcH, srcW);

        int[][][] outputThreeDeminsionData = new int[destH][destW][4];
        float rowRatio = ((float)srcH) / ((float)destH);
        float colRatio = ((float)srcW) / ((float)destW);

        for(int row = 0; row < destH; row++) {

            double srcRow = ((float)row) * rowRatio;
            double j = Math.floor(srcRow);
            double t = srcRow - j;

            for(int col = 0; col < destW; col++) {

                double srcCol = ((float)col) * colRatio;
                double k = Math.floor(srcCol);
                double u = srcCol - k;

                double coffiecent1 = (1.0d - t) * (1.0d - u);
                double coffiecent2 = (t) * (1.0d - u);
                double coffiecent3 = t * u;
                double coffiecent4 = (1.0d - t) * u;

                for (int i = 0; i < 4; i++) {
                    outputThreeDeminsionData[row][col][i] = (int)(
                            coffiecent1 * input3DData[getClip((int)j, srcH - 1, 0)][getClip((int)k, srcW - 1, 0)][i] +
                                    coffiecent2 * input3DData[getClip((int)(j + 1), srcH - 1, 0)][getClip((int)k, srcW - 1, 0)][i] +
                                    coffiecent3 * input3DData[getClip((int)(j + 1), srcH - 1, 0)][getClip((int)(k + 1),srcW - 1, 0)][i] +
                                    coffiecent4 * input3DData[getClip((int)j, srcH - 1, 0)][getClip((int)(k + 1), srcW - 1, 0)][i]
                    );

                }
            }
        }

        return convertToOneDim(outputThreeDeminsionData, destW, destH);
    }

    public Double[] imgScale_d(Double []inPixelsData,int srcW, int srcH, int destW, int destH){
        System.out.println(inPixelsData.length);
        Double[][] input3DData = processOneToTwoDeminsion(inPixelsData,srcH,srcW);
        Double[][] outputThreeDeminsionData = new Double[destH][destW];
        float rowRatio = ((float)srcH) / ((float)destH);
        float colRatio = ((float)srcW) / ((float)destW);

        for(int row = 0; row < destH; row++) {

            double srcRow = ((float)row) * rowRatio;
            double j = Math.floor(srcRow);
            double t = srcRow - j;

            for(int col = 0; col < destW; col++) {

                double srcCol = ((float)col) * colRatio;
                double k = Math.floor(srcCol);
                double u = srcCol - k;

                double coffiecent1 = (1.0d - t) * (1.0d - u);
                double coffiecent2 = (t) * (1.0d - u);
                double coffiecent3 = t * u;
                double coffiecent4 = (1.0d - t) * u;

                outputThreeDeminsionData[row][col] = (
                            coffiecent1 * input3DData[getClip((int)j, srcH - 1, 0)][getClip((int)k, srcW - 1, 0)] +
                                    coffiecent2 * input3DData[getClip((int)(j + 1), srcH - 1, 0)][getClip((int)k, srcW - 1, 0)] +
                                    coffiecent3 * input3DData[getClip((int)(j + 1), srcH - 1, 0)][getClip((int)(k + 1),srcW - 1, 0)] +
                                    coffiecent4 * input3DData[getClip((int)j, srcH - 1, 0)][getClip((int)(k + 1), srcW - 1, 0)]
                    );

                }
        }
        return convertToOneDim_d(outputThreeDeminsionData,destW,destH);
    }

    public Double[][] processOneToTwoDeminsion(Double []inPixelsData,int imgRows, int imgCols){
        Double[][] tempData = new Double[imgRows][imgCols];
        System.out.println(imgRows + ":" + imgCols);
        for(int row=0; row<imgRows; row++) {

            for (int col = 0; col < imgCols; col++) {
                int element = row * imgCols + col;
                tempData[row][col] = inPixelsData[element];
            }
        }
        return tempData;
    }

    private static  int getClip(int x, int max, int min) {
        return x > max ? max : x < min ? min : x;
    }

    public static Integer[] convertToOneDim(int[][][] data, int imgCols, int imgRows) {
        Integer[] oneDPix = new Integer[imgCols * imgRows];

        for (int row = 0, cnt = 0; row < imgRows; row++) {
            for (int col = 0; col < imgCols; col++) {

                oneDPix[cnt] =
                        ((data[row][col][0] << 24) & 0xFF000000)
                                | ((data[row][col][1] << 16) & 0x00FF0000)
                                | ((data[row][col][2] << 8) & 0x0000FF00)
                                | ((data[row][col][3]) & 0x000000FF);

                cnt++;
            }
        }
        return oneDPix;
    }

    public Double[] convertToOneDim_d(Double[][] data, int imgCols, int imgRows) {
        Double[] oneDPix = new Double[imgCols * imgRows];

        for (int row = 0, cnt = 0; row < imgRows; row++) {
            for (int col = 0; col < imgCols; col++) {

                oneDPix[cnt] = data[row][col];

                cnt++;
            }
        }
        return oneDPix;
    }

    private static  double [][][] processOneToThreeDeminsion(Integer[] oneDPix2, int imgRows, int imgCols) {
        double[][][] tempData = new double[imgRows][imgCols][4];
        for(int row=0; row<imgRows; row++) {

            int[] aRow = new int[imgCols];
            for (int col = 0; col < imgCols; col++) {
                int element = row * imgCols + col;
                aRow[col] = oneDPix2[element].intValue();
            }

            for(int col = 0; col < imgCols; col++) {
                tempData[row][col][0] = (aRow[col] >> 24) & 0xFF; // alpha
                tempData[row][col][1] = (aRow[col] >> 16) & 0xFF; // red
                tempData[row][col][2] = (aRow[col] >> 8) & 0xFF;  // green
                tempData[row][col][3] = (aRow[col]) & 0xFF;       // blue
            }

        }
        return tempData;
    }
}
