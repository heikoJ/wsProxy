package hj.wsProxy;

import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * Created by heiko on 02.08.15.
 */
@Component
public class ByteArrayToStringTransformer extends AbstractTransformer {

    @Override
    protected Object doTransform(Message<?> message) throws Exception {

        return MessageBuilder.
                withPayload(new String((byte[]) message.getPayload(), "UTF-8")).
                copyHeaders(message.getHeaders()).
                build();
    }

}
