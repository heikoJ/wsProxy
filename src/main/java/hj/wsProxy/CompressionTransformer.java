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
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;

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
        String fromContentEncoding = getContentEncoding(headers);
        String fromCharset = getContentCharset(headers);

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


    private String getContentEncoding(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_ENCODING);

        if(values==null || values.isEmpty()) {
            return null;
        }

        return values.get(0);

    }

    String getContentCharset(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_TYPE);

        if(values==null || values.isEmpty()) {
            return null;
        }

        String contentType = values.get(0);
        String charset = extractCharsetOrNull(contentType);

        if(charset==null) {
            charset = Charset.defaultCharset().name();
        }

        return charset;
    }

    private String extractCharsetOrNull(String contentType) {
        if(StringUtils.isEmpty(contentType)) return null;
        if(contentType.matches("charset=.+")) {
            return contentType.replaceFirst("^.*;charset=(.+)$", "$1");
        }
        return null;
    }

}




