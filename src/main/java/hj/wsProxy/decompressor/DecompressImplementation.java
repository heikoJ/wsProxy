package hj.wsProxy.decompressor;

import hj.wsProxy.ContentEncoding;
import org.springframework.util.Assert;

/**
 * Created by heiko on 26.07.15.
 */
enum DecompressImplementation {

    GZIP(new GZIPDecompressor()),
    DEFLATE(new DeflateDecompressor());

    private Decompressor decompressor;

    private DecompressImplementation(Decompressor decompressor) {
        this.decompressor = decompressor;
    }

    Decompressor getDecompressor() {
        return decompressor;
    }

    static DecompressImplementation forEncoding(ContentEncoding encoding) {
        Assert.notNull(encoding,"Encoding must not be null");
        return valueOf(encoding.name());
    }
}
