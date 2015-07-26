package hj.wsProxy;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created by heiko on 26.07.15.
 */
public class CompressionUtils {

    public static byte[] gzipString(String fromString) throws IOException {
            ByteArrayOutputStream out = new ByteArrayOutputStream(fromString.length());
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(fromString.getBytes("UTF-8"));
            gzip.close();

            return out.toByteArray();
     }

    public static byte[] deflateString(String fromString) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(fromString.length());
        DeflaterOutputStream deflate = new DeflaterOutputStream(out);
        deflate.write(fromString.getBytes("UTF-8"));
        deflate.close();

        return out.toByteArray();

    }
}
