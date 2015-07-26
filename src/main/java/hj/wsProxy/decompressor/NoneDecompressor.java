package hj.wsProxy.decompressor;

import java.io.IOException;

/**
 * Created by heiko on 25.07.15.
 */
public class NoneDecompressor extends Decompressor {




    @Override
    public String decompress(byte[] bytes, String encoding) throws IOException {
        if(bytes==null) return null;
        return new String(bytes, encoding);
    }
}
