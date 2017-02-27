package cn.zxm.sparkSIFT.SIFT;

import cn.zxm.sparkSIFT.ImageBasic.SpFImage;

import java.util.ArrayList;

/**
 * Created by root on 17-2-23.
 */
public class ImageSegment {

    public static class ModelImg{
        public int row;
        public int col;
        public ModelImg(int row, int col){
            this.row = row;
            this.col = col;
        }
    }

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

        for (int i = 0; i <= roffset; i++) {
            for (int j = 0; j <= coffset; j++) {
                //System.out.println(j);
                tpixel[i][j] = opixel[i+rowStart][j+colStart];
            }
        }

        SpFImage partimg = new SpFImage(tpixel);

        return partimg;
    }

    public static ArrayList<SpFImage> DiveImgByModel(SpFImage origin,ModelImg modelImg){
        int row = modelImg.row;
        int col = modelImg.col;

        int rowParts = origin.getRows()/row;
        int colParts = origin.getCols()/col;

        ArrayList<SpFImage> imgParts = new ArrayList<SpFImage>(rowParts*colParts);

        for (int rp = 0; rp < rowParts; rp++) {
            int rowOffset;
            if (rp == rowParts-1){
                rowOffset = row + origin.getRows()%row;
            }
            else {
                rowOffset = row;
            }
            for (int cp = 0; cp < colParts; cp++) {
                int colOffset;
                if (cp == colParts-1){
                    colOffset = col+origin.getCols()%col;
                }
                else {
                    colOffset = col;
                }

                SpFImage img = GetOnePart(origin,rp*row,cp*col,rowOffset-1,colOffset-1);
                imgParts.add(img);
            }
        }

        return imgParts;
    }
}
