package hj.wsProxy.decompressor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

/**
 * Created by heiko on 25.07.15.
 */
public class GZIPDecompressor extends Decompressor {


    @Override
    public String decompress(byte[] bytes, String encoding) throws IOException {
        if(bytes==null) return null;
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
        return getUncompressedString(gis,encoding);
    }
}
