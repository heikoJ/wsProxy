package hj.wsProxy.decompressor;

import org.springframework.util.StringUtils;

/**
 * Created by heiko on 26.07.15.
 */
enum DecompressAlgorithm {

    GZIP(new GZIPDecompressor()),
    DEFLATE(new DeflateDecompressor()),
    NONE(new NoneDecompressor());

    private Decompressor decompressor;

    private DecompressAlgorithm(Decompressor decompressor) {
        this.decompressor = decompressor;
    }

    Decompressor getDecompressor() {
        return decompressor;
    }

    static DecompressAlgorithm forEncoding(String encoding) {
        if (StringUtils.isEmpty(encoding)) return NONE;
        try {
            return valueOf(encoding.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
        }

        return NONE;
    }
}
