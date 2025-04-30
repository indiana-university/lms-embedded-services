package edu.iu.uits.lms.canvas.utils.resttemplate;

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

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class LoggingRequestInterceptor implements ClientHttpRequestInterceptor {

    private static final int REQUEST_LOGGING_LIMIT = 1000;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        traceRequest(request, body);
        ClientHttpResponse response = execution.execute(request, body);
        traceResponse(response);
        return response;
    }

    private void traceRequest(HttpRequest request, byte[] body) throws IOException {
        final String AUTHORIZATION_HEADER_NAME = "AUTHORIZATION";

        log.debug("*** Request Start ***");
        log.debug("URI         : " + request.getURI());
        log.debug("Method      : " + request.getMethod());
        log.debug("Headers     : ");

        HttpHeaders httpHeaders = request.getHeaders();
        Set<Map.Entry<String, List<String>>> headers = httpHeaders.entrySet();

        for(Map.Entry<String, List<String>> header : headers) {
            String headerName = header.getKey();

            for(String headerValue : header.getValue()) {
                if (headerName.toLowerCase().equalsIgnoreCase(AUTHORIZATION_HEADER_NAME)) {
                    headerValue = "*****";
                }

                log.debug("  " + headerName + " : " + headerValue);
            }
        }

        // If we have a large request body (like one that includes an attachment), we can get an OOM exception trying
        // to write out the body so truncate if the body is large
        byte[] requestBody;
        if (body.length > REQUEST_LOGGING_LIMIT) {
            requestBody = Arrays.copyOfRange(body,0, REQUEST_LOGGING_LIMIT);
            log.debug("***** THIS REQUEST LOG HAS BEEN TRUNCATED DUE TO SIZE LIMITATIONS *****");
        } else {
            requestBody = body;
        }

        log.debug("Request body: " + new String(requestBody, StandardCharsets.UTF_8));

        log.debug("*** Request End ***");
    }

    private void traceResponse(ClientHttpResponse response) throws IOException {
        StringBuilder inputStringBuilder = new StringBuilder();
        //There's a strange bug that sometimes presents itself as an IOException when getting the response body.
        //If you do a getStatusCode first, it "works"
        // See https://stackoverflow.com/questions/30356491/resttemplate-clienthttpresponse-getbody-throws-i-o-error
        HttpStatusCode status = response.getStatusCode();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        String line = bufferedReader.readLine();
        while (line != null) {
            inputStringBuilder.append(line);
            inputStringBuilder.append('\n');
            line = bufferedReader.readLine();
        }
        log.debug("============================response begin==========================================");
        log.debug("Status code  : " + status);
        log.debug("Status text  : " + response.getStatusText());
        log.debug("Headers      : " + response.getHeaders());
        log.debug("Response body: " + inputStringBuilder.toString());
        log.debug("=======================response end=================================================");
    }

}
