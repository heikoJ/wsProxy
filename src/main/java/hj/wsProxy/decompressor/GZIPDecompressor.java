package hj.wsProxy.decompressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.GZIPInputStream;

/**
 * Created by heiko on 25.07.15.
 */
public class GZIPDecompressor extends Decompressor {


    @Override
    public String doDecompress(byte[] bytes, Charset charset) throws IOException {
        GZIPInputStream inputStream = new GZIPInputStream(new ByteArrayInputStream(bytes));
        return getUncompressedString(inputStream, charset);
    }
}
