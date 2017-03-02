package cn.zxm.sparkSIFT.imageKeyPoint;

import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by root on 17-3-1.
 */
public class SequenceKeypointList implements Writable {

    public ArrayWritable kps;

    public static List<SpKeypoint> GetListKps(Writable[] wkps){
        SpKeypoint[] kps = new SpKeypoint[wkps.length];
        for (int i = 0; i < wkps.length; i++) {
            SequenceKeyPoint temp = (SequenceKeyPoint) wkps[i];

            float x = temp.x.get();
            float y = temp.y.get();
            float ori = temp.ori.get();
            float scale = temp.scale.get();
            byte[]ivec = temp.ivec.getBytes();
            kps[i] = new SpKeypoint(x,y,ori,scale,ivec);
        }

        return Arrays.asList(kps);
    }

    public static SequenceKeypointList changeWriteToSq(Writable writable){
        SequenceKeypointList qe = (SequenceKeypointList) writable;
        return qe;
    }
    public SequenceKeypointList(){
        kps = new ArrayWritable(SequenceKeyPoint.class);
    }

    public SequenceKeypointList(ArrayWritable awkps){
        this.kps = awkps;
    }

    public SequenceKeypointList(Class T,Writable[] kps){
        this.kps = new ArrayWritable(T,kps);
    }

    public SequenceKeypointList(Class T){
        this.kps = new ArrayWritable(T);
    }

    @Override
    public void write(DataOutput output) throws IOException {
        kps.write(output);
    }

    @Override
    public void readFields(DataInput input) throws IOException {
        kps.readFields(input);
    }


}
