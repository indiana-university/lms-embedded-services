package edu.iu.uits.lms.canvasoauth2.config;

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

import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Plain Mockito unit test for {@link CanvasOAuth2ClientConfig#canvasOAuth2AuthorizedClientManager}.
 * No Spring context is loaded - this exercises only the one property the bean's whole design
 * rests on: when no {@link org.springframework.security.oauth2.client.OAuth2AuthorizedClient} is
 * already persisted for the current principal, {@code authorize()} must throw
 * {@link ClientAuthorizationRequiredException} rather than silently returning {@code null} or
 * attempting some kind of redirect - since this manager is only ever invoked directly from
 * {@code CanvasOAuth2TokenInterceptor}, a plain {@code ClientHttpRequestInterceptor}, with no
 * {@code OAuth2AuthorizationRequestRedirectFilter} in the call path to catch a redirect attempt.
 */
@ExtendWith(MockitoExtension.class)
class CanvasOAuth2ClientConfigTest {

    private static final String REGISTRATION_ID = "lms_canvas_oauth2";
    private static final String CANVAS_USER_ID = "12345";

    @Mock
    private ClientRegistrationRepository clientRegistrationRepository;

    @Mock
    private OAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository;

    private ClientRegistration clientRegistration;
    private OidcAuthenticationToken principal;

    @BeforeEach
    void setUp() {
        clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
              .clientId("test-client-id")
              .clientSecret("test-client-secret")
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
              .authorizationUri("https://canvas.test/login/oauth2/auth")
              .tokenUri("https://canvas.test/login/oauth2/token")
              .build();

        Map<String, Object> customMap = new HashMap<>();
        customMap.put(LTIConstants.CUSTOM_CANVAS_USER_ID_KEY, CANVAS_USER_ID);
        principal = TestUtils.buildToken("username", TestUtils.defaultAuthority(), new HashMap<>(), customMap);
    }

    @Test
    void authorizeThrowsClientAuthorizationRequiredExceptionWhenNoAuthorizedClientExists() {
        OAuth2AuthorizedClientManager manager = new CanvasOAuth2ClientConfig()
              .canvasOAuth2AuthorizedClientManager(clientRegistrationRepository, canvasOAuth2AuthorizedClientRepository);

        OAuth2AuthorizeRequest request = OAuth2AuthorizeRequest.withClientRegistrationId(REGISTRATION_ID)
              .principal(principal)
              .attribute(HttpServletRequest.class.getName(), new MockHttpServletRequest())
              .attribute(HttpServletResponse.class.getName(), new MockHttpServletResponse())
              .build();

        when(clientRegistrationRepository.findByRegistrationId(REGISTRATION_ID)).thenReturn(clientRegistration);
        when(canvasOAuth2AuthorizedClientRepository.loadAuthorizedClient(eq(REGISTRATION_ID), eq(principal), any()))
              .thenReturn(null);

        assertThrows(ClientAuthorizationRequiredException.class, () -> manager.authorize(request));
    }
}
