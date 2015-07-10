package hj.wsProxy;

import net.bull.javamelody.MonitoringFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.jmx.config.EnableIntegrationMBeanExport;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.mapping.OutboundMessageMapper;
import org.springframework.integration.transformer.GenericTransformer;
import org.springframework.integration.transformer.Transformer;
import org.springframework.integration.ws.WebServiceHeaders;
import org.springframework.integration.xml.transformer.XsltPayloadTransformer;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.MessagingException;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;

import javax.servlet.Filter;
import javax.servlet.http.HttpSessionListener;
import javax.xml.soap.SOAPHeader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by heiko on 20.06.15.
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableIntegration
@EnableIntegrationMBeanExport(server = "mbeanServer")
@Configuration
@EnableWebMvc
public class Application extends SpringBootServletInitializer {

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
    public MBeanServerFactoryBean mbeanServer() {
        return new MBeanServerFactoryBean();
    }

    @Bean
    public HttpSessionListener javaMelodyListener(){
        return new net.bull.javamelody.SessionListener();
    }

    @Bean
    public Filter javaMelodyFilter(){
        return new net.bull.javamelody.MonitoringFilter();
    }

    @Bean
    HttpRequestHandlingMessagingGateway httpGateway(ApplicationContext context)  {
        HttpRequestHandlingMessagingGateway gateway = new HttpRequestHandlingMessagingGateway();
        gateway.setApplicationContext(context);
        RequestMapping mapping = new RequestMapping();
        mapping.setPathPatterns(wsInboundPath);
        gateway.setRequestMapping(mapping);
        gateway.setHeaderMapper(soapHeaderMapper());

        gateway.setMessageConverters(getXmlMessageConverter());

        return gateway;
    }

    private List<HttpMessageConverter<?>> getXmlMessageConverter() {
        List<HttpMessageConverter<?>> converters = new ArrayList<>();

        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converter.setWriteAcceptCharset(false);

        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.TEXT_XML);

        converter.setSupportedMediaTypes(mediaTypes);
        converters.add(converter);

        return converters;
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

        gateway.setErrorHandler(new DefaultResponseErrorHandler() {


            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });

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
                transform(new Transformer() {
                    @Override
                    public Message<?> transform(Message<?> message) {
                        System.out.println("MESSAGE: " + message.getPayload());
                        System.out.println("HEADERS:" + message.getHeaders());
                        return message;
                    }
                }).
                get();
    }

}
