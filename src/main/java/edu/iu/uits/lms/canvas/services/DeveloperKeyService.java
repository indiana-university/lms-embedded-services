package edu.iu.uits.lms.canvas.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
         HttpEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, String.class);
         log.debug("{}", responseEntity);

         if (responseEntity != null) {
            return responseEntity.getBody();
         }
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

         HttpEntity<String> responseEntity = this.restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
         log.debug("{}", responseEntity);

         if (responseEntity != null) {
            return responseEntity.getBody();
         }
      } catch (HttpClientErrorException | HttpServerErrorException hcee) {
         log.error("Unable to PUT the developer key account binding changes", hcee);
      }

      return null;
   }
}
