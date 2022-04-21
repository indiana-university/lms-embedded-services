package edu.iu.uits.lms.email.service;

/*-
 * #%L
 * lms-email-service
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.ess.security.Aws4AuthUtil;
import edu.iu.uits.lms.email.config.SignedEmailServiceConfig;
import edu.iu.uits.lms.email.model.sis.Message;
import edu.iu.uits.lms.email.model.sis.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Map;

@Slf4j
@Service
public class SignedEmailService {

   private static final String POST_MESSAGE_URI = "{url}/email/send";
   private static final UriTemplate POST_MESSAGE = new UriTemplate(POST_MESSAGE_URI);

   private static final String SERVICE = "email";

   @Autowired
   private SignedEmailServiceConfig sisConfiguration = null;

   @Autowired
   private RestTemplate sisRestTemplate = null;

   public Result postEmail(Message message) {
      URI uri = POST_MESSAGE.expand(sisConfiguration.getUrl());
      Result result = new Result();
      try {
         ObjectMapper om = new ObjectMapper();
         byte[] serializedMessage = om.writeValueAsBytes(message);

         Map<String, Iterable<String>> authHeaders = Aws4AuthUtil.getAuthHeaders(sisConfiguration.getApiToken(),
               sisConfiguration.getRegion(), SERVICE, HttpMethod.POST.name(), uri.toURL(), serializedMessage);

         HttpHeaders headers = new HttpHeaders();
         authHeaders.forEach((name, values) -> values.forEach(value -> headers.add(name, value)));
         headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

         HttpEntity<byte[]> requestEntity = new HttpEntity<>(serializedMessage, headers);

         HttpEntity<Result> response = sisRestTemplate.postForEntity(uri, requestEntity, Result.class);
         result = response.getBody();
      } catch (Exception e) {
         log.error("Failure posting message to SignedEmailService", e);
         result.setMessage(e.getMessage());
      }
      return result;

   }


}
