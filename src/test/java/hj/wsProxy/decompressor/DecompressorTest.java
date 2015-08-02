package hj.wsProxy.decompressor;


import hj.wsProxy.CompressionUtils;
import static hj.wsProxy.ContentEncoding.*;

import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.Charset;

/**
 * Created by heiko on 26.07.15.
 */
public class DecompressorTest  {


    private static Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private static final String UNCOMPRESSED_STRING ="This is an uncompressed String";

    @Test
    public void testGzipDecompress() throws Exception {

        byte[] compressed = CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        String uncompressedString = Decompressor.forEncoding(GZIP).doDecompress(compressed, DEFAULT_CHARSET);

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
                            String uncompressed = Decompressor.forEncoding(GZIP).doDecompress(compressed, DEFAULT_CHARSET);
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

        String uncompressedString = Decompressor.forEncoding(DEFLATE).doDecompress(compressed, DEFAULT_CHARSET);

        Assert.assertEquals(UNCOMPRESSED_STRING, uncompressedString);

    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(null).
                decompress(uncompressed, DEFAULT_CHARSET);
    }


    @Test
    public void testGzipEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(GZIP).
                decompress(uncompressed, DEFAULT_CHARSET);

        Assert.assertNull(uncompressedString);
    }

    @Test
    public void testDeflateEncodingWithNullContent() throws Exception {
        byte[] uncompressed = null;

        String uncompressedString = Decompressor.
                forEncoding(GZIP).
                decompress(uncompressed, DEFAULT_CHARSET);

        Assert.assertNull(uncompressedString);
    }


}