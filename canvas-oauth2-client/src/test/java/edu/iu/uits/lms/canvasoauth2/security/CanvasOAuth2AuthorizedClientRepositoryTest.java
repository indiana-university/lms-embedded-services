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
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Plain Mockito unit tests for {@link CanvasOAuth2AuthorizedClientRepository} - no Spring
 * context is loaded. Exercises the load/save/remove logic directly, including the
 * encrypt-on-save / decrypt-on-load round trip and the OidcAuthenticationToken-vs-other-principal
 * branch.
 */
@ExtendWith(MockitoExtension.class)
class CanvasOAuth2AuthorizedClientRepositoryTest {

    private static final String REGISTRATION_ID = "lms_canvas_oauth2";
    private static final String ENV = "dev";
    private static final String CANVAS_USER_ID = "12345";
    private static final String ENCRYPTION_PASSWORD = "test-password";
    private static final String ENCRYPTION_SALT = "deadbeef";

    @Mock
    private CanvasOAuth2AuthzRepository canvasOAuth2AuthzRepository;

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    private CanvasOAuth2AuthorizedClientRepository repository;
    private ClientRegistration clientRegistration;
    private OidcAuthenticationToken principal;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private TextEncryptor referenceEncryptor;

    @BeforeEach
    void setUp() {
        CanvasOAuth2ClientProperties properties = new CanvasOAuth2ClientProperties();
        properties.setEncryptionPassword(ENCRYPTION_PASSWORD);
        properties.setEncryptionSalt(ENCRYPTION_SALT);

        repository = new CanvasOAuth2AuthorizedClientRepository(canvasOAuth2AuthzRepository,
              clientRegistrationRepository, ENV, properties);

        // Independent encryptor built the same way the repository builds its own, so tests can
        // verify a genuine encrypt/decrypt round trip rather than asserting against plaintext.
        referenceEncryptor = Encryptors.delux(ENCRYPTION_PASSWORD, ENCRYPTION_SALT);

        clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
              .clientId("test-client-id")
              .clientSecret("test-client-secret")
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
              .authorizationUri("https://canvas.test/login/oauth2/auth")
              .tokenUri("https://canvas.test/login/oauth2/token")
              .scope("url:GET|/api/v1/courses/:course_id/users")
              .build();

        Map<String, Object> customMap = new HashMap<>();
        customMap.put(LTIConstants.CUSTOM_CANVAS_USER_ID_KEY, CANVAS_USER_ID);
        principal = TestUtils.buildToken("username", TestUtils.defaultAuthority(), new HashMap<>(), customMap);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void loadReturnsDecryptedAuthorizedClientWhenRowExists() {
        CanvasOAuth2Authz authz = new CanvasOAuth2Authz();
        authz.setRegistrationId(REGISTRATION_ID);
        authz.setEnv(ENV);
        authz.setCanvasUserId(CANVAS_USER_ID);
        authz.setTokenType("bearer");
        authz.setScopes("url:GET|/api/v1/courses");
        authz.setCreated(new Date());
        authz.setExpiresAt(Date.from(Instant.now().plusSeconds(3600)));
        authz.setAccessToken(referenceEncryptor.encrypt("real-access-token"));
        authz.setRefreshToken(referenceEncryptor.encrypt("real-refresh-token"));

        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(authz);
        when(clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID)).thenReturn(clientRegistration);

        OAuth2AuthorizedClient result = repository.loadAuthorizedClient(REGISTRATION_ID, principal, request);

        assertNotNull(result);
        assertEquals("real-access-token", result.getAccessToken().getTokenValue());
        assertEquals(OAuth2AccessToken.TokenType.BEARER, result.getAccessToken().getTokenType());
        assertEquals(Set.of("url:GET|/api/v1/courses"), result.getAccessToken().getScopes());
        assertNotNull(result.getRefreshToken());
        assertEquals("real-refresh-token", result.getRefreshToken().getTokenValue());
        assertEquals(CANVAS_USER_ID, result.getPrincipalName());
        assertSame(clientRegistration, result.getClientRegistration());
    }

    @Test
    void loadResolvesDpopTokenTypeWhenPersistedAsDpop() {
        CanvasOAuth2Authz authz = new CanvasOAuth2Authz();
        authz.setRegistrationId(REGISTRATION_ID);
        authz.setEnv(ENV);
        authz.setCanvasUserId(CANVAS_USER_ID);
        authz.setTokenType("dpop");
        authz.setScopes("url:GET|/api/v1/courses");
        authz.setCreated(new Date());
        authz.setExpiresAt(Date.from(Instant.now().plusSeconds(3600)));
        authz.setAccessToken(referenceEncryptor.encrypt("real-access-token"));

        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(authz);
        when(clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID)).thenReturn(clientRegistration);

        OAuth2AuthorizedClient result = repository.loadAuthorizedClient(REGISTRATION_ID, principal, request);

        assertNotNull(result);
        assertEquals(OAuth2AccessToken.TokenType.DPOP, result.getAccessToken().getTokenType());
    }

    @Test
    void loadFallsBackToBearerWhenPersistedTokenTypeIsNullOrUnrecognized() {
        CanvasOAuth2Authz authz = new CanvasOAuth2Authz();
        authz.setRegistrationId(REGISTRATION_ID);
        authz.setEnv(ENV);
        authz.setCanvasUserId(CANVAS_USER_ID);
        authz.setTokenType("something-unrecognized");
        authz.setScopes("url:GET|/api/v1/courses");
        authz.setCreated(new Date());
        authz.setExpiresAt(Date.from(Instant.now().plusSeconds(3600)));
        authz.setAccessToken(referenceEncryptor.encrypt("real-access-token"));

        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(authz);
        when(clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID)).thenReturn(clientRegistration);

        OAuth2AuthorizedClient result = repository.loadAuthorizedClient(REGISTRATION_ID, principal, request);

        assertNotNull(result);
        assertEquals(OAuth2AccessToken.TokenType.BEARER, result.getAccessToken().getTokenType());
    }

    @Test
    void loadReturnsNullWhenNoRowExists() {
        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(null);

        OAuth2AuthorizedClient result = repository.loadAuthorizedClient(REGISTRATION_ID, principal, request);

        assertNull(result);
        verifyNoInteractions(clientRegistrationRepository);
    }

    @Test
    void loadReturnsNullWhenPrincipalIsNotOidcAuthenticationToken() {
        Authentication other = new UsernamePasswordAuthenticationToken("someone", "n/a");

        OAuth2AuthorizedClient result = repository.loadAuthorizedClient(REGISTRATION_ID, other, request);

        assertNull(result);
        verifyNoInteractions(canvasOAuth2AuthzRepository, clientRegistrationRepository);
    }

    @Test
    void saveEncryptsAndPersistsNewRow() {
        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(null);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
              "plain-access-token", Instant.now(), Instant.now().plusSeconds(3600), Set.of("scope1", "scope2"));
        OAuth2RefreshToken refreshToken = new OAuth2RefreshToken("plain-refresh-token", Instant.now());
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, CANVAS_USER_ID,
              accessToken, refreshToken);

        repository.saveAuthorizedClient(authorizedClient, principal, request, response);

        ArgumentCaptor<CanvasOAuth2Authz> captor = ArgumentCaptor.forClass(CanvasOAuth2Authz.class);
        verify(canvasOAuth2AuthzRepository).save(captor.capture());
        CanvasOAuth2Authz saved = captor.getValue();

        assertEquals(REGISTRATION_ID, saved.getRegistrationId());
        assertEquals(ENV, saved.getEnv());
        assertEquals(CANVAS_USER_ID, saved.getCanvasUserId());
        assertEquals(OAuth2AccessToken.TokenType.BEARER.getValue(), saved.getTokenType());
        assertNotEquals("plain-access-token", saved.getAccessToken());
        assertNotEquals("plain-refresh-token", saved.getRefreshToken());
        assertEquals("plain-access-token", referenceEncryptor.decrypt(saved.getAccessToken()));
        assertEquals("plain-refresh-token", referenceEncryptor.decrypt(saved.getRefreshToken()));
    }

    @Test
    void saveFallsBackToRegisteredScopeWhenAccessTokenReportsNoScopes() {
        // Canvas's real authorization_code token response never includes a "scope" parameter
        // (confirmed against Canvas's own OAuth2 endpoint docs), so the OAuth2AccessToken built from
        // a genuine Canvas response always has an empty scope set - this simulates that, rather than
        // the unrealistic pre-populated-scope tokens the other save tests build.
        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(null);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
              "plain-access-token", Instant.now(), Instant.now().plusSeconds(3600), Set.of());
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, CANVAS_USER_ID,
              accessToken);

        repository.saveAuthorizedClient(authorizedClient, principal, request, response);

        ArgumentCaptor<CanvasOAuth2Authz> captor = ArgumentCaptor.forClass(CanvasOAuth2Authz.class);
        verify(canvasOAuth2AuthzRepository).save(captor.capture());
        assertEquals("url:GET|/api/v1/courses/:course_id/users", captor.getValue().getScopes());
    }

    @Test
    void savePrefersAccessTokenScopesOverRegisteredScopeWhenBothPresent() {
        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(null);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
              "plain-access-token", Instant.now(), Instant.now().plusSeconds(3600), Set.of("scope1"));
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, CANVAS_USER_ID,
              accessToken);

        repository.saveAuthorizedClient(authorizedClient, principal, request, response);

        ArgumentCaptor<CanvasOAuth2Authz> captor = ArgumentCaptor.forClass(CanvasOAuth2Authz.class);
        verify(canvasOAuth2AuthzRepository).save(captor.capture());
        // Not the registration's "url:GET|..." scope - proves the granted scope wins when present.
        assertEquals("scope1", captor.getValue().getScopes());
    }

    @Test
    void saveUpdatesExistingRowRatherThanDuplicating() {
        CanvasOAuth2Authz existing = new CanvasOAuth2Authz();
        existing.setId(99L);
        existing.setRegistrationId(REGISTRATION_ID);
        existing.setEnv(ENV);
        existing.setCanvasUserId(CANVAS_USER_ID);
        existing.setAccessToken(referenceEncryptor.encrypt("old-access-token"));

        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(existing);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
              "new-access-token", Instant.now(), Instant.now().plusSeconds(3600), Set.of("scope1"));
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, CANVAS_USER_ID,
              accessToken);

        repository.saveAuthorizedClient(authorizedClient, principal, request, response);

        ArgumentCaptor<CanvasOAuth2Authz> captor = ArgumentCaptor.forClass(CanvasOAuth2Authz.class);
        verify(canvasOAuth2AuthzRepository).save(captor.capture());
        CanvasOAuth2Authz saved = captor.getValue();

        // The same entity instance (same id) is updated in place rather than a new row being created.
        assertSame(existing, saved);
        assertEquals(99L, saved.getId());
        assertEquals("new-access-token", referenceEncryptor.decrypt(saved.getAccessToken()));
    }

    @Test
    void removeDeletesRowWhenFound() {
        CanvasOAuth2Authz existing = new CanvasOAuth2Authz();
        existing.setRegistrationId(REGISTRATION_ID);
        existing.setEnv(ENV);
        existing.setCanvasUserId(CANVAS_USER_ID);

        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(existing);

        repository.removeAuthorizedClient(REGISTRATION_ID, principal, request, response);

        verify(canvasOAuth2AuthzRepository).delete(existing);
    }

    @Test
    void removeNoOpsWhenNotFound() {
        when(canvasOAuth2AuthzRepository.findByRegistrationEnvUser(REGISTRATION_ID, ENV, CANVAS_USER_ID))
              .thenReturn(null);

        repository.removeAuthorizedClient(REGISTRATION_ID, principal, request, response);

        verify(canvasOAuth2AuthzRepository, never()).delete(any());
    }
}
