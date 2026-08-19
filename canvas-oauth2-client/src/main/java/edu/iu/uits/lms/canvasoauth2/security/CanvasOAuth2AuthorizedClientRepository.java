package edu.iu.uits.lms.canvasoauth2.security;

/*-
 * #%L
 * LMS Canvas OAuth2 Client
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import edu.iu.uits.lms.canvasoauth2.config.CanvasOAuth2ClientProperties;
import edu.iu.uits.lms.canvasoauth2.model.CanvasOAuth2Authz;
import edu.iu.uits.lms.canvasoauth2.repository.CanvasOAuth2AuthzRepository;
import edu.iu.uits.lms.lti.service.OidcTokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.Date;
import java.util.Set;

@Component
@Slf4j
public class CanvasOAuth2AuthorizedClientRepository implements OAuth2AuthorizedClientRepository {

    private final CanvasOAuth2AuthzRepository canvasOAuth2AuthzRepository;
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final String env;
    private final TextEncryptor textEncryptor;

    public CanvasOAuth2AuthorizedClientRepository(CanvasOAuth2AuthzRepository canvasOAuth2AuthzRepository,
                                                   ClientRegistrationRepository clientRegistrationRepository,
                                                   @Value("${canvas.env}") String env,
                                                   CanvasOAuth2ClientProperties canvasOAuth2ClientProperties) {
        this.canvasOAuth2AuthzRepository = canvasOAuth2AuthzRepository;
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.env = env;
        // Use delux() (AES-GCM, authenticated) rather than text() (AES-CBC, unauthenticated) -
        // Spring's own javadoc recommends against CBC mode for security-sensitive data.
        this.textEncryptor = Encryptors.delux(canvasOAuth2ClientProperties.getEncryptionPassword(),
              canvasOAuth2ClientProperties.getEncryptionSalt());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, Authentication principal, HttpServletRequest request) {
        String canvasUserId = resolveCanvasUserId(principal);
        if (canvasUserId == null) {
            return null;
        }

        CanvasOAuth2Authz authz = canvasOAuth2AuthzRepository.findByRegistrationEnvUser(clientRegistrationId, env, canvasUserId);
        if (authz == null) {
            return null;
        }

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (clientRegistration == null) {
            log.warn("No ClientRegistration found for registrationId {}", clientRegistrationId);
            return null;
        }

        OAuth2AccessToken accessToken = new OAuth2AccessToken(resolveTokenType(authz.getTokenType()),
              textEncryptor.decrypt(authz.getAccessToken()),
              authz.getCreated() != null ? authz.getCreated().toInstant() : null,
              authz.getExpiresAt() != null ? authz.getExpiresAt().toInstant() : null,
              StringUtils.commaDelimitedListToSet(authz.getScopes()));

        OAuth2RefreshToken refreshToken = null;
        if (StringUtils.hasText(authz.getRefreshToken())) {
            refreshToken = new OAuth2RefreshToken(textEncryptor.decrypt(authz.getRefreshToken()),
                  authz.getCreated() != null ? authz.getCreated().toInstant() : null);
        }

        return (T) new OAuth2AuthorizedClient(clientRegistration, canvasUserId, accessToken, refreshToken);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal,
                                      HttpServletRequest request, HttpServletResponse response) {
        String canvasUserId = resolveCanvasUserId(principal);
        if (canvasUserId == null) {
            log.warn("Cannot save authorized client - no Canvas user id could be resolved from principal");
            return;
        }

        String registrationId = authorizedClient.getClientRegistration().getRegistrationId();
        CanvasOAuth2Authz authz = canvasOAuth2AuthzRepository.findByRegistrationEnvUser(registrationId, env, canvasUserId);
        if (authz == null) {
            authz = new CanvasOAuth2Authz();
            authz.setRegistrationId(registrationId);
            authz.setEnv(env);
            authz.setCanvasUserId(canvasUserId);
        }

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        authz.setAccessToken(textEncryptor.encrypt(accessToken.getTokenValue()));
        authz.setTokenType(accessToken.getTokenType().getValue());
        authz.setScopes(StringUtils.collectionToCommaDelimitedString(resolveScopes(authorizedClient)));
        authz.setExpiresAt(accessToken.getExpiresAt() != null ? Date.from(accessToken.getExpiresAt()) : null);

        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        if (refreshToken != null) {
            authz.setRefreshToken(textEncryptor.encrypt(refreshToken.getTokenValue()));
        }

        canvasOAuth2AuthzRepository.save(authz);
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, Authentication principal,
                                        HttpServletRequest request, HttpServletResponse response) {
        String canvasUserId = resolveCanvasUserId(principal);
        if (canvasUserId == null) {
            return;
        }
        CanvasOAuth2Authz authz = canvasOAuth2AuthzRepository.findByRegistrationEnvUser(clientRegistrationId, env, canvasUserId);
        if (authz != null) {
            canvasOAuth2AuthzRepository.delete(authz);
        }
    }

    private String resolveCanvasUserId(Authentication principal) {
        if (principal instanceof OidcAuthenticationToken oidcAuthenticationToken) {
            try {
                return new OidcTokenUtils(oidcAuthenticationToken).getUserId();
            } catch (NullPointerException e) {
                // OidcTokenUtils.getUserId() throws NPE (rather than returning null) when the
                // "custom" claims map is entirely absent from the token - treat that the same
                // as "no Canvas user id resolvable" instead of letting it propagate as a 500.
                log.debug("Unable to resolve a Canvas user id from principal {} - no custom claims present", principal);
                return null;
            }
        }
        log.debug("Principal {} is not an OidcAuthenticationToken - cannot resolve a Canvas user id", principal);
        return null;
    }

    /**
     * Reconstruct the {@link OAuth2AccessToken.TokenType} from its persisted string value.
     * Falls back to {@code BEARER} if the stored value is null or doesn't match a known type.
     */
    private OAuth2AccessToken.TokenType resolveTokenType(String tokenType) {
        if (OAuth2AccessToken.TokenType.DPOP.getValue().equalsIgnoreCase(tokenType)) {
            return OAuth2AccessToken.TokenType.DPOP;
        }
        return OAuth2AccessToken.TokenType.BEARER;
    }

    /**
     * Canvas's authorization_code token response never includes a "scope" parameter (confirmed
     * against Canvas's own OAuth2 endpoint docs - unlike its client_credentials response, which does),
     * so {@code accessToken.getScopes()} is always empty for tokens obtained through this flow. Fall
     * back to what the registration actually requested, since that's the only scope information
     * Canvas's authorization_code flow makes available - it doesn't report a narrower granted scope
     * back to the client.
     */
    private Set<String> resolveScopes(OAuth2AuthorizedClient authorizedClient) {
        Set<String> grantedScopes = authorizedClient.getAccessToken().getScopes();
        if (!CollectionUtils.isEmpty(grantedScopes)) {
            return grantedScopes;
        }
        return authorizedClient.getClientRegistration().getScopes();
    }
}
