package cn.zxm.sparkSIFT;

/**
 * Created by root on 17-2-6.
 */
public class My_ImageMat implements java.io.Serializable{
    private  int rows;
    private  int cols;
    private int picType;
    public  int data[];
    public Double ddate[];

    public My_ImageMat(int picType,int w,int h){
        this.picType = picType;
        this.rows = h;
        this.cols = w;
        //data = new Integer[w*h];
    }


    public int GetRows(){
        return this.rows;
    }

    public int GetCols(){
        return this.cols;
    }

    public int GetType(){
        return this.picType;
    }
    public void CreateData(int w,int h,int []data){
        if (data.length != w*h)
        {
            System.out.println("the length of data is wrong!");
            return;
        }
        this.cols = w;
        this.rows = h;
        this.data = data;
    }

    public void CreateDData(int w,int h,Double []ddata){
        if (ddata.length != w*h)
        {
            //            System.out.println("the length of data is wrong!");
            return;
        }
        this.cols = w;
        this.rows = h;
        this.ddate = ddata;
    }

    public short GetAlpha(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>24) & 0xff);
    }

    public short GetRed(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>16) & 0xff);
    }

    public short GetGreen(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color>>8) & 0xff);
    }

    public short GetBlue(int row,int col){
        Integer color = data[row*cols + col];
        return (short) ((color) & 0xff);
    }

    /*
        public void setRows(int rows){
            this.rows = rows;
        }

        public void setCols(int cols){
            this.cols = cols;
        }*/
}
