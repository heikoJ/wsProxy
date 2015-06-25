package hj.wsProxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.ws.WebServiceHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

import javax.xml.soap.SOAPHeader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by heiko on 20.06.15.
 */
@SpringBootApplication
@IntegrationComponentScan
@Configuration
@EnableWs
public class Application extends WsConfigurerAdapter {

    public static void main(String[] args) {
        ConfigurableApplicationContext ctx = SpringApplication.run(Application.class, args);
    }


    @Value("${xsltPath}")
    private String xsltPath;

    @Value("${wsInboundPath}")
    private String wsInboundPath;

    @Value("${wsOutboundAddress}")
    private String wsOutboundAddress;


    @Bean
    HttpRequestHandlingMessagingGateway httpGateway(ApplicationContext context)  {
        HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway();
        gateway.setApplicationContext(context);
        RequestMapping mapping = new RequestMapping();
        mapping.setPathPatterns(wsInboundPath);
        gateway.setRequestMapping(mapping);
        gateway.setHeaderMapper(soapHeaderMapper());

        return gateway;
    }


    public HeaderMapper<HttpHeaders> soapHeaderMapper() {
        return new DefaultHttpHeaderMapper() {
            @Override
            public Map<String, Object> toHeaders(HttpHeaders source) {
                Map<String,Object> map = super.toHeaders(source);
                map.put(WebServiceHeaders.SOAP_ACTION,source.get("SOAPAction").get(0));
                return map;
            }
        };
    }




    @Bean
    HttpRequestExecutingMessageHandler httpOutboundGateway(ApplicationContext context){
        HttpRequestExecutingMessageHandler gateway = new HttpRequestExecutingMessageHandler(wsOutboundAddress);
        gateway.setApplicationContext(context);
        gateway.setHttpMethod(HttpMethod.POST);
        gateway.setExpectedResponseType(String.class);

        return gateway;
    }


    private Map<String,Object> headers() {
        Map<String,Object> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, "text/xml;charset=UTF-8");

        return headers;

    }



    @Bean
    public IntegrationFlow convert(HttpRequestHandlingMessagingGateway inbound, HttpRequestExecutingMessageHandler outbound) {
        return IntegrationFlows.
                from(inbound).
                enrichHeaders(headers()).
                handle(outbound).
                transform(Transformers.xslt(new ClassPathResource(xsltPath))).
                get();
    }

}
