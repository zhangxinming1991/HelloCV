package cn.zxm.sparkSIFT;

import org.apache.axis.utils.ByteArrayOutputStream;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.io.Writable;
import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.feature.local.list.MemoryLocalFeatureList;
import org.openimaj.hadoop.sequencefile.SequenceFileUtility;
import org.openimaj.hadoop.sequencefile.TextBytesSequenceFileUtility;
import org.openimaj.hadoop.tools.localfeature.HadoopLocalFeaturesTool;
import org.openimaj.hadoop.tools.sequencefile.SequenceFileTool;
import org.openimaj.image.feature.local.keypoints.Keypoint;

import java.io.*;

/**
 * Created by root on 17-2-14.
 */
public class HadoopSerializationUtil {


    public static byte[] serialize(Writable writable) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dataout = new DataOutputStream(out);

        writable.write(dataout);
        dataout.close();
        return out.toByteArray();
    }

    public static void deseriable(Writable write,byte[] bytes) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        DataInputStream datain = new DataInputStream(in);

        write.readFields(datain);
        datain.close();
    }

    public static void main(String[] args) throws Exception {
        /*MBFImage image = ImageUtilities.readMBF(new File("car2.jpg"));
        DoGSIFTEngine engine = new DoGSIFTEngine();
        LocalFeatureList<Keypoint> keypoints = engine.findFeatures(image.flatten());

        Kp kp = new Kp("simon");
        Kp[] kps = new Kp[2];
        kps[0] = new Kp("simon");
        kps[1] = new Kp("rose");

        ArrayWritable arrayWritable = new ArrayWritable(BytesWritable.class,kps);

        byte[] result = serialize(arrayWritable);
        System.out.println(StringUtils.byteToHexString(result));

        Kp d_kp = new Kp();

        ArrayWritable d_arrayWritable = new ArrayWritable(Kp.class);
        deseriable(d_arrayWritable,result);

        Kp[] d_kps = (Kp[]) d_arrayWritable.toArray();
        System.out.println(d_kps[1].name);*/

        String tmpImageSEQ_path = "hdfs://simon-Vostro-3905:9000/user/root/img_sq"; //图片集合序列文件
        String out_path = "des_out";
        String featureSq_path = "hdfs://simon-Vostro-3905:9000/user/root/featureSq";
        String featureSqfile = "hdfs://simon-Vostro-3905:9000/user/root/featureSq/part-m-00000";//特征点集合序列文件

        /*String[] cmdArgs = new String[] { "-m", "CREATE", "-kns", "FILENAME", "-o", tmpImageSEQ_path,
                "car1.jpg", "car2.jpg" };
        SequenceFileTool.main(cmdArgs);*/

        /*final String[] cmdDeArgs = new String[] { "-m", "EXTRACT", tmpImageSEQ_path, "-o", out_path,
                "-n", "KEY" };
        SequenceFileTool.main(cmdDeArgs);*/

        /*File ffile = new File(featureSq_path);
        ffile.delete();*/

        //特征提取
        HadoopLocalFeaturesTool.main(new String[] { "-D", "mapred.child.java.opts=\"-Xmx3000M\"", "-i",
                tmpImageSEQ_path, "-o", featureSq_path });

        //final LocalFeatureList<Keypoint> firstKPL = getKPLFromSequence(new Text("car2.jpg"), featureSq_path);

    }

    public static LocalFeatureList<Keypoint> getKPLFromSequence(Text text, String featureSeqFile_path) throws IOException {
        final File keyOut = new File("keypoutset");
        keyOut.delete();
        final SequenceFileUtility<Text, BytesWritable> utility = new TextBytesSequenceFileUtility(
                SequenceFileUtility.getFilePaths(featureSeqFile_path, "part")[0].toUri(), true);
        utility.findAndExport(text, keyOut.toString(), 0);
        return MemoryLocalFeatureList.read(new File(keyOut.toString(), text.toString()), Keypoint.class);
    }

    public static class Kp implements Writable{

        public Text name;

        public Kp(){
            name = new Text();
        }

        public Kp(String name){
           this.name = new Text(name);
        }

        @Override
        public void readFields(DataInput in) throws IOException {
            name.readFields(in);
        }

        @Override
        public void write(DataOutput out) throws IOException {
            name.write(out);
        }
    }

    public static class UnWriteable implements Writable{
        public String name;

        @Override
        public void readFields(DataInput in) throws IOException {

        }

        @Override
        public void write(DataOutput out) throws IOException {

        }
    }
}
