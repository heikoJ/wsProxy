package hj.wsProxy;

import hj.wsProxy.decompressor.Decompressor;
import hj.wsProxy.profiler.Profiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.nio.charset.Charset;
import java.util.*;

import static hj.wsProxy.ContentEncoding.*;

/**
 * Created by heiko on 26.07.15.
 */
public class CompressionTransformerTest {



    private static final String UNCOMPRESSED_STRING = "This is an uncompressed String dshfdsfhsdf dsfhsdjfhsdkjfhdskjfhdjksfhjdskhf kjdshfkjsdhfkdshf" +
            "jshdfkjsdh fkjdshf dskjfh sdkjfhsdjkf hdskjfh dsjkf hkjsdhf" +
            "sdfkjlh sdkjfh sdkjfh dsjkfh sdkjfhdsjkfhdsjkfhdsjkfhsdjkfhsjkdf" +
            "sdlafh sdkjfhsdjkfhdsjkfhsdjkfhdsjkafhsdajfkhsdlfjkhsdkfjhdskjfhdsjkf" +
            "sdakjf sdkjafjksdhfjksd fhsdjkafh sdkjfhds jkafhsdkjaf hsdkjf hsdjkfh dsjkf" +
            "sflksdjfalksd fljdshfjksdahfkjsd hfakjsdah fkjdsfh sdjkfhdjkshjkdahf jkdshfjkdshfkjsdhfkjsdhfkjsdf" +
            "skjfh skjdfhsdjkfhsdkjfh dsjkhfjkashdf ksjdahf ksdjhfkjsdahf jsdkfhsdkjfh dsjkfh sdkjfhsdjkf hsdjkafh sdjkafh sdkajf" +
            "sdk fjhsdakfjhsdkjf hsdkjfh sdjkfhsdajkf hsdjkafh sdjkf hsdjkfh sdjkfhsjkdf hsdkjafhsjdkaf hsjdkafhskadfhdajksf hsjkdafh jksdaf " +
            "sa kljhsadkjf hsdjkf hsdkjfhsdjakf hdsajkf sadkfhasjkdf hsakjf hsdajkf hsdjkafh sdakflhaskfhsaklfjhsalkfhaskjflhladsf" +
            "sdk ajlfsdjkaf hsdkjfh dskjaf hsajk hasdjk fhsadfkhsdajfkhsadfjklhsadfjksahdfjk sahfjks hfdsjkf adsjkf hsadfhjksdafh jkdshf " +
            "lskdah sdkaj hfsakjdhf dsakfjhdsajkf sadfjklh sdajkf hsdjkaf hsdakflhsdajkf hsdajfhasdjkfh sdajkf haskdlfhajksdf hjksdahf sd";


    private CompressionTransformer transformer;

    @Before
    public void setUp() throws Exception {
        transformer = new CompressionTransformer();
        Decompressor.forEncoding(GZIP);
    }

    @Test
    public void testDecompression() throws Exception {

        byte[] payload =  CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        Message<byte[]> fromMessage =
                        MessageBuilder.
                        withPayload(payload).
                        setHeader(HttpHeaders.CONTENT_TYPE, Collections.singletonList("text/xml;charset=UTF-8")).
                        setHeader(HttpHeaders.CONTENT_ENCODING, Collections.singletonList("gzip")).
                        build();

        Profiler profiler = Profiler.
                forPrintStream(System.out).
                withSubject("decompression").
                start();

        Message<String> toMessage = (Message<String>) transformer.doTransform(fromMessage);

        profiler.endAndPrint();

        Assert.assertEquals(toMessage.getPayload(),UNCOMPRESSED_STRING);
        Assert.assertFalse(toMessage.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/xml;charset=UTF-8",((List<String>)toMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0));

    }



    @Test
    public void testLArgeDecompression() throws Exception {

        String largeString = makeLargeRandomString();

        System.out.println("size: " + largeString.getBytes("UTF-8").length / 1024 / 1024 + "MB");

        Profiler profiler = Profiler.
                forPrintStream(System.out).
                withSubject("compressLargeString").
                start();
        byte[] payload =  CompressionUtils.gzipString(largeString);

        profiler.endAndPrint();

        Message<byte[]> fromMessage =
                MessageBuilder.
                        withPayload(payload).
                        setHeader(HttpHeaders.CONTENT_TYPE, Collections.singletonList("text/xml;charset=UTF-8")).
                        setHeader(HttpHeaders.CONTENT_ENCODING, Collections.singletonList("gzip")).
                        build();

        profiler = Profiler.
                forPrintStream(System.out).
                withSubject("decompression").
                start();
        Message<String> toMessage = (Message<String>) transformer.doTransform(fromMessage);

        profiler.endAndPrint();


        Assert.assertEquals(toMessage.getPayload(),largeString);
        Assert.assertFalse(toMessage.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/xml;charset=UTF-8",((List<String>)toMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE)).get(0));

    }


    @Test
      public void testExtractCharset() throws Exception {

        Map<String,Object> headerMap =Collections.<String,Object>singletonMap(HttpHeaders.CONTENT_TYPE,Collections.singletonList("text/xml;charset=UTF-8"));
        MessageHeaders headers = new MessageHeaders(headerMap);

        Charset charset = HeaderUtils.getContentCharset(headers);

        Assert.assertEquals(Charset.forName("UTF-8"), charset);


    }

    @Test
    public void testExtractCharset_missing() throws Exception {

        Map<String,Object> headerMap =Collections.<String,Object>singletonMap(HttpHeaders.CONTENT_TYPE,Collections.singletonList("text/xml"));
        MessageHeaders headers = new MessageHeaders(headerMap);

        Charset charset = HeaderUtils.getContentCharset(headers);


        Assert.assertEquals(Charset.defaultCharset(), charset);

    }

    @Test
    public void testExtractCharset_missing2() throws Exception {

        Map<String,Object> headerMap =Collections.<String,Object>singletonMap(HttpHeaders.CONTENT_TYPE,Collections.singletonList("text/xml;charset="));
        MessageHeaders headers = new MessageHeaders(headerMap);

        Charset charset = HeaderUtils.getContentCharset(headers);


        Assert.assertEquals(Charset.defaultCharset(), charset);

    }

    @Test
    public void testExtractCharset_null() throws Exception {

        Map<String,Object> headerMap =Collections.<String,Object>singletonMap(HttpHeaders.CONTENT_TYPE,Collections.singletonList(null));
        MessageHeaders headers = new MessageHeaders(headerMap);

        Charset charset = HeaderUtils.getContentCharset(headers);


        Assert.assertEquals(Charset.defaultCharset(), charset);

    }




    private String makeLargeRandomString() {
        Profiler profiler =
                Profiler.forPrintStream(System.out).
                        withSubject("makeLargeRandomString").
                        start();
        StringBuilder sb = new StringBuilder();
        for(int i=0;i< 700000;i++) {
            sb.append(UUID.randomUUID().toString());
        }
        String result = sb.toString();

        profiler.endAndPrint();

        return result;

    }

}