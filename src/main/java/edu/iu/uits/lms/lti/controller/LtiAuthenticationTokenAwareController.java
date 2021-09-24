package edu.iu.uits.lms.lti.controller;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import edu.iu.uits.lms.common.session.CourseSessionService;
import edu.iu.uits.lms.lti.security.LtiAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpSession;

/**
 * Created by chmaurer on 12/3/15.
 */
public class LtiAuthenticationTokenAwareController {

    public static final String SESSION_TOKEN_KEY = "session_token_key";

    /**
     * Get the authn token from the SecurityContext
     * @param context Context from a request to compare against the token's context
     * @return A validated LtiAuthenticationToken
     * @throws InvalidTokenContextException Throws exception if no token was found, or if the context doesn't match
     */
    protected LtiAuthenticationToken getValidatedToken(String context) throws InvalidTokenContextException {
        return getValidatedToken(context, null);
    }

    protected LtiAuthenticationToken getValidatedToken(String context, CourseSessionService courseSessionService) throws InvalidTokenContextException {
        LtiAuthenticationToken token = null;
        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();

        if (authToken == null) {
            throw new InvalidTokenContextException("No authentication token found");
        }

        if (authToken instanceof LtiAuthenticationToken) {
            token = (LtiAuthenticationToken) authToken;

            if (courseSessionService == null) {
                boolean contextMatch = context.equals(token.getContext());

                if (! contextMatch) {
                    throw new InvalidTokenContextException("Context in authentication token does not match request context");
                }
            } else { // we have a courseSessionService
                ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                HttpSession session = servletRequestAttributes.getRequest().getSession(false);

                LtiAuthenticationToken sessionToken = courseSessionService.getAttributeFromSession(session, context, SESSION_TOKEN_KEY, LtiAuthenticationToken.class);

                if (sessionToken == null) {
                    throw new InvalidTokenContextException("Context in authentication token does not match any session context");
                } else {
                    token = sessionToken;
                }
            }  // end else "we have a courseSessionService"
        }
        return token;
    }

    /**
     * Get the authn token from the SecurityContext. Only use this method if you do not have a
     * context/course id (ie SiteRequest or Multiclass Messenger)
     * @return a validated LtiAuthenticationToken
     * @throws InvalidTokenContextException throws exception if not token was found
     */
    protected LtiAuthenticationToken getTokenWithoutContext() {
        LtiAuthenticationToken token = null;

        Authentication authToken = SecurityContextHolder.getContext().getAuthentication();

        if (authToken == null) {
            throw new InvalidTokenContextException("No authentication token found");
        }

        if (authToken instanceof LtiAuthenticationToken) {
            token = (LtiAuthenticationToken) authToken;
        }

        return token;
    }
}
