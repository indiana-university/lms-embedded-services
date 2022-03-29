package edu.iu.uits.lms.lti.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@ConfigurationProperties(prefix = "lti.clientregistration")
@Getter
@Setter
public class LtiClientRegistrationProperties {

   private String defaultClient;

   private Map<String, RegistrationDetails> clients;

   public RegistrationDetails getDefaultRegistrationDetails() {
      return clients.get(defaultClient);
   }

   @Getter
   @Setter
   protected static class RegistrationDetails {
      private String issuer;
      private String authzUrl;
      private String tokenUri;
      private String jwkSetUri;
   }
}
