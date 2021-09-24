package edu.iu.uits.lms.common.session;

/*-
 * #%L
 * lms-canvas-common-configuration
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Resolver that allows for both cookie and header session id
 * @since 4.0.15
 */
@Slf4j
public class DualSessionIdResolver implements HttpSessionIdResolver {

   private final CookieHttpSessionIdResolver cookieHttpSessionIdResolver;
   private final HeaderHttpSessionIdResolver headerHttpSessionIdResolver;
   private final String headerResolverPath;

   /**
    *
    * @param headerResolverPath Path prefix that will use the x auth token header.  All other request paths will
    *                           use the session cookie
    */
   public DualSessionIdResolver(String headerResolverPath) {
      cookieHttpSessionIdResolver = new CookieHttpSessionIdResolver();
      headerHttpSessionIdResolver = HeaderHttpSessionIdResolver.xAuthToken();

      this.headerResolverPath = headerResolverPath;
   }

   @Override
   public List<String> resolveSessionIds(HttpServletRequest request) {
      List<String> sessionIds = getResolver(request).resolveSessionIds(request);
      log.trace("resolving sessionIds: {}", sessionIds);
      return sessionIds;
   }

   @Override
   public void setSessionId(HttpServletRequest request, HttpServletResponse response, String sessionId) {
      log.trace("Setting sessionId: {}", sessionId);
      getResolver(request).setSessionId(request, response, sessionId);
   }

   @Override
   public void expireSession(HttpServletRequest request, HttpServletResponse response) {
      log.trace("expiring session");
      getResolver(request).expireSession(request, response);
   }

   private HttpSessionIdResolver getResolver(HttpServletRequest request) {
      String requestURI = request.getRequestURI();
      if (requestURI.startsWith(headerResolverPath)) {
         log.trace("{}: {}", requestURI, "headerHttpSessionIdResolver");
         return headerHttpSessionIdResolver;
      }
      log.trace("{}: {}", requestURI, "cookieHttpSessionIdResolver");
      return cookieHttpSessionIdResolver;
   }
}
