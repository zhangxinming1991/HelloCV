package cn.zxm.sparkSIFT.imageKeyPoint;

import org.openimaj.io.VariableLength;

import java.io.*;
import java.util.Locale;
import java.util.Scanner;

/**
 * Created by root on 17-2-25.
 */
class SpLocalFeatureListUtils {
    protected static <T extends SpLocalFeature<?, ?>> void writeBinary(DataOutput out, SpLocalFeatureList<T> list)
            throws IOException
    {
        out.writeInt(list.size());
        out.writeInt(list.vecLength());
        for (final T k : list)
            k.writeBinary(out);
    }

    protected static <T extends SpLocalFeature<?, ?>> void writeASCII(PrintWriter out, SpLocalFeatureList<T> list)
            throws IOException
    {
        final Locale def = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);

        out.println(list.size() + " " + list.vecLength());
        for (final T k : list)
            k.writeASCII(out);

        Locale.setDefault(def);
    }

    protected static <T extends SpLocalFeature<?, ?>> void readBinary(File file,
                                                                    SpMemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
    {
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            readBinary(bis, memoryKeypointList, clz);
        } finally {
            if (bis != null)
                try {
                    bis.close();
                } catch (final IOException e) {
                }
        }
    }

    protected static <T extends SpLocalFeature<?, ?>> void readBinary(BufferedInputStream bis,
                                                                    SpMemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
    {
        DataInputStream dis = null;

        dis = new DataInputStream(bis);
        // read the header line
        dis.read(new byte[memoryKeypointList.binaryHeader().length]);
        final int nItems = dis.readInt();
        final int veclen = dis.readInt();

        memoryKeypointList.clear();
        memoryKeypointList.cached_veclen = veclen;

        for (int i = 0; i < nItems; i++) {
            final T t = newInstance(clz, veclen);
            t.readBinary(dis);

            memoryKeypointList.add(t);
        }
    }

    protected static <T extends SpLocalFeature<?, ?>> void readBinary(DataInput in,
                                                                    SpMemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
    {
        final int nItems = in.readInt();
        final int veclen = in.readInt();

        memoryKeypointList.clear();
        memoryKeypointList.cached_veclen = veclen;

        for (int i = 0; i < nItems; i++) {
            final T t = newInstance(clz, veclen);
            t.readBinary(in);

            memoryKeypointList.add(t);
        }
    }

    protected static <T extends SpLocalFeature<?, ?>> void readASCII(File file,
                                                                   SpMemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
    {
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            readASCII(bis, memoryKeypointList, clz);
        } finally {
            if (bis != null)
                try {
                    bis.close();
                } catch (final IOException e) {
                }
        }
    }

    protected static <T extends SpLocalFeature<?, ?>> void readASCII(BufferedInputStream bis,
                                                                   SpMemoryLocalFeatureList<T> memoryKeypointList, Class<T> clz) throws IOException
    {
        final Scanner in = new Scanner(bis);

        // read the header line
        final String head = in.nextLine().trim();
        final String[] h = head.split(" ");

        final int nItems = Integer.decode(h[0]);
        int veclen = 0;
        if (h.length > 1)
        {
            veclen = Integer.decode(h[1]);
        }
        else {
            veclen = Integer.decode(in.nextLine().trim());
        }

        memoryKeypointList.clear();
        memoryKeypointList.cached_veclen = veclen;

        for (int i = 0; i < nItems; i++) {
            final T t = newInstance(clz, veclen);
            t.readASCII(in);
            memoryKeypointList.add(t);
        }
    }

    public static <T> T newInstance(Class<T> cls, int length) {
        try {
            if (VariableLength.class.isAssignableFrom(cls)) {
                return cls.getConstructor(Integer.TYPE).newInstance(length);
            }

            return cls.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static int[] readHeader(File keypointFile, boolean isBinary) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(keypointFile);
            return readHeader(fis, isBinary, true);
        } finally {
            if (fis != null)
                try {
                    fis.close();
                } catch (final IOException e) {
                }
        }
    }

    protected static int[] readHeader(InputStream stream, boolean isBinary, boolean close) throws IOException {
        if (isBinary) {
            DataInputStream dis = null;
            try {
                dis = new DataInputStream(stream);

                for (int i = 0; i < SpLocalFeatureList.BINARY_HEADER.length; i++)
                    dis.readByte();

                // read the header
                final int nItems = dis.readInt();
                final int veclen = dis.readInt();

                return new int[] { nItems, veclen, 8 + SpLocalFeatureList.BINARY_HEADER.length };
            } finally {
                if (close && dis != null)
                    try {
                        dis.close();
                    } catch (final IOException e) {
                    }
            }
        } else {
            InputStreamReader fr = null;
            BufferedReader br = null;
            int nlines = 1;
            final boolean isBuffered = stream.markSupported();
            try {
                if (isBuffered)
                    stream.mark(1024);
                fr = new InputStreamReader(stream);
                br = new BufferedReader(fr);

                // read the header line
                final String head = br.readLine().trim();
                if (isBuffered)
                    stream.reset();
                final String[] h = head.split(" ");

                final int nItems = Integer.decode(h[0]);
                int veclen = 0;
                if (h.length > 1)
                {
                    veclen = Integer.decode(h[1]);
                }
                else {
                    veclen = Integer.decode(br.readLine().trim());
                    nlines++;
                }

                return new int[] { nItems, veclen, nlines };
            } finally {
                if (close && br != null)
                    try {
                        br.close();
                    } catch (final IOException e) {
                    }
                if (close && fr != null)
                    try {
                        fr.close();
                    } catch (final IOException e) {
                    }
            }
        }
    }
}
