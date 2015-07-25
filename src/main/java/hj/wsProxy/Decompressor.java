package hj.wsProxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by heiko on 25.07.15.
 */
public abstract class Decompressor {


    private static Map<String, Decompressor> decompressors;

    private static final Logger LOGGER = LoggerFactory.getLogger(Decompressor.class);

    static {
        decompressors = new HashMap<>();
        decompressors.put("gzip", new GZIPDecompressor());
        decompressors.put("deflate", new DeflateDecompressor());
        decompressors.put(null, new NoneDecompressor());
    }


    public static Decompressor forEncoding(String inputEncoding) {
        Decompressor decompressor = decompressors.get(inputEncoding);
        if(decompressor==null) decompressor = decompressors.get(null);

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

    public abstract String decompress(byte []bytes, String encoding) throws IOException;

}
