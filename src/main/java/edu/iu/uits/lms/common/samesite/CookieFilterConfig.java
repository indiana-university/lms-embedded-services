package edu.iu.uits.lms.common.samesite;

/*-
 * #%L
 * lms-canvas-common-configuration
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

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public class CookieFilterConfig implements ImportAware {

    private String uaPattern = "(iPhone; CPU iPhone OS 1[0-2]|iPad; CPU OS 1[0-2]|iPod touch; CPU iPhone OS 1[0-2]|Macintosh; Intel Mac OS X.*Version/1[0-2].*Safari|Macintosh;.*Mac OS X 10_14.* AppleWebKit.*Version/1[0-3].*Safari)";

    private String[] ignoredRequestPatterns = {"/actuator/info"};


    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        Map<String, Object> attributeMap = importMetadata
              .getAnnotationAttributes(EnableCookieFilter.class.getName());
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributeMap);
        boolean overwrite = attributes.getBoolean("overrideIgnoredRequestPatterns");
        String[] ignoredRequestPatternsFromAnnotation = attributes.getStringArray("ignoredRequestPatterns");
        if (ignoredRequestPatternsFromAnnotation.length > 0) {
            if (overwrite) {
                this.ignoredRequestPatterns = ignoredRequestPatternsFromAnnotation;
            } else {
                this.ignoredRequestPatterns = Stream.concat(Arrays.stream(ignoredRequestPatterns), Arrays.stream(ignoredRequestPatternsFromAnnotation))
                      .toArray(String[]::new);
            }
        }
        String userAgentRegex = attributes.getString("ignoredUserAgentRegex");
        if (!StringUtils.isEmpty(userAgentRegex)) {
            this.uaPattern = userAgentRegex;
        }
    }

    @Bean
    public FilterRegistrationBean<CookieFilter> cookieFilter(){
        FilterRegistrationBean<CookieFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CookieFilter(uaPattern, ignoredRequestPatterns));
        //Our filter needs to be loaded early so that our response wrapper gets used for subsequent filters in the chain
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registrationBean;
    }
}
