package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;

/**
 * Created by root on 17-2-23.
 */
public class ImageSegment {

    /**
     * 获取图片中的某一指定区域
     * @param origin
     * @param rowStart
     * @param colStart
     * @param roffset
     * @param coffset
     * @return
     */
    public static SpFImage GetOnePart(SpFImage origin, int rowStart, int colStart, int roffset, int coffset){
        float [][]opixel = origin.pixels;
        float [][]tpixel = new float[roffset+1][coffset+1];

        for (int i = 0; i <= rowStart+roffset; i++) {
            for (int j = 0; j <= colStart+coffset; j++) {
                tpixel[i][j] = opixel[i+rowStart][j+colStart];
            }
        }

        SpFImage partimg = new SpFImage(tpixel);

        return partimg;
    }
}
