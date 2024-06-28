package edu.iu.uits.lms.common.samesite;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.util.SessionConfig;
import org.apache.catalina.valves.ValveBase;

import java.io.IOException;
import java.util.Collection;

/**
 * This valve adds an additional session cookie for clients that ignore all cookies with
 * SameSite=None (iOS 12).
 * We can't just add another cookie to the request in this valve because the session has already
 * been linked with the request by CoyoteAdaptor earlier in the call chain. I couldn't easily find a
 * way to have something like a valve execute before CoyoteAdaptor so we link the session here
 * ourselves.
 *
 * This class doesn't use a proper parse for the Set-Cookie header as we know the code building
 * the header (Tomcat) and I don't believe the extra complexity is worth it.
 */
public class SameSiteCookieValve extends ValveBase {

    // The suffix we want to append to the normal session cookie name for old clients.
    private final String suffix;

    public SameSiteCookieValve() {
        this("-legacy");
    }

    public SameSiteCookieValve(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException {
        String sessionCookieName = SessionConfig.getSessionCookieName(request.getContext());

        // Check for legacy cookie only if we haven't already found a valid session ID.
        if (!request.isRequestedSessionIdFromURL() && !request.isRequestedSessionIdFromCookie()) {
            Cookie[] cookies = request.getCookies();
            // Check if we've got a legacy cookie here.
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ((sessionCookieName+suffix).equals(cookie.getName())) {
                        request.setRequestedSessionId(cookie.getValue());
                        request.setRequestedSessionCookie(true);
                        request.setRequestedSessionURL(false);
                        break;
                    }
                }
            }
        }

        getNext().invoke(request, response);

        // Find all the cookies set on this request and add a duplicate if they are a SameSite one.
        Collection<String> headers = response.getHeaders("Set-Cookie");
        for (String header : headers) {
            // Rather than parsing the cookie we just knock out the bit we don't want.
            String nonSameSiteHeader = header.replaceAll("; SameSite=None", "");
            // We only do this for the main session cookie set by Tomcat
            if (!header.equals(nonSameSiteHeader) && header.contains(sessionCookieName+"=")) {
                nonSameSiteHeader = nonSameSiteHeader.replaceFirst("=", suffix+"=");
                response.addHeader("Set-Cookie", nonSameSiteHeader);
            }
        }

    }
}
