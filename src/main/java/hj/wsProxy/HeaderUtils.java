package hj.wsProxy;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by heiko on 02.08.15.
 */
public class HeaderUtils {

    private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    static ContentEncoding getContentEncoding(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_ENCODING);

        if(values==null || values.isEmpty()) {
            return null;
        }

        return ContentEncoding.forValue(values.get(0));

    }



    static Charset getContentCharset(MessageHeaders headers) {
        List<String> values  = (List<String>)headers.get(HttpHeaders.CONTENT_TYPE);

        if(values==null || values.isEmpty()) {
            return DEFAULT_CHARSET;
        }

        String contentType = values.get(0);
        return extractCharsetOrDefault(contentType);

    }

    static Charset extractCharsetOrDefault(String contentType) {
        if(StringUtils.isEmpty(contentType)) return DEFAULT_CHARSET;
        if(contentType.matches("charset=.+")) {
            return Charset.forName(contentType.replaceFirst("^.*;charset=(.+)$", "$1"));
        }
        return DEFAULT_CHARSET;
    }


}
