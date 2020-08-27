package edu.iu.uits.lms.email;

import edu.iu.uits.lms.common.oauth.OAuthConfig;
import edu.iu.uits.lms.common.oauth.OpenResourceOwnerPasswordResourceDetails;
import email.client.generated.ApiClient;
import email.client.generated.api.EmailApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.DefaultOAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.AccessTokenRequest;
import org.springframework.security.oauth2.client.token.DefaultAccessTokenRequest;
import org.springframework.security.oauth2.common.AuthenticationScheme;

import java.util.Collections;

@EnableConfigurationProperties(OAuthConfig.class)
public class EmailClientConfig {

   @Autowired
   private OAuthConfig oAuthConfig;

   @Bean
   public EmailApi emailApi() {
      return new EmailApi(new ApiClient(emailRestTemplate()));
   }

   @Bean(name = "emailRestTemplate")
   public OAuth2RestTemplate emailRestTemplate() {
      OpenResourceOwnerPasswordResourceDetails resourceDetails = new OpenResourceOwnerPasswordResourceDetails();
      resourceDetails.setClientId(oAuthConfig.getClientId());
      resourceDetails.setClientSecret(oAuthConfig.getClientSecret());
      resourceDetails.setUsername(oAuthConfig.getClientId());
      resourceDetails.setPassword(oAuthConfig.getClientPassword());
      resourceDetails.setAccessTokenUri(oAuthConfig.getAccessTokenUri());
      resourceDetails.setClientAuthenticationScheme(AuthenticationScheme.form);
      resourceDetails.setScope(Collections.singletonList("email:send"));

      AccessTokenRequest atr = new DefaultAccessTokenRequest();
      DefaultOAuth2ClientContext clientContext = new DefaultOAuth2ClientContext(atr);

      OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails, clientContext);
      return restTemplate;
   }
}
