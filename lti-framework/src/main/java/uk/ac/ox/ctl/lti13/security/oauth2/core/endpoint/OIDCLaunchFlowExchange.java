/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ox.ctl.lti13.security.oauth2.core.endpoint;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.util.Assert;

/**
 * An &quot;exchange&quot; of an OAuth 2.0 Authorization Request and Response
 * for the authorization code grant type.
 *
 * @author Joe Grandja
 * @since 5.0
 * @see OAuth2AuthorizationRequest
 * @see OAuth2AuthorizationResponse
 */
public final class OIDCLaunchFlowExchange {
	// This should be our own class in the future
	private final OAuth2AuthorizationRequest authorizationRequest;
	private final OIDCLaunchFlowResponse authorizationResponse;

	/**
	 * Constructs a new {@code OAuth2AuthorizationExchange} with the provided
	 * Authorization Request and Authorization Response.
	 *
	 * @param authorizationRequest the {@link OAuth2AuthorizationRequest Authorization Request}
	 * @param authorizationResponse the {@link OAuth2AuthorizationResponse Authorization Response}
	 */
	public OIDCLaunchFlowExchange(OAuth2AuthorizationRequest authorizationRequest,
								  OIDCLaunchFlowResponse authorizationResponse) {
		Assert.notNull(authorizationRequest, "authorizationRequest cannot be null");
		Assert.notNull(authorizationResponse, "authorizationResponse cannot be null");
		this.authorizationRequest = authorizationRequest;
		this.authorizationResponse = authorizationResponse;
	}

	/**
	 * Returns the {@link OAuth2AuthorizationRequest Authorization Request}.
	 *
	 * @return the {@link OAuth2AuthorizationRequest}
	 */
	public OAuth2AuthorizationRequest getAuthorizationRequest() {
		return this.authorizationRequest;
	}

	/**
	 * Returns the {@link OIDCLaunchFlowResponse Authorization Response}.
	 *
	 * @return the {@link OIDCLaunchFlowResponse}
	 */
	public OIDCLaunchFlowResponse getAuthorizationResponse() {
		return this.authorizationResponse;
	}
}
