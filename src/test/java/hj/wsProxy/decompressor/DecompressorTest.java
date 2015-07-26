package hj.wsProxy.decompressor;


import hj.wsProxy.CompressionUtils;
import junit.framework.TestCase;
import org.junit.Assert;

/**
 * Created by heiko on 26.07.15.
 */
public class DecompressorTest extends TestCase {


    private static final String UNCOMPRESSED_STRING ="This is an uncompressed String";

    public void testGzipDecompress() throws Exception {

        byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding("gzip").decompress(compressed,"UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }

    public void testMultiThreaded() throws Exception {



        Thread[] threads = new Thread[10];

        for(int i = 0;i<10;i++) {

            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        for(int j=0;j<10;j++) {
                            final byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING + this.toString());
                            String uncompressed = Decompressor.forEncoding("gzip").decompress(compressed, "UTF-8");
                            Assert.assertEquals(UNCOMPRESSED_STRING + this.toString(), uncompressed);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }
            };
            threads[i] = t;
            t.start();
        }

        for(int i=0;i<10;i++) {
            threads[i].join();
        }
    }

    public void testDeflateDecompress() throws Exception {

        byte[] compressed = CompressionUtils.deflateString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding("deflate").decompress(compressed,"UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }

    public void testNoCompression() throws Exception {
        byte [] uncompressed =UNCOMPRESSED_STRING.getBytes("UTF-8");

        String uncompressedString = Decompressor.forEncoding(null).decompress(uncompressed,"UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }


}