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
//        RestTemplate restTemplate = new RestTemplate();

        RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new HttpComponentsClientHttpRequestFactory()));

        restTemplate.getInterceptors().add(new CanvasTokenAuthorizationInterceptor(canvasConfiguration.getToken()));
//        restTemplate.getInterceptors().add(new LoggingRequestInterceptor());

        //This RequestFactory allows us to have get requests that contain a body
//        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestWithBodyFactory());

//        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
//        list.add(new MappingJackson2HttpMessageConverter());
//        restTemplate.setMessageConverters(list);

//        restTemplate.setErrorHandler(new CanvasErrorHandler());
        return restTemplate;
    }

    @Bean(name = "restTemplateNoBuffer")
    public RestTemplate restTemplateNoBuffer() {

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        RestTemplate restTemplate = new RestTemplate(requestFactory);

        restTemplate.getInterceptors().add(new CanvasTokenAuthorizationInterceptor(canvasConfiguration.getToken()));

        return restTemplate;
    }

//    private static final class HttpComponentsClientHttpRequestWithBodyFactory extends HttpComponentsClientHttpRequestFactory {
//        @Override
//        protected ClassicHttpRequest createHttpUriRequest(HttpMethod httpMethod, URI uri) {
//            if (httpMethod == HttpMethod.GET) {
//                return new HttpGetRequestWithEntity(uri);
//            }
//            return super.createHttpUriRequest(httpMethod, uri);
//        }
//    }

//    private static final class HttpGetRequestWithEntity extends HttpEntityEnclosingRequestBase implements ClassicHttpRequest {
//        public HttpGetRequestWithEntity(final URI uri) {
//            super.setURI(uri);
//        }
//
//        @Override
//        public String getMethod() {
//            return HttpMethod.GET.name();
//        }
//    }
}
