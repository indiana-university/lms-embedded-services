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
   private static final String EXTERNAL_TOOLS_VIA_COURSES_URI = "/courses/{id}/external_tools";
   private static final String EXTERNAL_TOOL_VIA_COURSES_URI = EXTERNAL_TOOLS_VIA_COURSES_URI + "/{toolId}";
   private static final String EXTERNAL_TOOLS_VIA_ACCOUNTS_URI = "{url}/accounts/{id}/external_tools";
   private static final UriTemplate EXTERNAL_TOOLS_TEMPLATE = new UriTemplate(EXTERNAL_TOOLS_URI);
   private static final UriTemplate EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE = new UriTemplate("{url}" + EXTERNAL_TOOLS_VIA_COURSES_URI);
   private static final UriTemplate EXTERNAL_TOOLS_VIA_ACCOUNTS_URI_TEMPLATE = new UriTemplate(EXTERNAL_TOOLS_VIA_ACCOUNTS_URI);
   private static final UriTemplate EXTERNAL_TOOL_VIA_COURSES_URI_TEMPLATE = new UriTemplate(CANVAS_BASE_URI + EXTERNAL_TOOL_VIA_COURSES_URI);

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
    * Get all external tools for a course
    * @param courseId Course id
    * @return List of ExternalTools
    */
   public List<ExternalTool> getExternalTools(@NonNull String courseId) {
      return getExternalTools(courseId, null, null);
   }

   /**
    * Get all external tools for a course
    * @param courseId Course id
    * @param searchTerm Search term that will be used as a partial match for the tool name
    * @param placement Placement type that will be used as a filter for the results
    * @return List of ExternalTools matching the provided criteria
    */
   public List<ExternalTool> getExternalTools(@NonNull String courseId, String searchTerm, String placement) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

      if (searchTerm != null) {
         builder.queryParam("search_term", searchTerm);
      }

      if (placement != null) {
         builder.queryParam("placement", placement);
      }

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
    * Delete an external tool from an account
    * @param accountId Account where the tool is placed
    * @param toolId External tool id to delete
    * @return Tool that was deleted, or null of none was deleted
    */
   public ExternalTool deleteExternalToolFromAccount(String accountId, String toolId) {
      return deleteExternalToolFromAccount(canvasConfiguration.getBaseApiUrl(), accountId, toolId);
   }

   /**
    * Delete an external tool from an account
    * @param serverUrl Server where the api should be called from
    * @param accountId Account where the tool is placed
    * @param toolId External tool id to delete
    * @return Tool that was deleted, or null of none was deleted
    */
   public ExternalTool deleteExternalToolFromAccount(String serverUrl, String accountId, String toolId) {
      URI uri = EXTERNAL_TOOLS_VIA_ACCOUNTS_URI_TEMPLATE.expand(serverUrl, accountId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.path("/" + toolId);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

         log.debug("Uri to DELETE: {}", builder.build().toUri());
         HttpEntity<ExternalTool> externalToolResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, ExternalTool.class);
         log.debug("{}", externalToolResponse);

         ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) externalToolResponse;

         if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request to Canvas was not successful. Response code: "
                    + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                    + ", body: " + responseEntity.getBody());
         }

         if (externalToolResponse != null) {
            log.info("Deleted ExternalTool toolId " + toolId + " from Canvas accountId: " + accountId);
            return externalToolResponse.getBody();
         }
      } catch (HttpClientErrorException hcee) {
         if (HttpStatus.NOT_FOUND.equals(hcee.getStatusCode())) {
            log.warn("External tool not found for accountId {} and toolId {} on {}", accountId, toolId, serverUrl);
         } else {
            log.error("Error deleting external tool", hcee);
            throw new RuntimeException("Error deleting external tool", hcee);
         }
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
    * Update an external tool in an account
    * @param canvasServer Url of the canvas server
    * @param accountId Account id where the tool is placed
    * @param toolId Tool id to update
    * @param ltiSettings Settings to update
    * @return ExternalTool result
    */
   public ExternalTool updateExternalToolForAccount(String canvasServer, String accountId, String toolId, LtiSettings ltiSettings) {
      return updateExternalTool(canvasServer, accountId, toolId, ltiSettings, EXTERNAL_TOOLS_TEMPLATE);
   }

   /**
    * Update an external tool in a course
    * @param canvasServer Url of the canvas server
    * @param courseId Course id where the tool is placed
    * @param toolId Tool id to update
    * @param ltiSettings Settings to update
    * @return ExternalTool result
    */
   public ExternalTool updateExternalToolForCourse(String canvasServer, String courseId, String toolId, LtiSettings ltiSettings) {
      return updateExternalTool(canvasServer, courseId, toolId, ltiSettings, EXTERNAL_TOOL_VIA_COURSES_URI_TEMPLATE);
   }

   /**
    * Update an external tool in a context
    * @param canvasServer Url of the canvas server
    * @param contextId Context id (account or course) where the tool is placed
    * @param toolId Tool id to update
    * @param ltiSettings Settings to update
    * @param uriTemplate UriTemplate used to make the rest call to canvas
    * @return ExternalTool result
    */
   private ExternalTool updateExternalTool(String canvasServer, String contextId, String toolId, LtiSettings ltiSettings, UriTemplate uriTemplate) {
      URI uri = uriTemplate.expand(canvasServer, contextId, toolId);
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
    * Create an external tool (1.3) in a course
    * @param courseId Course id
    * @param clientId Client id of the LTI developer key
    * @return The created ExternalTool
    */
   public ExternalTool createExternalToolForCourse(String courseId, String clientId) {
      LtiSettings ltiSettings = new LtiSettings();
      ltiSettings.setClientId(clientId);
      return createExternalToolForCourse(courseId, ltiSettings);

   }

   /**
    * Create an external tool (1.1 or 1.3) in a course
    * @param courseId Course id
    * @param ltiSettings LtiSettings
    * @return The created ExternalTool
    */
   public ExternalTool createExternalToolForCourse(String courseId, LtiSettings ltiSettings) {
      URI uri = EXTERNAL_TOOLS_VIA_COURSES_URI_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

      try {
         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED);
         HttpEntity<LtiSettings> requestEntity = new HttpEntity<>(ltiSettings, headers);

         ResponseEntity<ExternalTool> responseEntity = this.restTemplate.exchange(uri, HttpMethod.POST, requestEntity, ExternalTool.class);
         log.debug("{}", responseEntity);

         if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Request to Canvas was not successful. Response code: "
                    + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                    + ", body: " + responseEntity.getBody());
         }

         if (responseEntity != null) {
            log.info("Created ExternalTool for Canvas courseId: " + courseId);
            return responseEntity.getBody();
         }
      } catch (HttpClientErrorException hcee) {
         log.error("Error creating external tool", hcee);
         throw new RuntimeException("Error creating external tool", hcee);
      }

      return null;
   }

   /**
    * Get the first matching external tool
    * @param courseId Course id
    * @param toolName Tool name to look up
    * @param placement Placement type (optional)
    * @return First matching ExternalTool (or null, if none found)
    */
   public ExternalTool getExternalToolByName(String courseId, String toolName, String placement) {
      List<ExternalTool> tools = getExternalTools(courseId, toolName, placement);
      return tools.stream().filter(et -> toolName.equals(et.getName())).findFirst().orElse(null);
   }
}
