package hj.wsProxy.decompressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.InflaterInputStream;

/**
 * Created by heiko on 25.07.15.
 */
public class DeflateDecompressor extends Decompressor {


    @Override
    public String decompress(byte[] bytes, String encoding) throws IOException {
        InflaterInputStream dis = new InflaterInputStream(new ByteArrayInputStream(bytes));
        return getUncompressedString(dis,encoding);
    }

}
