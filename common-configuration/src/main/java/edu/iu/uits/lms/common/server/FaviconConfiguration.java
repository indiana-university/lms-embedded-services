package edu.iu.uits.lms.common.server;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import java.util.Collections;
import java.util.List;

/**
 * @since 6.0.3
 */
@Configuration
@Slf4j
public class FaviconConfiguration {

    @Value("${lms.favicon.enabled:false}")
    private boolean enabled;

    @Value("${lms.favicon.url:}")
    private String url;

    @Value("${lms.favicon.path:/favicon.ico}")
    private String path;

    @Autowired
    private GenericWebApplicationContext context;

    @Bean
    public FaviconControllerAdvice faviconControllerAdvice() {
        return new FaviconControllerAdvice(faviconProperties());
    }

//    @Bean(name = "FaviconProperties")
    private FaviconProperties faviconProperties() {
        String faviconUrl = path;
        FaviconProperties.TYPE type = FaviconProperties.TYPE.PATH;

        if (enabled && StringUtils.isNotBlank(url)) {
            faviconUrl = url;
            type = FaviconProperties.TYPE.URL;
            log.info("Enabling favicon via external URL: {}", url);
        }

        if (enabled && type.equals(FaviconProperties.TYPE.PATH)) {
            SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
            mapping.setOrder(Integer.MIN_VALUE);
            mapping.setUrlMap(Collections.singletonMap(path, faviconRequestHandler()));
            context.registerBean("FaviconHandler", SimpleUrlHandlerMapping.class, () -> mapping);
            log.info("Enabling favicon via path handler: {}", path);
        }

        return new FaviconProperties(enabled, faviconUrl, type);
    }

    private static final List<String> CLASSPATH_RESOURCE_LOCATIONS = List.of(
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/META-INF/resources/");

    @Bean(autowireCandidate = false)
    protected ResourceHttpRequestHandler faviconRequestHandler() {
        ResourceHttpRequestHandler requestHandler = new ResourceHttpRequestHandler();
        requestHandler.setLocationValues(CLASSPATH_RESOURCE_LOCATIONS);
        return requestHandler;
    }

}
