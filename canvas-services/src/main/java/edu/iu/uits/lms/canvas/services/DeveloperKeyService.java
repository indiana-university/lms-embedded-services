package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2023 Indiana University
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Service
@Slf4j
public class DeveloperKeyService extends SpringBaseService {

   private static final String CANVAS_BASE_URI = "{url}/api";

   // Note that this URI is different from most, as it goes through /api/lti instead of /api/v1
   private static final String TOOL_CONFIGURATION_URI = CANVAS_BASE_URI + "/lti/developer_keys/{id}/tool_configuration";
   private static final UriTemplate TOOL_CONFIGURATION_TEMPLATE = new UriTemplate(TOOL_CONFIGURATION_URI);

   private static final String ACCOUNT_BINDING_URI = CANVAS_BASE_URI + "/v1/accounts/{account_id}/developer_keys/{id}/developer_key_account_bindings";
   private static final UriTemplate ACCOUNT_BINDING_TEMPLATE = new UriTemplate(ACCOUNT_BINDING_URI);

   /**
    * Update the contents of a developer key on a server
    * @param canvasServer Server the call will be executed against
    * @param devKeyId Dev Key Id
    * @param jsonBody JSON body that will be used to update the dev key
    * @return The results
    */
   public String updateToolConfiguration(String canvasServer, String devKeyId, String jsonBody) {
      URI uri = TOOL_CONFIGURATION_TEMPLATE.expand(canvasServer, devKeyId);
      log.debug("{}", uri);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);
         ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
         log.debug("{}", responseEntity);

          return responseEntity.getBody();
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to PUT the developer key changes", hcee);
      }

      return null;
   }

   /**
    * Update the developer key account bindings on a server
    * @param canvasServer Server the call will be executed against
    * @param accountId Account id where the developer key lives
    * @param devKeyId Dev Key Id
    * @param on Flag indicating if it should be turned on or off
    * @return The results
    */
   public String updateAccountBindings(String canvasServer, String accountId, String devKeyId, boolean on) {
      URI uri = ACCOUNT_BINDING_TEMPLATE.expand(canvasServer, accountId, devKeyId);
      log.debug("{}", uri);

      MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add("developer_key_account_binding[workflow_state]", on ? "on" : "off");

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

         HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(multiValueMap, headers);

         ResponseEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
         log.debug("{}", responseEntity);

          return responseEntity.getBody();
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to PUT the developer key account binding changes", hcee);
      }

      return null;
   }
}
