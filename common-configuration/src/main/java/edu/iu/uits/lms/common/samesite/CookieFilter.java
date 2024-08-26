package edu.iu.uits.lms.common.samesite;

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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.util.PatternMatchUtils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class CookieFilter implements Filter {

   /**
    * Pattern identifying "bad" user agents that don't like to set the SameSite=None cookie attribute
    */
   private Pattern pattern;

   /**
    * String[] of request paths to ignore.
    */
   private String[] ignoredRequests;

   public CookieFilter(String uaPattern, String[] ignoredRequestPatterns) {
      log.info("Registering CookieFilter with ignored UA pattern '{}' and ignored request patterns '{}'", uaPattern, ignoredRequestPatterns);
      pattern = Pattern.compile(uaPattern, Pattern.CASE_INSENSITIVE);
      this.ignoredRequests = ignoredRequestPatterns;
   }

   @Override
   public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
         throws IOException, ServletException {
      HttpServletRequest req = (HttpServletRequest) request;
      HttpServletResponse res = (HttpServletResponse) response;

      String userAgent = req.getHeader(HttpHeaders.USER_AGENT);
      log.trace("User agent: {}", userAgent);
      log.trace("Request: {}", req.getRequestURL());

      if (isOkUserAgent(userAgent) && !isIgnoredRequest(req.getServletPath())) {
         //Call the chain with our wrapped response
         chain.doFilter(req, new LmsServletResponseWrapper(res));
      } else {
         //Call the chain with normal response
         log.trace("Skipping the session cookie rewrite");
         chain.doFilter(req, res);
      }
   }

   /**
    * Identify a user-agent string as good/bad based on the defined pattern above
    * @param userAgent User-agent string as reported by the browser
    * @return True if the pattern does not match the flagged "bad" patterns
    */
   private boolean isOkUserAgent(String userAgent) {
      if(userAgent == null) {
         return true;
      }
      Matcher matcher = pattern.matcher(userAgent);
      boolean isBad = matcher.matches();
      return !isBad;
   }

   /**
    * Identify a request to be ignored based on the pattern specified
    * @param req Current request
    * @return True if the request matched a pattern to be ignored
    */
   private boolean isIgnoredRequest(String req) {
      return PatternMatchUtils.simpleMatch(ignoredRequests, req);
   }
}
