package edu.iu.uits.lms.common.server;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

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
