package cn.zxm.sparkSIFT;

import org.apache.commons.math3.analysis.function.Min;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * Created by root on 17-1-11.
 */
public class BuildGaussPry {

    public static  class My_Mat implements java.io.Serializable {
        private  int rows;
        private  int cols;
        private int picType;
        public  Integer data[];
        public Double ddate[];

        public My_Mat(int picType){
            this.picType = picType;
            //data = new Integer[w*h];
        }

        public int GetRows(){
            return this.rows;
        }

        public int GetCols(){
            return this.cols;
        }

        public void CreateData(int w,int h,Integer []data){
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

    private static int Edge(int i,int x,int w){
        int i_k = x + i;

        if (i_k < 0) i_k = -x;
        else if (i_k >= w) i_k = w - x - 1;
        else i_k = i;
        return i_k;
    }

    private static Double Clamp(Double t){
        if (t > 255.0){
            return 255.0;
        }
        else{
            return  t;
        }
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

    public static void CreateGaussFilter2D(int n,Double sigma,Double[] gaussfilter){
        int size = 2*n + 1;
        Double sigma22 = 2*sigma*sigma;
        Double sigma22PI = Math.PI * sigma22;

        for (int num = 0, i = -n; i <= n; i++) {
            for (long j = -n; j <= n ; j++,num++) {
                gaussfilter[num] = Math.exp(-(i*i + j*j)/sigma22)/sigma22PI;
            }
        }

        Double sum = 0.0;
        for (int i = 0; i < gaussfilter.length; i++) {
            sum = sum + gaussfilter[i];}
    //    System.out.println("sum:" + sum);
        if (sum.equals(1.0)) return;
        else {
    //        System.out.println("sum<1");
            for (int i = 0;i<gaussfilter.length;i++){
                gaussfilter[i] = gaussfilter[i]/sum;
            }
        }

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
        //        System.out.print(gaussfilter[i*size + j] + " ");
            }
        //    System.out.println();
        }
    }

    /**
     * CreateGaussFilter1D 创建高斯滤波模板
     * @param n 高斯模糊半径
     * @param sigma 尺度参数
     * @param gaussfilter 保存结果模板
     */
    public static void CreateGaussFilter1D(int n,Double sigma,Double[] gaussfilter){
        int size = 2*n + 1;
        Double sigma22 = 2*sigma*sigma;

        Double SQRT2PI = Math.sqrt(2.0*Math.PI);
        for (int num = 0, i = -n; i <= n; i++,num++) {
                gaussfilter[num] = Math.exp(-(i*i)/sigma22)/(SQRT2PI*sigma);
        }

        Double sum = 0.0;
        for (int i = 0; i < size; i++) {
            sum = sum + gaussfilter[i];
        }

        if (sum.equals(1.0)) return;
        else {
            for (int i = 0;i<size;i++){//归一化
                gaussfilter[i] = gaussfilter[i]/sum;
            }
        }
    }

    public static void ToOri(BufferedImage bimg,My_Mat oimg){
        for (int i = 0; i < bimg.getHeight(); i++) {
            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);
                oimg.data[i*(bimg.getWidth())+j] = Integer.valueOf(color);
            }
        }
    }

    /**
     * ToGray 获得原始图片的灰度图片
     * @param bimg 原始图片
     * @param gimg 结果：灰度图
     */
    public static void ToGray(BufferedImage bimg,My_Mat gimg){

        for (int i = 0; i < bimg.getHeight(); i++) {

            for (int j = 0; j < bimg.getWidth(); j++) {
                final int color = bimg.getRGB(j,i);

                final short r =   (short) ((color>>16) & 0xff);
                final short g =  (short) ((color>>8) & 0xff);
                final short b =  (short) (color & 0xff);

                double gray =   0.3*r + 0.59*g + 0.11*b;

                gimg.ddate[i*(bimg.getWidth())+j] = gray/255.0;

                int gray_i = (int) Math.round(Double.valueOf(gray));
                gimg.data[i*(bimg.getWidth())+j] = colorToRGB((byte) 255,gray_i,gray_i,gray_i);
            }
        }
    }

    public static void Blur2D(My_Mat img_mat,Double []gaussfilter,int radius){
        for (int inx = 0,i = 0; i < img_mat.rows; i++) {
            for (int j = 0; j < img_mat.cols; j++,inx++) {

                double r = 0.0,g=0.0,b = 0.0;
                for (int n = 0,k = -radius; k <=radius; k++) {

                    int k_edge = Edge(k,i,img_mat.rows);
                    for (int l = -radius; l <= radius; l++,n++) {
                        int l_edge = Edge(l,j,img_mat.cols);
                        int inx_k = inx + img_mat.cols*k_edge + l_edge;
                 //       System.out.println(inx_k + ":" + inx + ":" + n + ":" + k_edge + ":" + l_edge + ":" + l + ";" + j);

                        r += img_mat.data[inx_k]*gaussfilter[n];
                    }
                }
                Clamp(r);
                img_mat.data[inx] = Double.valueOf(r).intValue();
            }
        }
    }

    public static void Blur1D(My_Mat img_src,My_Mat img_dst,Double []gaussfilter,int radius){
        //double temp_r = 0.0;
        Integer[] data = new Integer[img_src.rows*img_src.cols];
        Double[] ddata = new Double[img_src.rows*img_src.cols];
        //System.out.println("src:" + img_src.rows*img_src.cols + " " + "dst:" + img_dst.rows*img_dst.cols);
        Double []temp_r = new Double[img_src.rows*img_src.cols];

        for (int inx = 0,i = 0; i < img_src.rows; i++) {
            for (int j = 0; j < img_src.cols; j++,inx++) {
                //double temp_r = 0.0;
                temp_r[inx] = 0.0;
                for (int n = 0,l = -radius; l <= radius; l++,n++) {
                    int l_edge = Edge(l,j,img_src.cols);
                    int inx_k = inx + l_edge;
                //    System.out.println(inx_k + ":" + n);
                    temp_r[inx] += img_src.data[inx_k]*gaussfilter[n];
                }
                //img_mat.data[inx] = Double.valueOf(temp_r).intValue();
            }
        }

        for (int inx = 0,i = 0; i < img_src.cols; i++) {
            for (int j = 0; j < img_src.rows; j++) {
                double r = 0.0;
                inx = j*img_src.cols + i;
                for (int n = 0,k = -radius; k <=radius; k++,n++) {
                    int k_edge = Edge(k,j,img_src.rows);
                    int inx_k = inx + img_src.cols*k_edge;
                    r += temp_r[inx_k]*gaussfilter[n];
                }
                Clamp(r);
                ddata[inx] = Double.valueOf(r)/255.0;
                data[inx] = Double.valueOf(r).intValue();
                //img_dst.data[inx] = Double.valueOf(r).intValue();
            }
        }

        img_dst.CreateData(img_src.cols,img_src.rows,data);
        img_dst.CreateDData(img_src.cols,img_src.rows,ddata);
    }

    /**
     * Blur1D_d 一维高斯模糊
     * @param img_src 输入
     * @param img_dst 输出
     * @param gaussfilter 高斯模板
     * @param radius 模糊半径
     */
    public static void Blur1D_d(My_Mat img_src,My_Mat img_dst,Double []gaussfilter,int radius){

        Integer[] data = new Integer[img_src.rows*img_src.cols];
        Double[] ddata = new Double[img_src.rows*img_src.cols];

        Double []temp_r = new Double[img_src.rows*img_src.cols];

        for (int inx = 0,i = 0; i < img_src.rows; i++) {
            for (int j = 0; j < img_src.cols; j++,inx++) {
                temp_r[inx] = 0.0;
                for (int n = 0,l = -radius; l <= radius; l++,n++) {
                    int l_edge = Edge(l,j,img_src.cols);
                    int inx_k = inx + l_edge;
                    temp_r[inx] += img_src.ddate[inx_k]*gaussfilter[n];
                }
            }
        }

        for (int i = 0; i < img_src.cols; i++) {
            for (int j = 0; j < img_src.rows; j++) {
                double r = 0.0;
                int inx = j*img_src.cols + i;
                for (int n = 0,k = -radius; k <=radius; k++,n++) {
                    int k_edge = Edge(k,j,img_src.rows);
                    int inx_k = inx + img_src.cols*k_edge;
                    r += temp_r[inx_k]*gaussfilter[n];
                }
                if (r > 1.0)
                    r = 1.0;
                ddata[inx] = r;
                data[inx] = (int) Math.round(Clamp(r*255));
            }
        }

        img_dst.CreateData(img_src.cols,img_src.rows,data);
        img_dst.CreateDData(img_src.cols,img_src.rows,ddata);
    }

    /**
     * Subtract_Gray 矩阵相减
     * @param minuend
     * @param subtrahend
     * @param result
     */
    public static void Subtract_Gray(My_Mat minuend,My_Mat subtrahend,My_Mat result){

        short p;
        Double p_d;
        Double color_d[] = new Double[minuend.rows * minuend.cols];
        Integer color[] = new Integer[minuend.rows * minuend.cols];
        for (int r = 0; r < minuend.rows; r++) {
            for (int c = 0; c < minuend.cols; c++) {

                Double gray1_d = minuend.ddate[r*minuend.cols + c];
                Double gray2_d = subtrahend.ddate[r*minuend.cols + c];
                p_d = gray1_d - gray2_d;

                if (p_d < 0)
                    p = 0;//?是否为0
                else
                    p = (short) Math.round(p_d * 255.0);

                color[r*minuend.cols + c] = (int) p;
                color_d[r*minuend.cols + c] = p_d;
            }
        }

        result.CreateData(minuend.cols,minuend.rows,color);
        result.CreateDData(minuend.cols,minuend.rows,color_d);
    }

    /**
     * CreateGaussinPry 创建高斯塔
     * @param img_mat save the results of gaussian pry
     * @param intvls the layer num of per group
     * @param nOctaves the num of group
     * @param sig the coefficient of gaussianfilter formula
     */
    public static void CreateGaussinPry (My_Mat []img_mat,int intvls,int nOctaves,Double []sig,int radius) throws IOException {

        for (int o = 0; o < nOctaves; o++) {
            for (int i = 0; i < intvls + 3; i++) {
                My_Mat dst = img_mat[o*(intvls+3) + i];
                if (i == 0 && o == 0){
                    continue;
                }
                else if(i == 0){
                    int w = img_mat[(o-1)*(intvls+3)+intvls].cols;
                    int h = img_mat[(o-1)*(intvls+3)+intvls].rows;

                    Double[] src = img_mat[(o-1)*(intvls+3)+intvls].ddate;

                    BilineInterpolationScale bps = new BilineInterpolationScale();

                    Double [] resized_ddate = bps.imgScale_d(src,w,h,w/2,h/2);//双线性插值

                    dst.CreateDData(w/2,h/2,resized_ddate);

            /*        BiCubicInterpolationScale cps = new BiCubicInterpolationScale();//立方插值
                    Integer[] srcI = img_mat[(o-1)*(intvls+3)+intvls].data;*/
                }
                else{
                    Double []gaussfilter1D = new Double[(2*radius + 1)];
                    CreateGaussFilter1D(radius,sig[i],gaussfilter1D);

                    My_Mat src = img_mat[o*(intvls + 3) + i-1];

                    Blur1D_d(src,dst,gaussfilter1D,radius);
                }
            }
        }
    }

    /**
     * CreateDogGaussinPry 创建差分高斯塔
     * @param gaussian 输入：高斯塔
     * @param doggaussian 输出：差分高斯塔
     * @param intvls 高斯塔层数
     * @param nOctaves 组数
     * @throws IOException
     */
    public static void CreateDogGaussinPry(My_Mat []gaussian,My_Mat []doggaussian,int intvls,int nOctaves) throws IOException {

        for (int o = 0; o < nOctaves; o++) {
            for (int i = 0; i < intvls+2; i++) {
                int one_dim_g = o*(intvls+3) + i;
                int one_dim_d = o*(intvls+2) + i;

                Subtract_Gray(gaussian[one_dim_g+1],gaussian[one_dim_g],doggaussian[one_dim_d]);

                String tileNamed = "dogmes/dog_" + o + "_" + (i);
                SavePicPixel(tileNamed,doggaussian[one_dim_d],"FLOAT");
            }
        }
    }

    public static void ShowPic(My_Mat showimg,String titleName){

        //List<Integer> color_temp = GrayToColor(ctx,Arrays.asList(showimg.data));
        List<Integer> color_temp = Arrays.asList(showimg.data);
        int []color = ChangeIntegerToInt_Array(color_temp.toArray(new Integer[color_temp.size()]));
        BufferedImage gray_temp = new BufferedImage(showimg.cols,showimg.rows,showimg.picType);
        gray_temp.setRGB(0,0,showimg.cols,showimg.rows,color,0,showimg.cols);

        JFrame frame = new JFrame(titleName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new JLabel(new ImageIcon(gray_temp)));
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * GrayToColor 将灰度值转变成rgb值
     * @param ctx
     * @param gray
     * @return
     */
    public static List<Integer> GrayToColor(JavaSparkContext ctx,List<Integer> gray){
        JavaRDD rdd_img_mat= ctx.parallelize(gray,1);
        List<Integer> color_temp = rdd_img_mat.map(new Function<Integer,Integer>() {
            @Override
            public Integer call(Integer v1) throws Exception {
                byte alpha = (byte) 255;
                return  colorToRGB(alpha,v1.intValue(),v1.intValue(),v1.intValue());
            }
        }).collect();

        return color_temp;
    }

    public static int[] ChangeIntegerToInt_Array(Integer []Integer_Color){
        int []color = new int[Integer_Color.length];
        for (int i = 0;i<Integer_Color.length;i++) {
            color[i] = Integer_Color[i].intValue();
        }
        return color;
    }

    public static void SavePic(JavaSparkContext ctx,My_Mat showimg,String picName) throws IOException {
        List<Integer> color_temp = GrayToColor(ctx,Arrays.asList(showimg.data));
        int []color = ChangeIntegerToInt_Array(color_temp.toArray(new Integer[color_temp.size()]));
        BufferedImage gray_temp = new BufferedImage(showimg.cols,showimg.rows,showimg.picType);
        gray_temp.setRGB(0,0,showimg.cols,showimg.rows,color,0,showimg.cols);

        File newFile = new File(picName);
        ImageIO.write(gray_temp,"jpg",newFile);
    }

    public static Double[] CreateSigma(int intvls){
        final Double k = Math.pow(2,1.0/intvls);

        Double[] sig = new Double[intvls+3];

        sig[0] = 1.6;
        sig[1] = sig[0] * Math.sqrt(k*k - 1);

        for (int i = 2; i < sig.length; i++) {
            sig[i] = sig[i-1]*k;
        }

        return sig;
        /*JavaRDD rdd_sig = ctx.parallelize(Arrays.asList(sig),1);

        FlatMapFunction<Iterator<Double>,Double> make_sig = new FlatMapFunction<Iterator<Double>, Double>() {
            @Override
            public Iterator<Double> call(Iterator<Double> doubleIterator) throws Exception {
                int i = 0;
                Double []result = new Double[6];
                while (doubleIterator.hasNext()){
                    if (i < 2)
                    {
                        result[i] = doubleIterator.next();
                    }
                    else{
                        result[i] = result[i - 1]*k;
                        doubleIterator.next();
                    }
                    i++;
                }
                return Arrays.asList(result).iterator();
            }
        };

        List<Double> sigma = rdd_sig.mapPartitions(make_sig).collect();

        return sigma.toArray(new Double[intvls+3]);*/
    }

    /**
     * CreateInitImg 初始化高斯塔最底层的
     * @param srcImg
     * @param sigma
     * @param radius
     * @throws IOException
     */
    public static void CreateInitImg(My_Mat srcImg,Double sigma,int radius) throws IOException {
        SavePicPixel("init_imgmes/scale_before.txt",srcImg,"FLOAT");

        BiCubicInterpolationScale cps = new BiCubicInterpolationScale();

        int re_r = srcImg.rows*2;
        int re_c = srcImg.cols*2;

        srcImg.data = cps.imgScale(srcImg.data,srcImg.cols,srcImg.rows,re_c,re_r);
        srcImg.rows = re_r;
        srcImg.cols = re_c;

        Double []ddata = new Double[srcImg.rows*srcImg.cols];

        for (int i = 0; i < srcImg.rows*srcImg.cols; i++) {
            ddata[i] = (srcImg.data[i] & 0x000000ff) / 255.0;
        }

        srcImg.ddate = ddata;
        SavePicPixel("init_imgmes/smooth_before.txt",srcImg,"FLOAT");
        ShowPic(srcImg,"2_scale");

        Double sig_diff = Math.sqrt( sigma * sigma - 0.5 * 0.5 * 4 );
        Double []gaussfilter1D = new Double[(2*radius + 1)];
        CreateGaussFilter1D(radius,sig_diff,gaussfilter1D);

        Blur1D_d(srcImg,srcImg,gaussfilter1D,radius);
    }

    public static void SavePicPixel(String filename,My_Mat pic,String type) throws IOException {
        File file = new File(filename);
        FileWriter outputStream = new FileWriter(file);

        if (type.equals("INT"))
        {
            for (int r = 0; r < pic.GetRows(); r++) {
                outputStream.write("row:[" + r + "]\n");
                for (int c = 0; c < pic.GetCols(); c++) {
                    int onediem = r*pic.GetCols() + c;
                    short gray = pic.GetBlue(r,c);
                    outputStream.write(c + ":" + gray + "\n");
                }
                outputStream.write("\n");
            }
        }
        else {
            for (int r = 0; r < pic.GetRows(); r++) {
                outputStream.write("row:[" + r + "]\n");
                for (int c = 0; c < pic.GetCols(); c++) {
                    int onediem = r*pic.GetCols() + c;
                    Double gray_d = pic.ddate[r*pic.GetCols()+c];
                    outputStream.write(c + ":" + gray_d + "\n");
                }
                outputStream.write("\n");
            }
        }
        outputStream.close();
    }

    public static void SavedogPixel(String filename,My_Mat dog,My_Mat src1,My_Mat src2) throws IOException {
        File file = new File(filename);
        FileWriter outputStream = new FileWriter(file);

        for (int r = 0; r < dog.GetRows(); r++) {
            outputStream.write("row:[" + r + "]\n");
            for (int c = 0; c < dog.GetCols(); c++) {
                short gray = dog.GetBlue(r,c);
                int onediem = r*dog.GetCols() + c;
                Double dog_gray = dog.ddate[onediem];
                Double src1_gray = src1.ddate[onediem];
                Double src2_gray = src2.ddate[onediem];
                outputStream.write(c + ":" + "(" + src1_gray + "-" + src2_gray + ")" + dog_gray + "\n");
            }
            outputStream.write("\n");
        }
    }


    public static void main(String[] args) throws IOException {
/*        SparkConf sparkConf = new SparkConf()
                .setAppName("BuildGaussPry")
                .set("spark.cores.max","4");
        final JavaSparkContext ctx = new JavaSparkContext(sparkConf);*/

        int radius = 5;//高斯模糊半径

        BufferedImage bimg = ImageIO.read(new File("dataset_500k/car2.jpg"));//原始图片

        Integer intvls = 3;//高斯塔每组的层数 （实际为3+3层）

        Double nOctaves_d = Math.log( new Min().value(bimg.getWidth(), bimg.getHeight() ) ) / Math.log(2.0) - 2;
        Integer nOctaves = nOctaves_d.intValue();//高斯塔组数

        My_Mat []img_pry = new My_Mat[nOctaves*(intvls+3)];//高斯塔
        My_Mat[] doggaussian = new My_Mat[nOctaves*(intvls+2)];//差分高斯塔
        for (int i = 0; i < img_pry.length; i++) {
            img_pry[i] = new My_Mat(bimg.getType());
        }
        for (int i = 0; i < doggaussian.length; i++) {
            doggaussian[i] = new My_Mat(bimg.getType());
        }
        img_pry[0].CreateData(bimg.getWidth(),bimg.getHeight(),new Integer[bimg.getWidth()*bimg.getHeight()]);
        img_pry[0].CreateDData(bimg.getWidth(),bimg.getHeight(),new Double[bimg.getWidth()*bimg.getHeight()]);

        ToGray(bimg,img_pry[0]);//取得灰度图

        long startMili = System.currentTimeMillis();

        /*build gaussian pry*/
        Double[] sigma = CreateSigma(intvls);//初始化尺度参数
        CreateInitImg(img_pry[0],sigma[0],radius);//初始化高斯塔最底层的图片

        SavePicPixel("init_imgmes/init_img.txt",img_pry[0],"FLOAT");//保存init_img

        CreateGaussinPry(img_pry,intvls,nOctaves,sigma,radius);//创建高斯塔
        CreateDogGaussinPry(img_pry,doggaussian,intvls,nOctaves);//创建差分高斯塔
        /*build gaussian pry*/

        long endMili = System.currentTimeMillis();
        System.out.println((endMili-startMili) + "ms" );

        double contr_thr = 0.04;
        int curv_thr = 10;
        ScaleSpaceExtrema sse = new ScaleSpaceExtrema();
        Vector<ScaleSpaceExtrema.KeyPoint>vkeys = sse.findScaleSpaceExtrema(doggaussian,intvls,nOctaves,contr_thr,curv_thr);
        System.out.println("keypoints:" + vkeys.size());

        for (int i = 0; i < vkeys.size(); i++) {
            System.out.println(vkeys.get(i).x + "," + vkeys.get(i).y);
        }
    }
}
