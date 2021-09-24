package edu.iu.uits.lms.common.cors;

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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LmsCorsInterceptor extends HandlerInterceptorAdapter {

    private Map<String, LmsCorsInterceptorConfiguration> lmsCorsInterceptorConfigurationMap;

    /**
     *
     * @param matchUri The part of the request Uri that is used to match this interceptor to fire
     *               Example: "/adrx/rest/post"
     * @param allowedOrigin  * for everywhere or something like https://iu.instructure.com
     * @param methodList List of HttpMethods - Example: HttpMethod.GET, HttpMethod.POST,...
     * @param miscHeaderMap Optional (null is fine) Map of headers to add
     * @throws IllegalArgumentException
     */
    public LmsCorsInterceptor(String matchUri, String allowedOrigin, List<HttpMethod> methodList, Map<String, String> miscHeaderMap) throws IllegalArgumentException {
        super();

        if (matchUri == null || matchUri.trim().length() == 0 ||
                allowedOrigin == null || allowedOrigin.trim().length() == 0 || methodList == null) {
            throw new IllegalArgumentException();
        }

        synchronized (this) {
            if (lmsCorsInterceptorConfigurationMap == null) {
                this.lmsCorsInterceptorConfigurationMap = new HashMap<String, LmsCorsInterceptorConfiguration>();
            }

            LmsCorsInterceptorConfiguration lmsCorsInterceptorConfiguration = new LmsCorsInterceptorConfiguration();

            lmsCorsInterceptorConfiguration.setMatchUri(matchUri);

            List<String> methodListStrings = methodList.stream().map(HttpMethod::name).collect(Collectors.toList());

            lmsCorsInterceptorConfiguration.setMethodNameString(String.join(",", methodListStrings));

            if (miscHeaderMap == null) {
                 lmsCorsInterceptorConfiguration.setMiscHeaderMap(new HashMap<String, String>());
            }
            else {
                lmsCorsInterceptorConfiguration.setMiscHeaderMap(miscHeaderMap);
            }

            lmsCorsInterceptorConfiguration.setAllowedOrigin(allowedOrigin);

            lmsCorsInterceptorConfigurationMap.put(matchUri, lmsCorsInterceptorConfiguration);
        } // end synchronized

        log.info(this.getClass().getName() + " initialized for uri " + matchUri);
    }

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestedUri = request.getRequestURI();

        for (Map.Entry <String, LmsCorsInterceptorConfiguration> lmsCorsInterceptorConfigurationEntry: lmsCorsInterceptorConfigurationMap.entrySet()) {
            LmsCorsInterceptorConfiguration lmsCorsInterceptorConfiguration = lmsCorsInterceptorConfigurationEntry.getValue();

            if (requestedUri.contains(lmsCorsInterceptorConfiguration.getMatchUri())) {
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, lmsCorsInterceptorConfiguration.getAllowedOrigin());
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, lmsCorsInterceptorConfiguration.getMethodNameString());

                for (Map.Entry<String, String> miscHeaderEntry : lmsCorsInterceptorConfiguration.getMiscHeaderMap().entrySet()) {
                    response.addHeader(miscHeaderEntry.getKey(), miscHeaderEntry.getValue());
                }

                break;
            }
        }

        return true;
    }

}
