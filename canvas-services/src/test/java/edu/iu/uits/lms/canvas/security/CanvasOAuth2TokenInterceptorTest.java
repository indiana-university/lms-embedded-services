package edu.iu.uits.lms.canvas.security;

/*-
 * #%L
 * LMS Canvas Services
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CanvasOAuth2TokenInterceptorTest {

    private static final String REGISTRATION_ID = "lms_canvas_oauth2";

    @BeforeEach
    void setUp() {
        RequestContextHolder.setRequestAttributes(
              new ServletRequestAttributes(new MockHttpServletRequest(), new MockHttpServletResponse()));
        SecurityContextHolder.getContext().setAuthentication(
              new TestingAuthenticationToken("userId", "n/a"));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        SecurityContextHolder.clearContext();
    }

    @Test
    void addsBearerHeaderFromAuthorizedClient() throws Exception {
        OAuth2AuthorizedClientManager authorizedClientManager = mock(OAuth2AuthorizedClientManager.class);
        ClientRegistration clientRegistration = ClientRegistration.withRegistrationId(REGISTRATION_ID)
              .clientId("test-client")
              .clientSecret("test-secret")
              .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
              .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
              .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
              .authorizationUri("https://canvas.test/login/oauth2/auth")
              .tokenUri("https://canvas.test/login/oauth2/token")
              .build();
        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
              "the-access-token", Instant.now(), Instant.now().plusSeconds(3600));
        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(clientRegistration, "userId", accessToken);

        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(authorizedClient);

        CanvasOAuth2TokenInterceptor interceptor = new CanvasOAuth2TokenInterceptor(authorizedClientManager, REGISTRATION_ID);

        MockClientHttpRequest request = new MockClientHttpRequest();
        ClientHttpRequestExecution execution = mock(ClientHttpRequestExecution.class);
        ClientHttpResponse expectedResponse = mock(ClientHttpResponse.class);
        when(execution.execute(any(), any())).thenReturn(expectedResponse);

        ClientHttpResponse actualResponse = interceptor.intercept(request, new byte[0], execution);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Bearer the-access-token", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void throwsClientAuthorizationRequiredExceptionWhenNoAuthorizedClient() {
        OAuth2AuthorizedClientManager authorizedClientManager = mock(OAuth2AuthorizedClientManager.class);
        when(authorizedClientManager.authorize(any(OAuth2AuthorizeRequest.class))).thenReturn(null);

        CanvasOAuth2TokenInterceptor interceptor = new CanvasOAuth2TokenInterceptor(authorizedClientManager, REGISTRATION_ID);

        MockClientHttpRequest request = new MockClientHttpRequest();
        ClientHttpRequestExecution execution = Mockito.mock(ClientHttpRequestExecution.class);

        assertThrows(ClientAuthorizationRequiredException.class,
              () -> interceptor.intercept(request, new byte[0], execution));
    }
}
