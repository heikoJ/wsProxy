package hj.wsProxy;

import net.bull.javamelody.MonitoredWithSpring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;

import java.util.List;

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

        long start = System.nanoTime();

        String resultPayload = Decompressor.forEncoding(contentEncoding).decompress(bytes, "UTF-8");

        long end = System.nanoTime();

        LOGGER.info("Transformation took " + (end - start) / 1000000.0 + " milliseconds");

        return new GenericMessage<>(resultPayload,message.getHeaders());

    }




    private String getContentEncoding(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_ENCODING);

        if(values==null || values.isEmpty()) {
            return null;
        }

        return values.get(0);
    }
}




