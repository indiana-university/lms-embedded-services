package edu.iu.uits.lms.canvas.config;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import edu.iu.uits.lms.canvas.security.CanvasTokenAuthorizationInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Created by chmaurer on 6/14/17.
 */
@Configuration
@Slf4j
public class CanvasEnvironmentConfiguration {

    @Autowired
    CanvasConfiguration canvasConfiguration;

    @Bean(name = "CanvasRestTemplate")
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

        restTemplate.getInterceptors().add(new CanvasTokenAuthorizationInterceptor(canvasConfiguration.getToken()));
//        restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
//        restTemplate.setErrorHandler(new CanvasErrorHandler());
        return restTemplate;
    }

    @Bean(name = "restTemplateNoBuffer")
    public RestTemplate restTemplateNoBuffer() {
        RestTemplate restTemplate = new RestTemplate(new SimpleClientHttpRequestFactory());

        restTemplate.getInterceptors().add(new CanvasTokenAuthorizationInterceptor(canvasConfiguration.getToken()));
        return restTemplate;
    }

    /**
     * Creates a RestTemplate bean using HttpComponentsClientHttpRequestFactory with a CloseableHttpClient.
     * This RestTemplate is configured with a CanvasTokenAuthorizationInterceptor.
     * Unlike the default RestTemplate, this one can handle PATCH requests.
     *
     * @return a RestTemplate instance configured with HttpComponentsClientHttpRequestFactory and CanvasTokenAuthorizationInterceptor
     */
    @Bean(name = "RestTemplateHttpComponent")
    public RestTemplate restTemplateHttpComponent() {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient)));

        restTemplate.getInterceptors().add(new CanvasTokenAuthorizationInterceptor(canvasConfiguration.getToken()));
        return restTemplate;
    }

}
