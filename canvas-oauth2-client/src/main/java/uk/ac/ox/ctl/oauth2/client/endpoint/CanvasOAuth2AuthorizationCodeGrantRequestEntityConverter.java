/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ox.ctl.oauth2.client.endpoint;

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

import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

/**
 * This class is so that we can ask Canvas to replace the existing OAuth tokens on a user's account.
 * If we don't do this then a user will end up with multiple Approved Integrations on their settings
 * page for the same application.
 * <p>
 * As of Spring Security 7.x, only this class's {@link #REPLACE_TOKENS}/{@link #REPLACE_TOKENS_VALUE}
 * constants are actually reused (directly, by viewem's {@code SecurityConfig}). This class's own
 * {@link #convert} method is not wired into any consumer: it implements {@code Converter<
 * OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>>}, the "replace the whole RequestEntity"
 * extension point that {@code RestClientAuthorizationCodeTokenResponseClient} (Spring Security 7.x's
 * {@code OAuth2AccessTokenResponseClient} for the authorization_code grant) no longer exposes - it
 * only accepts narrower headers/parameters converters and a {@code RestClient}. If you're looking at
 * this class hoping to wire it up somewhere, it can't be, as designed, under current Spring Security;
 * see viewem's {@code SecurityConfig#canvasOAuth2AccessTokenResponseClient} for how the same
 * replace_tokens behavior is achieved instead.
 */
public class CanvasOAuth2AuthorizationCodeGrantRequestEntityConverter
    implements Converter<OAuth2AuthorizationCodeGrantRequest, RequestEntity<?>> {

  public static final String REPLACE_TOKENS = "replace_tokens";
  public static final String REPLACE_TOKENS_VALUE = "true";

  /**
   * Returns the {@link RequestEntity} used for the Access Token Request.
   *
   * @param authorizationCodeGrantRequest the authorization code grant request
   * @return the {@link RequestEntity} used for the Access Token Request
   */
  @Override
  public RequestEntity<?> convert(
      OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
    ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();

    HttpHeaders headers =
        OAuth2AuthorizationGrantRequestEntityUtils.getTokenRequestHeaders(clientRegistration);
    MultiValueMap<String, String> formParameters =
        this.buildFormParameters(authorizationCodeGrantRequest);
    URI uri =
        UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getTokenUri())
            .build()
            .toUri();

    return new RequestEntity<>(formParameters, headers, HttpMethod.POST, uri);
  }

  /**
   * Returns a {@link MultiValueMap} of the form parameters used for the Access Token Request body.
   *
   * @param authorizationCodeGrantRequest the authorization code grant request
   * @return a {@link MultiValueMap} of the form parameters used for the Access Token Request body
   */
  private MultiValueMap<String, String> buildFormParameters(
      OAuth2AuthorizationCodeGrantRequest authorizationCodeGrantRequest) {
    ClientRegistration clientRegistration = authorizationCodeGrantRequest.getClientRegistration();
    OAuth2AuthorizationExchange authorizationExchange =
        authorizationCodeGrantRequest.getAuthorizationExchange();

    MultiValueMap<String, String> formParameters = new LinkedMultiValueMap<>();
    formParameters.add(
        OAuth2ParameterNames.GRANT_TYPE, authorizationCodeGrantRequest.getGrantType().getValue());
    formParameters.add(
        OAuth2ParameterNames.CODE, authorizationExchange.getAuthorizationResponse().getCode());
    formParameters.add(
        OAuth2ParameterNames.REDIRECT_URI,
        authorizationExchange.getAuthorizationRequest().getRedirectUri());
    // This is the special Canvas bit.
    formParameters.add(REPLACE_TOKENS, REPLACE_TOKENS_VALUE);
    if (ClientAuthenticationMethod.CLIENT_SECRET_POST.equals(clientRegistration.getClientAuthenticationMethod()) ) {
      formParameters.add(OAuth2ParameterNames.CLIENT_ID, clientRegistration.getClientId());
      formParameters.add(OAuth2ParameterNames.CLIENT_SECRET, clientRegistration.getClientSecret());
    } else {
      // Maybe we should validate the client registration before we start
      throw new InternalAuthenticationServiceException("Client Registration "+ clientRegistration.getClientId() + " can't be supported with a client authentication of: "+ clientRegistration.getClientAuthenticationMethod());
    }

    return formParameters;
  }
}
