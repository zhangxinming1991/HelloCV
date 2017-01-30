package cn.zxm.sparkSIFT;
import cn.zxm.sparkSIFT.BuildGaussPry;

import java.security.Key;
import java.util.Vector;
import Jama.*;
/**
 * Created by root on 17-1-16.
 */
public class ScaleSpaceExtrema {

    public final static int SIFT_MAX_INTERP_STEPS = 5;

    public class Increment{
        public double xi;
        public double xr;
        public double xc;
    }

    public class KeyPoint{
        public  double x;//row
        public  double y;//col

        public int intvl;
        public int nOctave;
    }

    /**
     *
     * @param dogaussianpry
     * @param intvl
     * @param nOctave
     * @param row
     * @param col
     * @param intvls
     * @return
     */
    public boolean isExtrema(BuildGaussPry.My_Mat []dogaussianpry,int intvl,int nOctave,int row,int col,int intvls){
        if (row == 37 && col == 119){
            System.out.println(row + ":" + col);
        }
        BuildGaussPry.My_Mat curScale= dogaussianpry[nOctave*(intvls+2) + intvl];
        int curOneDiem = row*curScale.GetCols() + col;
        double val = curScale.ddate[curOneDiem];

        /* check for max*/
        if (val > 0){
            for (int i = -1; i <= 1 ; i++) {//intvl
                for (int r = -1; r <= 1; r++) {//row
                    for (int c = -1; c <= 1; c++) {
                        BuildGaussPry.My_Mat compare = dogaussianpry[nOctave*(intvls+2)+intvl+i];
                        int comOneDiem = (r+row)*compare.GetCols() + (c+col);

                        if (val < compare.ddate[comOneDiem].doubleValue())
                            return false;
                    }
                }
            }
        }
        else{//? why exit the val<0
            for (int i = -1; i <= 1 ; i++) {//intvl
                for (int r = -1; r <= 1; r++) {//row
                    for (int c = -1; c <= 1; c++) {
                        BuildGaussPry.My_Mat compare = dogaussianpry[nOctave*(intvls+2)+intvl+i];
                        int comOneDiem = (r+row)*compare.GetCols() + (c+col);
                        if (val > compare.ddate[comOneDiem].doubleValue())
                            return false;
                    }
                }
            }
        }
        return true;
    }

    public Vector<KeyPoint> findScaleSpaceExtrema(BuildGaussPry.My_Mat []dogaussianpry,int intvls,int nOctaves ,double contr_thr,int curv_thr){
        System.out.println(dogaussianpry.length);

        int is_stream_count = 0;
        int more_pre_thr = 0;
        Vector<KeyPoint> keypoints = new Vector<KeyPoint>();
        double prelim_contr_thr = 0.5*contr_thr/intvls;//contr_thr = 0.04
        for (int o = 0; o < nOctaves; o++) {//组遍历
            for (int i = 1; i <= intvls; i++) {//层遍历1～intvls层

                for (int r = 5; r < dogaussianpry[o*(intvls+2)].GetRows() - 5; r++) {//层内行遍历
                    for (int c = 5; c < dogaussianpry[o*(intvls+2)].GetCols() - 5; c++) {//层内列遍历
                        BuildGaussPry.My_Mat curScale = dogaussianpry[o*(intvls+2) + i];
                        if (r == 37 && c == 119){
                            System.out.println(r + ":" + c);
                        }
                        //if (Math.abs(curScale.data[r*curScale.GetRows()+c]) > 0.006){//第一次筛选
                        if (Math.abs(curScale.ddate[r*curScale.GetCols()+c]) > 0.006){//第一次筛选
                              more_pre_thr++;
                            if (isExtrema(dogaussianpry,i,o,r,c,intvls)){//第二层筛选
                                is_stream_count++;
                                System.out.println(is_stream_count + ":" + "(" + r + "," + c + ")");

                                KeyPoint point = Interp_extremum(dogaussianpry,o,i,r,c,intvls,contr_thr);//精确极值的坐标
                                if (point != null){
                                    if (!Is_too_edge_like(dogaussianpry[o*(intvls+2)+i],r,c,curv_thr)){
                                        point.x = point.x / 2.0;
                                        point.y = point.y / 2.0;
                                        keypoints.add(point);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.out.println("pre_thr:" + prelim_contr_thr);
        System.out.println("is_stream_count:" + is_stream_count);
        System.out.println("more the preth:" + more_pre_thr);
        return keypoints;
    }

    /*边缘检测 1:yes 0:no*/
    public boolean Is_too_edge_like(BuildGaussPry.My_Mat dog_img,int row,int col,double curv_thr){
        double d,dxx,dyy,dxy,tr,det;

        /*d = dog_img.GetBlue(row,col);
        dxx = dog_img.GetBlue(row,col + 1) + dog_img.GetBlue(row,col - 1) - 2*d;
        dyy = dog_img.GetBlue(row + 1,col) + dog_img.GetBlue(row - 1,col) - 2*d;
        dxy = (dog_img.GetBlue(row+1,col+1) - dog_img.GetBlue(row+1,col-1) -
                dog_img.GetBlue(row-1,col+1) + dog_img.GetBlue(row-1,col-1))/4.0;*/

        int onediem = row*dog_img.GetCols() + col;
        int cols = dog_img.GetCols();

        d = dog_img.ddate[onediem];
        dxx = dog_img.ddate[onediem+1] + dog_img.ddate[onediem-1] - 2*d;
        dyy = dog_img.ddate[onediem+cols] + dog_img.ddate[onediem-cols] - 2*d;
        dxy = (dog_img.ddate[onediem+cols+1] - dog_img.ddate[onediem+cols-1]-
                dog_img.ddate[onediem-cols+1] + dog_img.ddate[onediem-cols-1])/4.0;

        tr = dxx + dyy;
        det = dxx * dyy - dxy * dxy;

        if (det <= 0) {
            //System.out.println("is too edge");
            return true;
        }
        if (tr*tr*curv_thr < (curv_thr+1)*(curv_thr+1)*det){
            //System.out.println("is too edge");
            return false;
        }

        //System.out.println("is too edge");
        return true;
    }

    public KeyPoint Interp_extremum(BuildGaussPry.My_Mat []dogaussianpry,int octv,
                                    int intvl, int r, int c, int intvls,
                                    double contr_thr){

        int i;
        Increment increment = null;
        for (i = 0; i < SIFT_MAX_INTERP_STEPS; i++) {
            increment = Interp_step(dogaussianpry,octv,intvl,r,c,intvls);

            if (Math.abs(increment.xi) < 0.5 && Math.abs(increment.xr) < 0.5 && Math.abs(increment.xc) < 0.5)
                break;

            c += Math.round(increment.xc);
            r += Math.round(increment.xr);
            intvl += Math.round(increment.xi);

            if (intvl < 1 || intvl > intvls || c < 5 || r < 5 || r >= dogaussianpry[octv*(intvls + 2)].GetRows() - 5
                    || c >= dogaussianpry[octv*(intvls + 2)].GetCols() - 5){
                return null;
            }
        }

        if (i >= SIFT_MAX_INTERP_STEPS){
            return null;
        }

        double contr = Interp_contr(increment,dogaussianpry,octv,intvl,r,c,intvls);
        if (Math.abs(contr) < contr_thr/intvls){
            return null;
        }

        KeyPoint keyPoint = new KeyPoint();
        keyPoint.x = (c + increment.xc)*Math.pow(2.0,octv);
        keyPoint.y = (r + increment.xr)*Math.pow(2.0,octv);
        keyPoint.nOctave = octv;
        keyPoint.intvl = intvl;
        return keyPoint;
    }

    public Increment Interp_step(BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Increment increment = new Increment();

        Matrix dD = Deriv_3D(dogaussianpry,octv,intvl,r,c,intvls);
        Matrix H = Hessian_3D(dogaussianpry,octv,intvl,r,c,intvls);

        Matrix H_inv = H.inverse();
        Matrix X = H_inv.times(dD);
        X = X.times(-1);

        increment.xi = X.get(2,0);
        increment.xr = X.get(1,0);
        increment.xc = X.get(0,0);

        return increment;
    }

    public double Interp_contr(Increment increment,BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Matrix X = new Matrix(3,1);
        X.set(0,0,increment.xc);
        X.set(1,0,increment.xr);
        X.set(2,0,increment.xi);

        Matrix dD = Deriv_3D(dogaussianpry,octv,intvl,r,c,intvls).transpose();
        Matrix T = dD.times(X);

        int onediem = octv*(intvls+2)+intvl;
        int cols = dogaussianpry[onediem].GetCols();
        return dogaussianpry[onediem].ddate[r*cols+c] + 0.5*T.get(0,0);
    }

    public Matrix Deriv_3D(BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Matrix dI = new Matrix(3,1);
        double dx,dy,ds;

        BuildGaussPry.My_Mat curimg = dogaussianpry[octv*(intvls+2) + intvl];
        BuildGaussPry.My_Mat preimg = dogaussianpry[octv*(intvls+2) + intvl - 1];
        BuildGaussPry.My_Mat nextimg = dogaussianpry[octv*(intvls+2) + intvl + 1];

        int onediem = r*curimg.GetCols() + c;
        int cols = curimg.GetCols();

        dx = (curimg.ddate[onediem+1] - curimg.ddate[onediem-1])/2.0;
        dy = (curimg.ddate[onediem+cols] - curimg.ddate[onediem-cols])/2.0;
        ds = (nextimg.ddate[onediem] - preimg.ddate[onediem])/2.0;

        dI.set(0,0,dx);
        dI.set(1,0,dy);
        dI.set(2,0,ds);

        return dI;
    }

    public Matrix Hessian_3D(BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Matrix H = new Matrix(3,3);
        double v,dxx,dyy,dss,dxy,dxs,dys;

        BuildGaussPry.My_Mat curimg = dogaussianpry[octv*(intvls+2) + intvl];
        BuildGaussPry.My_Mat preimg = dogaussianpry[octv*(intvls+2) + intvl - 1];
        BuildGaussPry.My_Mat nextimg = dogaussianpry[octv*(intvls+2) + intvl + 1];

        int onediem = r*curimg.GetCols() + c;
        int cols = curimg.GetCols();

        v = curimg.ddate[onediem];
        dxx = curimg.ddate[onediem+1] + curimg.ddate[onediem-1] - 2*v;
        dyy = curimg.ddate[onediem+cols] + curimg.ddate[onediem-cols] - 2*v;
        dss = nextimg.ddate[onediem] + preimg.ddate[onediem] - 2*v;

        dxy = (curimg.ddate[onediem+cols+1] - curimg.ddate[onediem+cols-1] -
                curimg.ddate[onediem-cols+1] + curimg.ddate[onediem-cols-1])/4.0;

        dxs = (nextimg.ddate[onediem+1]-nextimg.ddate[onediem-1]-
        preimg.ddate[onediem+1] + preimg.ddate[onediem-1])/4.0;

        dys = (nextimg.ddate[onediem+cols] - nextimg.ddate[onediem-cols] -
        preimg.ddate[onediem+cols] + preimg.ddate[onediem-cols])/4.0;

        H.set(0,0,dxx);
        H.set(0,1,dxy);
        H.set(0,2,dxs);
        H.set(1,0,dxy);
        H.set(1,1,dyy);
        H.set(1,2,dys);
        H.set(2,0,dxs);
        H.set(2,1,dys);
        H.set(2,2,dss);

        return H;
    }
}
