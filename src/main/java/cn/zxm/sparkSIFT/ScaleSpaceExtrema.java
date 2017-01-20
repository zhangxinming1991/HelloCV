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

    public boolean isExtrema(BuildGaussPry.My_Mat []dogaussianpry,int intvl,int nOctave,int row,int col,int intvls){
        BuildGaussPry.My_Mat curScale= dogaussianpry[nOctave*(intvls+2) + intvl];
        int val = curScale.GetBlue(row,col);

        /* check for max*/
        if (val > 0){
            for (int i = -1; i <= 1 ; i++) {//intvl
                for (int r = -1; r <= 1; r++) {//row
                    for (int c = 1; c <= 1; c++) {
                        BuildGaussPry.My_Mat compare = dogaussianpry[nOctave*(intvls+2)+i];
                        if (val < compare.GetBlue(r,c))
                            return false;
                    }
                }
            }
        }
        else{//? why exit the val<0
            for (int i = -1; i <= 1 ; i++) {//intvl
                for (int r = -1; r <= 1; r++) {//row
                    for (int c = 1; c <= 1; c++) {
                        BuildGaussPry.My_Mat compare = dogaussianpry[nOctave*(intvls+2)+i];
                        if (val > compare.GetBlue(r,c))
                            return false;
                    }
                }
            }
        }
        return true;
    }

    public Vector<KeyPoint> findScaleSpaceExtrema(BuildGaussPry.My_Mat []dogaussianpry,int intvls,int nOctaves ,double contr_thr){
        Vector<KeyPoint> keypoints = new Vector<KeyPoint>();
        double prelim_contr_thr = 0.5*contr_thr/intvls;
        for (int o = 0; o < nOctaves; o++) {
            for (int i = 1; i <= intvls; i++) {
                for (int r = 5; r < dogaussianpry[o*(intvls+2)].GetRows() - 5; r++) {
                    for (int c = 0; c < dogaussianpry[o*(intvls+2)].GetCols() - 5; c++) {
                        BuildGaussPry.My_Mat curScale = dogaussianpry[o*(intvls+2) + i];
                        if (Math.abs(curScale.data[r*curScale.GetRows()+c]) > prelim_contr_thr){
                            if (isExtrema(dogaussianpry,i,o,r,c,intvls)){
                                KeyPoint point = Interp_extremum(dogaussianpry,o,i,r,c,intvls,contr_thr);
                                if (point != null){
                                    if (!Is_too_edge_like(dogaussianpry[o*(intvls+2)+i],r,c,contr_thr)){
                                       keypoints.add(point);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return keypoints;
    }

    /*边缘检测 1:yes 0:no*/
    public boolean Is_too_edge_like(BuildGaussPry.My_Mat dog_img,int row,int col,double curv_thr){
        double d,dxx,dyy,dxy,tr,det;

        d = dog_img.GetBlue(row,col);
        dxx = dog_img.GetBlue(row,col + 1) + dog_img.GetBlue(row,col - 1) - 2*d;
        dyy = dog_img.GetBlue(row + 1,col) + dog_img.GetBlue(row - 1,col) - 2*d;
        dxy = (dog_img.GetBlue(row+1,col+1) - dog_img.GetBlue(row+1,col-1) -
                dog_img.GetBlue(row-1,col+1) + dog_img.GetBlue(row-1,col-1))/4.0;

        tr = dxx + dyy;
        det = dxx * dyy - dxy * dxy;

        if (det <= 0 || tr*tr*curv_thr >= (curv_thr+1)*(curv_thr+1)*det){
            return true;
        }

        return false;
    }

    public KeyPoint Interp_extremum(BuildGaussPry.My_Mat []dogaussianpry,int octv,
                                    int intvl, int r, int c, int intvls,
                                    double contr_thr){

        int i = 0;
        Increment increment = null;
        for (i = 0; i < SIFT_MAX_INTERP_STEPS; i++) {
            increment = Interp_step(dogaussianpry,octv,intvl,r,c,intvls);
            if (Math.abs(increment.xi) < 0.5 && Math.abs(increment.xr) < 0.5 && Math.abs(increment.xc) < 0.5)
                break;

            c += Math.round(increment.xc);
            r += Math.round(increment.xr);
            intvl += Math.round(increment.xi);

            if (intvl < 1 || intvl > intvls || c < 5 || r < 5 || c >= dogaussianpry[octv*(intvls + 2)].GetRows() - 5
                    || c >= dogaussianpry[octv*(intvls + 2)].GetCols() - 5){
                return null;
            }

            i = i + 1;
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

        increment.xi = X.get(0,2);
        increment.xr = X.get(0,1);
        increment.xc = X.get(0,0);

        return increment;
    }

    public double Interp_contr(Increment increment,BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Matrix X = new Matrix(3,1);
        X.set(0,0,increment.xc);
        X.set(1,0,increment.xr);
        X.set(2,0,increment.xi);

        Matrix dD = Deriv_3D(dogaussianpry,octv,intvl,r,c,intvls).inverse();
        Matrix T = dD.times(X);

        return dogaussianpry[octv*(intvls+2)+intvl].GetBlue(r,c) + 0.5*T.get(0,0);
    }

    public Matrix Deriv_3D(BuildGaussPry.My_Mat []dogaussianpry,int octv, int intvl, int r, int c,int intvls){
        Matrix dI = new Matrix(3,1);
        double dx,dy,ds;

        BuildGaussPry.My_Mat curimg = dogaussianpry[octv*(intvls+2) + intvl];
        BuildGaussPry.My_Mat preimg = dogaussianpry[octv*(intvls+2) + intvl - 1];
        BuildGaussPry.My_Mat nextimg = dogaussianpry[octv*(intvls+2) + intvl + 1];
        dx = (curimg.GetBlue(r,c+1) - curimg.GetBlue(r,c-1))/2.0;
        dy = (curimg.GetBlue(r+1,c) - curimg.GetBlue(r-1,c))/2.0;
        ds = (nextimg.GetBlue(r,c) - preimg.GetBlue(r,c))/2.0;

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

        v = curimg.GetBlue(r,c);
        dxx = curimg.GetBlue(r,c+1) + curimg.GetBlue(r,c-1) - 2*v;
        dyy = curimg.GetBlue(r+1,c) + curimg.GetBlue(r-1,c) - 2*v;
        dss = nextimg.GetBlue(r,c) + preimg.GetBlue(r,c) - 2*v;

        dxy = (curimg.GetBlue(r+1,c+1) - curimg.GetBlue(r+1,c-1) -
                curimg.GetBlue(r-1,c+1) + curimg.GetBlue(r-1,c-1))/4.0;

        dxs = (nextimg.GetBlue(r,c+1) - nextimg.GetBlue(r,c-1) -
        preimg.GetBlue(r,c+1) + preimg.GetBlue(r,c-1))/4.0;

        dys = (nextimg.GetBlue(r+1,c) - nextimg.GetBlue(r-1,c) -
                preimg.GetBlue(r+1,c) + preimg.GetBlue(r-1,c))/4.0;

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
