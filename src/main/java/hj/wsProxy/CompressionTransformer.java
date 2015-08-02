package hj.wsProxy;

import hj.wsProxy.decompressor.Decompressor;
import hj.wsProxy.profiler.Profiler;
import net.bull.javamelody.MonitoredWithSpring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.Charset;

/**
 * Created by heiko on 25.07.15.
 */

@MonitoredWithSpring
public class CompressionTransformer extends AbstractTransformer {

    private static final Logger LOGGER = LoggerFactory.getLogger(CompressionTransformer.class);

    @Override
    protected Object doTransform(Message<?> fromMessage) throws Exception {
        byte[] fromPayload = (byte[])fromMessage.getPayload();
        MessageHeaders headers = fromMessage.getHeaders();
        ContentEncoding fromContentEncoding = HeaderUtils.getContentEncoding(headers);
        Charset fromCharset = HeaderUtils.getContentCharset(headers);

        Profiler profiler = Profiler.
                forLogger(LOGGER).
                withSubject("decompression").
                start();

        String toPayload = Decompressor.
                forEncoding(fromContentEncoding).
                decompress(fromPayload, fromCharset);

        profiler.endAndPrint();

        return createUncompressedMessage(toPayload,headers);

    }



    private Message<String> createUncompressedMessage(String payload,MessageHeaders headers) {
        return MessageBuilder.
                withPayload(payload).
                copyHeaders(headers).
                removeHeader(HttpHeaders.CONTENT_ENCODING).
                build();
    }


}




