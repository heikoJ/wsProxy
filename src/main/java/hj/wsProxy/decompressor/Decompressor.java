package hj.wsProxy.decompressor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


/**
 * Created by heiko on 25.07.15.
 */
public abstract class Decompressor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Decompressor.class);


    public static Decompressor forEncoding(String inputEncoding) {
        Decompressor decompressor = DecompressAlgorithm.
                forEncoding(inputEncoding).
                getDecompressor();

        LOGGER.info("Using " + decompressor.getClass().getSimpleName() + " for " + inputEncoding + " encoding");

        return decompressor;
    }



    protected String getUncompressedString(InputStream inputStream, String encoding) throws IOException {
        String result = "";

        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, encoding));
        String line;
        while ((line = bf.readLine()) != null) {
            result += line;
        }

        return result;
    }

    public String decompress(byte[] bytes, String encoding) throws IOException {
        if(bytes==null) return null;
        return decompressInternal(bytes,encoding);
    }

    protected abstract String decompressInternal(byte[] bytes, String encoding) throws IOException;

}
