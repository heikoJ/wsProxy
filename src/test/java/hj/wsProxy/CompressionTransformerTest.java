package hj.wsProxy;

import hj.wsProxy.decompressor.Decompressor;
import hj.wsProxy.profiler.Profiler;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import java.util.UUID;

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
        Decompressor.forEncoding("gzip");
    }

    @Test
    public void testDecompression() throws Exception {

        byte[] payload =  CompressionUtils.gzipString(UNCOMPRESSED_STRING);

        Message<byte[]> fromMessage =
                        MessageBuilder.
                        withPayload(payload).
                        setHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8").
                        setHeader(HttpHeaders.CONTENT_ENCODING,"gzip").
                        build();

        Profiler profiler = Profiler.
                printStreamProfiler(System.out,"decompression").
                start();

        Message<String> toMessage = (Message<String>) transformer.doTransform(fromMessage);

        profiler.endAndPrint();

        Assert.assertEquals(toMessage.getPayload(),UNCOMPRESSED_STRING);
        Assert.assertFalse(toMessage.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/xml;charset=UTF-8",toMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE));

    }



    @Test
    public void testLArgeDecompression() throws Exception {

        String largeString = makeLargeRandomString();

        System.out.println("size: " + largeString.getBytes("UTF-8").length / 1024 / 1024 + "MB");

        byte[] payload =  CompressionUtils.gzipString(largeString);

        Message<byte[]> fromMessage =
                MessageBuilder.
                        withPayload(payload).
                        setHeader(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8").
                        setHeader(HttpHeaders.CONTENT_ENCODING,"gzip").
                        build();

        Profiler profiler = Profiler.
                printStreamProfiler(System.out,"decompression").
                start();
        Message<String> toMessage = (Message<String>) transformer.doTransform(fromMessage);

        profiler.endAndPrint();


        Assert.assertEquals(toMessage.getPayload(),largeString);
        Assert.assertFalse(toMessage.getHeaders().containsKey(HttpHeaders.CONTENT_ENCODING));
        Assert.assertEquals("text/xml;charset=UTF-8",toMessage.getHeaders().get(HttpHeaders.CONTENT_TYPE));

    }

    private String makeLargeRandomString() {
        StringBuilder sb = new StringBuilder();
        for(int i=0;i< 1000000;i++) {
            sb.append(UUID.randomUUID().toString());
        }

        return sb.toString();

    }

}