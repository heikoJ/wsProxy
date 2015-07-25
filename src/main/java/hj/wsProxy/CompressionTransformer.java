package hj.wsProxy;

import net.bull.javamelody.MonitoredWithSpring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by heiko on 25.07.15.
 */

@MonitoredWithSpring

public class CompressionTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionTransformer.class);

    @Override
    protected Object doTransform(Message<?> message) throws Exception {
        byte[] bytes = (byte[])message.getPayload();

        String contentEncoding = getContentEncoding(message.getHeaders());

        LOGGER.info("Content encoding: " + contentEncoding);
        long start = System.nanoTime();

        String resultPayload;

        if ("gzip".equalsIgnoreCase(contentEncoding)) {
            resultPayload = gunzip(bytes);
        } else if("deflate".equalsIgnoreCase(contentEncoding)) {
            resultPayload = deflate(bytes);
        } else {
            resultPayload = new String(bytes, "UTF-8");
        }

        long end = System.nanoTime();

        LOGGER.info("Transformation took " + (end - start) / 1000000.0 + " milliseconds");


        return new GenericMessage<>(resultPayload,message.getHeaders());

    }


    public String gunzip(byte[] bytes) throws IOException {



        String result = "";

        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));


        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String line;
        while ((line = bf.readLine()) != null) {
            result += line;
        }


        return result;
    }


    public String deflate(byte[] bytes) throws IOException {


        String result = "";

        InflaterInputStream dis = new InflaterInputStream(new ByteArrayInputStream(bytes));



        BufferedReader bf = new BufferedReader(new InputStreamReader(dis, "UTF-8"));
        String line;
        while ((line = bf.readLine()) != null) {
            result += line;
        }



        return result;
    }




    private String getContentEncoding(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_ENCODING);

        if(values==null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}




