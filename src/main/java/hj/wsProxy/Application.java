package hj.wsProxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.http.support.DefaultHttpHeaderMapper;
import org.springframework.integration.jmx.config.EnableIntegrationMBeanExport;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.integration.transformer.AbstractTransformer;
import org.springframework.integration.ws.WebServiceHeaders;
import org.springframework.jmx.support.MBeanServerFactoryBean;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import javax.servlet.Filter;
import javax.servlet.http.HttpSessionListener;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.DeflaterInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by heiko on 20.06.15.
 */
@SpringBootApplication
@IntegrationComponentScan
@EnableIntegration
@ImportResource("classpath:net/bull/javamelody/monitoring-spring-aspectj.xml")
@Configuration
@EnableWebMvc
@EnableAspectJAutoProxy
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {
        //Set saxon as transformer.
        System.setProperty("javax.xml.transform.TransformerFactory",
                "net.sf.saxon.TransformerFactoryImpl");
        SpringApplication.run(Application.class, args);
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

        List<HttpMessageConverter<?>> converters = new ArrayList<>();
        converters.add(new ByteArrayHttpMessageConverter());
        converters.add(getXmlMessageConverter());

        gateway.setMessageConverters(converters);

        gateway.setRequestPayloadType(byte[].class);




        return gateway;
    }

    private HttpMessageConverter<?> getXmlMessageConverter() {
        StringHttpMessageConverter converter = new StringHttpMessageConverter(Charset.forName("UTF-8"));
        converter.setWriteAcceptCharset(false);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.TEXT_XML));

        return converter;
    }


    public HeaderMapper<HttpHeaders> soapHeaderMapper() {
        return new DefaultHttpHeaderMapper() {
            @Override
            public Map<String, Object> toHeaders(HttpHeaders source) {
                Map<String,Object> map = super.toHeaders(source);
                map.put(WebServiceHeaders.SOAP_ACTION,source.get("SOAPAction").get(0));



                map.put(HttpHeaders.CONTENT_TYPE, source.get(HttpHeaders.CONTENT_TYPE));
                map.put(HttpHeaders.CONTENT_ENCODING, source.get(HttpHeaders.CONTENT_ENCODING));
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
    CompressionTransformer compressionTransformer() {
        return new CompressionTransformer();
    }


    @Bean
    public IntegrationFlow convert(HttpRequestHandlingMessagingGateway inbound, HttpRequestExecutingMessageHandler outbound, CompressionTransformer compressionTransformer) {





        return IntegrationFlows.
                from(inbound).
                transform(compressionTransformer).
                enrichHeaders(headers()).
                handle(outbound).
                transform(Transformers.xslt(new ClassPathResource(xsltPath))).

                get();
    }

}
