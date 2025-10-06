package uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web;

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

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import uk.ac.ox.ctl.lti13.utils.StringReader;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @see StateCheckingAuthenticationSuccessHandler
 */
public class StateAuthorizationRedirectHandler implements AuthorizationRedirectHandler {

	private final Logger logger = LoggerFactory.getLogger(StateAuthorizationRedirectHandler.class);

	private final JsonStringEncoder encoder = JsonStringEncoder.getInstance();
	private final String htmlTemplate;

	private String name = "/uk/ac/ox/ctl/lti13/step-1-redirect.html";

	public StateAuthorizationRedirectHandler() {
		try {
			htmlTemplate = StringReader.readString(getClass().getResourceAsStream(name));
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read "+ name, e);
		}
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * This sends the user off, but before that it saves data in the user's browser's sessionStorage so that
	 * when they come back we can check that noting malicious is going on.
	 */
	public void sendRedirect(HttpServletRequest request, HttpServletResponse response, OAuth2AuthorizationRequest authorizationRequest) throws IOException {
		String url = authorizationRequest.getAuthorizationRequestUri();
		if (response.isCommitted()) {
            logger.debug("Response has already been committed. Unable to redirect to {}", url);
			return;
		}
		String state = new String(encoder.quoteAsString(authorizationRequest.getState()));
		// TODO We should be using a LTI Specific Auth request here.
		String nonce = new String(encoder.quoteAsString((String)authorizationRequest.getAdditionalParameters().get("nonce")));
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter writer = response.getWriter();
		final String body = htmlTemplate
				.replaceFirst("@@state@@", state)
				.replaceFirst("@@url@@", url)
				.replaceFirst("@@nonce@@", nonce);
		writer.append(body);
	}
}
