package edu.iu.uits.lms.lti.config;

import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.service.LtiAuthorizationService;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Custom implementation of a ClientRegistrationRepository so we can look up registrations in real-time.
 */
@AllArgsConstructor
public class LmsClientRegistrationRepository implements ClientRegistrationRepository {

   private LtiClientRegistrationProperties ltiClientRegistrationProperties;
   private LtiAuthorizationService ltiAuthorizationService;
   private String env;
   private List<String> toolKeys;
   private String toolKeyPrefix;

   @Override
   public ClientRegistration findByRegistrationId(String registrationId) {
      return buildRegistrations().get(registrationId);
   }

   /**
    * Build a map containing all the desired registrations based on the took keys and/or prefixes
    * @return Map of ClientRegistration objects with the registrationId as the key.
    */
   private Map<String, ClientRegistration> buildRegistrations() {
      LtiClientRegistrationProperties.RegistrationDetails registrationDetails = ltiClientRegistrationProperties.getDefaultRegistrationDetails();

      List<ClientRegistration> registrations = new ArrayList<>();

      // Use toolKey and/or prefix to lookup client and secret
      List<LmsLtiAuthz> ltiAuthzs = ltiAuthorizationService.findByRegistrationsPrefixesEnvActive(toolKeys, toolKeyPrefix, env);
      if (ltiAuthzs != null) {
         for (LmsLtiAuthz ltiAuthz :ltiAuthzs) {

            ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(ltiAuthz.getRegistrationId())
                  .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                  .authorizationGrantType(AuthorizationGrantType.IMPLICIT)
                  .redirectUri("{baseUrl}/lti/login")
                  .scope("openid")
                  .authorizationUri(registrationDetails.getAuthzUrl())
                  .tokenUri(registrationDetails.getTokenUri())
                  .jwkSetUri(registrationDetails.getJwkSetUri())
                  .issuerUri(registrationDetails.getIssuer())
                  .userNameAttributeName("sub")
                  .clientName(ltiAuthz.getRegistrationId())
                  .clientId(ltiAuthz.getClientId())
                  .clientSecret(ltiAuthz.getSecret());

            registrations.add(builder.build());
         }
      }
      return registrations.stream().collect(Collectors.toMap(ClientRegistration::getRegistrationId, Function.identity()));
   }
}
