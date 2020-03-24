package edu.iu.uits.lms.email.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.security.Aws4AuthUtil;
import edu.iu.uits.lms.email.config.SignedEmailServiceConfig;
import edu.iu.uits.lms.email.model.sis.Message;
import edu.iu.uits.lms.email.model.sis.Result;
import lombok.extern.log4j.Log4j;
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

@Log4j
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
