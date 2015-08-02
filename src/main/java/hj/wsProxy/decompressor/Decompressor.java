package hj.wsProxy.decompressor;

import hj.wsProxy.ContentEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;


/**
 * Created by heiko on 25.07.15.
 */
public abstract class Decompressor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Decompressor.class);


    public static Decompressor forEncoding(ContentEncoding inputEncoding) {
        Decompressor decompressor = DecompressImplementation.
                forEncoding(inputEncoding).
                getDecompressor();

        LOGGER.info("Using " + decompressor.getClass().getSimpleName() + " for " + inputEncoding + " encoding");

        return decompressor;
    }



    protected String getUncompressedString(InputStream inputStream, Charset charset) throws IOException {
        String result = "";

        BufferedReader bf = new BufferedReader(new InputStreamReader(inputStream, charset));
        String line;
        while ((line = bf.readLine()) != null) {
            result += line;
        }

        return result;
    }

    public String decompress(byte[] bytes, Charset charset) throws IOException {
        if(bytes==null) return null;
        return doDecompress(bytes, charset);
    }

    protected abstract String doDecompress(byte[] bytes, Charset charset) throws IOException;

}
