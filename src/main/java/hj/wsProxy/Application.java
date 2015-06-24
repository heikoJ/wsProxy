package hj.wsProxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.http.inbound.HttpRequestHandlingMessagingGateway;
import org.springframework.integration.http.inbound.RequestMapping;
import org.springframework.integration.http.outbound.HttpRequestExecutingMessageHandler;
import org.springframework.integration.mapping.InboundMessageMapper;
import org.springframework.integration.support.AbstractIntegrationMessageBuilder;
import org.springframework.integration.support.MessageBuilderFactory;
import org.springframework.integration.ws.DefaultSoapHeaderMapper;
import org.springframework.integration.ws.SimpleWebServiceInboundGateway;
import org.springframework.integration.ws.SimpleWebServiceOutboundGateway;
import org.springframework.integration.ws.WebServiceHeaders;
import org.springframework.integration.xml.DefaultXmlPayloadConverter;
import org.springframework.integration.xml.xpath.XPathEvaluationType;
import org.springframework.messaging.Message;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.ws.InvalidXmlException;
import org.springframework.ws.WebServiceMessage;
import org.springframework.ws.WebServiceMessageFactory;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.validation.XmlValidator;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

import javax.xml.transform.Source;
import javax.xml.xpath.XPathConstants;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.*;

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

        return gateway;
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
        headers.put(WebServiceHeaders.SOAP_ACTION,
                "http://www.w3schools.com/webservices/CelsiusToFahrenheit");
        headers.put(HttpHeaders.CONTENT_TYPE,"text/xml;charset=UTF-8");

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
