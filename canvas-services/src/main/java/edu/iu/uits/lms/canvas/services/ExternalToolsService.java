package edu.iu.uits.lms.canvas.services;

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

import edu.iu.uits.lms.canvas.model.ExternalTool;
import edu.iu.uits.lms.canvas.model.LtiSettings;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class ExternalToolsService extends SpringBaseService {
   private static final String CANVAS_BASE_URI = "{url}/api/v1";
   private static final String ACCOUNTS_BASE_URI = CANVAS_BASE_URI + "/accounts";
   private static final String ACCOUNT_URI = ACCOUNTS_BASE_URI + "/{id}";
   private static final String EXTERNAL_TOOLS_URI = ACCOUNT_URI + "/external_tools/{toolId}";
   private static final String EXTERNAL_TOOLS_VIA_COURSES_URI = "{url}/courses/{id}/external_tools";
   private static final UriTemplate EXTERNAL_TOOLS_TEMPLATE = new UriTemplate(EXTERNAL_TOOLS_URI);
   private static final UriTemplate EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE = new UriTemplate(EXTERNAL_TOOLS_VIA_COURSES_URI);

   /**
    *
    * @param canvasServer
    * @param accountId
    * @param toolId
    * @return
    */
   public ExternalTool getExternalTool(String canvasServer, String accountId, String toolId) {
      URI uri = EXTERNAL_TOOLS_TEMPLATE.expand(canvasServer, accountId, toolId);
      log.debug("{}", uri);
      try {
         ResponseEntity<ExternalTool> response = restTemplate.getForEntity(uri, ExternalTool.class);
         return response.getBody();
      } catch (HttpClientErrorException hcee) {
         log.error("Unable to GET the external tool from the uri + " + uri, hcee);
      }
      return null;
   }

   /**
    *
    * @param courseId
    * @return
    */
   public List<ExternalTool> getExternalTools(@NonNull String courseId) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

      builder.queryParam("per_page", "100");

      return doGet(builder.build().toUri(), ExternalTool[].class);
   }

   /**
    *
    * @param courseId
    * @param toolId
    * @return
    */
   public ExternalTool deleteExternalTool(String courseId, String toolId) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.path("/" + toolId);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

         HttpEntity<ExternalTool> externalToolResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, ExternalTool.class);
         log.debug("{}", externalToolResponse);

         ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) externalToolResponse;

         if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request to Canvas was not successful. Response code: "
                    + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                    + ", body: " + responseEntity.getBody());
         }

         if (externalToolResponse != null) {
            log.info("Deleted ExternalTool toolId " + toolId + " from Canvas courseId: " + courseId);
            return externalToolResponse.getBody();
         }
      } catch (HttpClientErrorException hcee) {
         log.error("Error deleting external tool", hcee);
         throw new RuntimeException("Error deleting external tool", hcee);
      }

      return null;
   }

   /**
    *
    * @param courseId
    * @param toolId
    * @param name
    * @param url
    * @param isNewTab
    * @param isCourseNav
    * @return
    */
   public ExternalTool editExternalTool(String courseId, String toolId, String name, String url, boolean isNewTab, boolean isCourseNav) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.path("/" + toolId);
      builder.queryParam("custom_fields[url]", url);

      // if link is internal, set field to not open in a new tab
      builder.queryParam("custom_fields[new_tab]", isNewTab ? 1 : 0);

      // if adding to the course nav, set property to true. Otherwise set to null, since that's what Canvas seems to prefer
      if (isCourseNav) {
         builder.queryParam("course_navigation[enabled]", isCourseNav);
      } else {
         builder.queryParam("course_navigation", (Object) null);
      }

      MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
      multiValueMap.add("name", name);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
         HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(multiValueMap, headers);

         HttpEntity<ExternalTool> externalToolResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, ExternalTool.class);
         log.debug("{}", externalToolResponse);

         ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) externalToolResponse;

         if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request to Canvas was not successful. Response code: "
                    + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                    + ", body: " + responseEntity.getBody());
         }

         if (externalToolResponse != null) {
            log.info("Edited ExternalTool toolId " + toolId + " from Canvas courseId: " + courseId);
            return externalToolResponse.getBody();
         }
      } catch (HttpClientErrorException hcee) {
         log.error("Error updating external tool", hcee);
         throw new RuntimeException("Error updating external tool", hcee);
      }

      return null;
   }

   /**
    *
    * @param canvasServer
    * @param accountId
    * @param toolId
    * @param ltiSettings
    * @return
    */
   public ExternalTool updateExternalTool(String canvasServer, String accountId, String toolId, LtiSettings ltiSettings) {
      URI uri = EXTERNAL_TOOLS_TEMPLATE.expand(canvasServer, accountId, toolId);
      log.debug("{}", uri);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_JSON);

         HttpEntity<LtiSettings> requestEntity = new HttpEntity<>(ltiSettings, headers);
         HttpEntity<ExternalTool> responseEntity = this.restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, ExternalTool.class);
         log.debug("{}", responseEntity);

         if (responseEntity != null) {
            return responseEntity.getBody();
         }
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to PUT the external tool changes", hcee);
      }

      return null;
   }

   /**
    * Create a new external tool in the given course based on the given client id
    * @param courseId CourseId where the new tool should be created
    * @param clientId ClientId
    * @return The newly created ExternalTool
    */
   public ExternalTool createExternalTool(String courseId, String clientId) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
      log.debug("{}", uri);

      try {
         UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
         builder.queryParam("client_id", clientId);

         HttpEntity<ExternalTool> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, null, ExternalTool.class);
         log.debug("{}", responseEntity);

         if (responseEntity != null) {
            return responseEntity.getBody();
         }
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to POST the new external tool", hcee);
      }

      return null;
   }

   /**
    * Update an external tool with the given properties
    * @param courseId CourseId containing the tool placement
    * @param toolId External tool id
    * @param properties Map of properties that will be updated in the tool
    * @return The updated ExternalTool
    */
   public ExternalTool updateExternalToolProperties(String courseId, String toolId, MultiValueMap<String, String> properties) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.path("/" + toolId);
      URI builtUri = builder.build().toUri();
      log.debug("{}", builtUri);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);

         HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(properties, headers);
         HttpEntity<ExternalTool> responseEntity = this.restTemplate.exchange(builtUri, HttpMethod.PUT, requestEntity, ExternalTool.class);
         log.debug("{}", responseEntity);

         if (responseEntity != null) {
            return responseEntity.getBody();
         }
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to PUT the external tool changes", hcee);
      }

      return null;
   }
}
