package hj.wsProxy.decompressor;


import hj.wsProxy.CompressionUtils;
import static hj.wsProxy.ContentEncoding.*;

import org.junit.Assert;
import org.junit.Test;

/**
 * Created by heiko on 26.07.15.
 */
public class DecompressorTest  {


    private static final String UNCOMPRESSED_STRING ="This is an uncompressed String";

    @Test
    public void testGzipDecompress() throws Exception {

        byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding(GZIP.name()).decompressInternal(compressed, "UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }

    @Test
    public void testGzipDecompressWithMixedCaseEncoding() throws Exception {

        byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding("gZiP").decompressInternal(compressed, "UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }

    @Test
    public void testMultiThreaded() throws Exception {



        Thread[] threads = new Thread[10];

        for(int i = 0;i<10;i++) {

            Thread t = new Thread() {
                @Override
                public void run() {
                    try {
                        for(int j=0;j<10;j++) {
                            final byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING + this.toString());
                            String uncompressed = Decompressor.forEncoding(GZIP.name()).decompressInternal(compressed, "UTF-8");
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

    @Test
    public void testDeflateDecompress() throws Exception {

        byte[] compressed = CompressionUtils.deflateString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding(DEFLATE.name()).decompressInternal(compressed, "UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }

    @Test
    public void testNoCompression() throws Exception {
        byte [] uncompressed =UNCOMPRESSED_STRING.getBytes("UTF-8");

        String uncompressedString = Decompressor.forEncoding(null).decompressInternal(uncompressed, "UTF-8");

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }


    @Test
    public void testNoneEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(null).
                decompress(uncompressed, "UTF-8");

        Assert.assertNull(uncompressedString);
    }


    @Test
    public void testGzipEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(GZIP.name()).
                decompress(uncompressed, "UTF-8");

        Assert.assertNull(uncompressedString);
    }

    @Test
    public void testDeflateEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(GZIP.name()).
                decompress(uncompressed, "UTF-8");

        Assert.assertNull(uncompressedString);
    }


}