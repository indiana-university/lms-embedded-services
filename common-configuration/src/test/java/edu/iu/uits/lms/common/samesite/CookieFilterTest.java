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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockCookie;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.HttpCookie;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Slf4j
public class CookieFilterTest {
   private MockHttpServletResponse response;
   private MockFilterChain mockRedirectChain;

   private static final String cookieNameToFilter = "JSESSIONID";
   private static final String uaPattern = "BAD PATTERN";

   @BeforeEach
   public void beforeEach() {
      response = new MockHttpServletResponse();
      response.addHeader(HttpHeaders.SET_COOKIE,
            "randoCookie=foobar;Path=/;HttpOnly");

      final String[] ignoredRequests = {"/ignored", "/chicken/**"};

      CookieFilter filter = new CookieFilter(uaPattern, ignoredRequests);
      FakeFilter filter2 = new FakeFilter(cookieNameToFilter);

      Servlet redirectServlet = new TestRedirectServlet();
      mockRedirectChain = new MockFilterChain(redirectServlet, filter, filter2);
   }

   /** Test the samesite filter works correctly with None values when a redirect response is issued. */
   @Test
   public void testRegular() throws IOException, ServletException {
      MockHttpServletRequest mockRequest = new MockHttpServletRequest();
      mockRequest.setMethod("POST");
      mockRequest.setServletPath("/foo");
      mockRequest.addHeader(HttpHeaders.USER_AGENT, "asdf");
      mockRedirectChain.doFilter(mockRequest, response);

      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, false);
   }

   @Test
   public void testIgnoredPath() throws IOException, ServletException {
      MockHttpServletRequest mockIgnoredRequest = new MockHttpServletRequest();
      mockIgnoredRequest.setMethod("POST");
      mockIgnoredRequest.setServletPath("/ignored");
      mockIgnoredRequest.addHeader(HttpHeaders.USER_AGENT, "asdf");

      mockRedirectChain.doFilter(mockIgnoredRequest, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("Lax",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, false);
   }

   @Test
   public void testIgnoredPath2() throws IOException, ServletException {
      MockHttpServletRequest mockIgnoredRequest = new MockHttpServletRequest();
      mockIgnoredRequest.setMethod("POST");
      mockIgnoredRequest.setServletPath("/chicken/nuggets");
      mockIgnoredRequest.addHeader(HttpHeaders.USER_AGENT, "asdf");

      mockRedirectChain.doFilter(mockIgnoredRequest, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("Lax",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, false);
   }

   @Test
   public void testIgnoredUserAgent() throws IOException, ServletException {
      MockHttpServletRequest mockIgnoredUserAgent = new MockHttpServletRequest();
      mockIgnoredUserAgent.setMethod("POST");
      mockIgnoredUserAgent.setServletPath("/foo");
      mockIgnoredUserAgent.addHeader(HttpHeaders.USER_AGENT, uaPattern);

      mockRedirectChain.doFilter(mockIgnoredUserAgent, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("Lax",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, false);
   }

   @Test
   public void testSecure() throws IOException, ServletException {
      MockHttpServletRequest mockSecureRequest = new MockHttpServletRequest();
      mockSecureRequest.setMethod("POST");
      mockSecureRequest.setSecure(true);
      mockSecureRequest.setServletPath("/secure");
      mockSecureRequest.addHeader(HttpHeaders.USER_AGENT, "asdf");
      mockRedirectChain.doFilter(mockSecureRequest, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, true);
   }

   @Test
   public void cookieParser() {
      String cookieHeaderString = "Set-Cookie: SESSION=YWY4Y2UzMjMtMGY3Yy00MmM1LWE5ODYtZWUwN2I5YjQ0Mzkx; Path=/; HttpOnly; SameSite=Lax";
      List<HttpCookie> cookies = HttpCookie.parse(cookieHeaderString);
      Assertions.assertEquals(1, cookies.size());
      HttpCookie cookie = cookies.get(0);
      Assertions.assertEquals("SESSION", cookie.getName());
      Assertions.assertEquals("YWY4Y2UzMjMtMGY3Yy00MmM1LWE5ODYtZWUwN2I5YjQ0Mzkx", cookie.getValue());
      Assertions.assertEquals("/", cookie.getPath());
      Assertions.assertEquals(-1, cookie.getMaxAge());
      Assertions.assertTrue(cookie.isHttpOnly());
      Assertions.assertFalse(cookie.getSecure());
   }

   @Test
   public void testNullUserAgent() throws Exception {
      MockHttpServletRequest mockSecureRequest = new MockHttpServletRequest();
      mockSecureRequest.setMethod("POST");
      mockSecureRequest.setSecure(true);
      mockSecureRequest.setServletPath("/secure");
//      mockSecureRequest.addHeader(HttpHeaders.USER_AGENT, "asdf");
      mockRedirectChain.doFilter(mockSecureRequest, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, true);
   }

   @Test
   public void testEmptyUserAgent() throws Exception {
      MockHttpServletRequest mockSecureRequest = new MockHttpServletRequest();
      mockSecureRequest.setMethod("POST");
      mockSecureRequest.setSecure(true);
      mockSecureRequest.setServletPath("/secure");
      mockSecureRequest.addHeader(HttpHeaders.USER_AGENT, "");
      mockRedirectChain.doFilter(mockSecureRequest, response);
      Assertions.assertTrue(mockRedirectChain.getResponse() instanceof MockHttpServletResponse);

      testExpectedHeadersInResponse("None",(MockHttpServletResponse)mockRedirectChain.getResponse(),
            Collections.singletonList(cookieNameToFilter),
            Collections.singletonList("randoCookie"),2, true);
   }

   /**
    * Test the Set-Cookie headers in the response contain the {@literal SameSite=<sameSiteValue>} attribute if they are named
    * in the {@code cookiesWithSamesite} list, and do not if named in the {@code cookiesWithoutSameSite} list.
    * <p>
    * Also checks the number of Set-Cookie headers matches {@code numberOfHeaders}. This makes sure the filter
    * is not adding or removing headers during operation - it should only ever append the SameSite attribute
    * to existing cookies.
    * </p>
    *
    * @param sameSiteValue the value of samesite to check for.
    * @param response the http servlet response.
    * @param cookiesWithSamesite the list of cookies that should have the {@literal SameSite=None} attribute set.
    * @param cookiesWithoutSameSite the list of cookies that should not have the {@literal SameSite} attribute set.
    * @param numberOfHeaders the number of Set-Cookie headers expected in the response.
    */
   private void testExpectedHeadersInResponse(final String sameSiteValue, final MockHttpServletResponse response,
                                              final List<String> cookiesWithSamesite, final List<String> cookiesWithoutSameSite,
                                              final int numberOfHeaders, final boolean secure) {

      final Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);

      Assertions.assertEquals(numberOfHeaders, headers.size());

      int withCount = 0;
      int withoutCount = 0;

      for (String header : headers) {

         List<HttpCookie> cookies = HttpCookie.parse(header);
         Assertions.assertNotNull(cookies);
         Assertions.assertTrue(cookies.size()==1);
         Cookie cookie = response.getCookie(cookies.get(0).getName());
         Assertions.assertNotNull(cookie);
         Assertions.assertTrue(cookie instanceof MockCookie);
         MockCookie mockCookie = (MockCookie) cookie;

         if (cookiesWithSamesite.contains(mockCookie.getName())) {
            Assertions.assertNotNull(mockCookie.getSameSite());
            Assertions.assertEquals(sameSiteValue, mockCookie.getSameSite());
            Assertions.assertEquals(secure, mockCookie.getSecure());
            withCount++;
         } else if (cookiesWithoutSameSite.contains(mockCookie.getName())) {
            Assertions.assertNull(mockCookie.getSameSite());
            withoutCount++;
         }
      }
      Assertions.assertEquals(cookiesWithSamesite.size(), withCount, "wrong number of cookies with samesite");
      Assertions.assertEquals(cookiesWithoutSameSite.size(), withoutCount, "wrong number of cookies without samesite");
   }

   /**
    * Servlet that initiates a redirect on the response.
    */
   public class TestRedirectServlet implements Servlet {

      public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
         Assertions.assertNotNull(req, "HttpServletRequest was null");
         Assertions.assertNotNull(res, "HttpServletResponse was null");
         ((HttpServletResponse) res).sendRedirect("/redirect");
      }

      public void init(ServletConfig config) throws ServletException {
      }

      public ServletConfig getServletConfig() {
         return null;
      }

      public String getServletInfo() {
         return null;
      }

      public void destroy() {
      }

   }
}
