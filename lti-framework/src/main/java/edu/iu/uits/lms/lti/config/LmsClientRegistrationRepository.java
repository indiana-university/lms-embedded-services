package edu.iu.uits.lms.lti.config;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
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

import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.service.LtiAuthorizationService;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
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

   private OAuth2ClientProperties oAuth2ClientProperties;
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

      // If the calling tool has configured any additional clients (via standard application.yml means), wire them in here as well.
      List<ClientRegistration> additionalRegistrations = new ArrayList<>(
            OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(oAuth2ClientProperties).values());
      registrations.addAll(additionalRegistrations);

      return registrations.stream().collect(Collectors.toMap(ClientRegistration::getRegistrationId, Function.identity()));
   }
}
