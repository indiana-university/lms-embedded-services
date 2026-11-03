package edu.iu.uits.lms.canvasoauth2.controller;

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

import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import edu.iu.uits.lms.canvasoauth2.security.CanvasOAuth2AuthorizedClientRepository;
import edu.iu.uits.lms.lti.LTIConstants;
import edu.iu.uits.lms.lti.config.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link OAuth2ConsentControllerAdvice}. Instantiated directly (no Spring context)
 * with its collaborators set via reflection, same style as {@link OAuth2CallbackControllerTest}.
 */
class OAuth2ConsentControllerAdviceTest {

    private static final String REGISTRATION_ID = "lms_canvas_oauth2_viewem";

    private OAuth2ConsentControllerAdvice advice;
    private CanvasOAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository;

    @BeforeEach
    void setUp() throws Exception {
        advice = new OAuth2ConsentControllerAdvice();
        ReflectionTestUtils.setField(advice, "canvasOAuth2Registration",
                new CanvasOAuth2Registration("viewem", "/app/jsrivet"));
        ReflectionTestUtils.setField(advice, "canvasOAuth2ConsentText", new CanvasOAuth2ConsentText(null));

        // Defaults to "resolvable" so every existing test below - which exercises concerns unrelated
        // to Canvas-user-id resolution (course/tool id memory, launch path capture) - is unaffected by
        // the fail-fast check. Tests specifically covering that check override this stub themselves.
        canvasOAuth2AuthorizedClientRepository = mock(CanvasOAuth2AuthorizedClientRepository.class);
        when(canvasOAuth2AuthorizedClientRepository.hasResolvableCanvasUserId(any())).thenReturn(true);
        ReflectionTestUtils.setField(advice, "canvasOAuth2AuthorizedClientRepository", canvasOAuth2AuthorizedClientRepository);
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void handleClientAuthorizationRequired_returnsConnectViewAndRemembersCourse() {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "course-123", LTIConstants.INSTRUCTOR_AUTHORITY);
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/app/launch");
        ClientAuthorizationRequiredException exception = new ClientAuthorizationRequiredException("some-registration-id");

        ModelAndView mav = advice.handleClientAuthorizationRequired(request, exception);

        assertEquals("connectCanvas", mav.getViewName());
        assertEquals("/oauth2/authorization/some-registration-id", mav.getModel().get("authorizationUri"));
        assertEquals("/app/jsrivet", mav.getModel().get("rivetCssPathPrefix"));
        assertEquals("course-123", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        // TestUtils.buildToken's 3-arg overload doesn't populate canvas_external_tool_id - this proves
        // the older/simpler registration shape still works and doesn't leave a stale tool id behind.
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
        // The captured launch path doesn't depend on any token claim - it's whatever request this
        // tool's own launch endpoint happens to be, regardless of tool.
        assertEquals("/app/launch", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void handleClientAuthorizationRequired_requestHasQueryString_launchPathIncludesIt() {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "course-123", LTIConstants.INSTRUCTOR_AUTHORITY);
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/some/other/tools/launch-path");
        request.setQueryString("foo=bar&baz=qux");
        ClientAuthorizationRequiredException exception = new ClientAuthorizationRequiredException("some-registration-id");

        advice.handleClientAuthorizationRequired(request, exception);

        assertEquals("/some/other/tools/launch-path?foo=bar&baz=qux",
                request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void handleClientAuthorizationRequired_tokenHasToolId_remembersToolIdToo() {
        Map<String, Object> customAttributes = new HashMap<>();
        customAttributes.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, "course-123");
        customAttributes.put(LTIConstants.CUSTOM_CANVAS_EXTERNAL_TOOL_ID_KEY, "tool-789");
        OidcAuthenticationToken token = TestUtils.buildToken("userId", LTIConstants.INSTRUCTOR_AUTHORITY,
                new HashMap<>(), customAttributes);
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/app/launch");
        ClientAuthorizationRequiredException exception = new ClientAuthorizationRequiredException("some-registration-id");

        advice.handleClientAuthorizationRequired(request, exception);

        assertEquals("course-123", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        assertEquals("tool-789", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
    }

    @Test
    void handleOAuth2AuthorizationException_returnsConnectViewAndRemembersCourse() {
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "course-456", LTIConstants.INSTRUCTOR_AUTHORITY);
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        OAuth2AuthorizationException exception = new OAuth2AuthorizationException(new OAuth2Error("invalid_token"));

        ModelAndView mav = advice.handleOAuth2AuthorizationException(request, exception);

        assertEquals("connectCanvas", mav.getViewName());
        assertEquals("/oauth2/authorization/" + REGISTRATION_ID, mav.getModel().get("authorizationUri"));
        assertEquals("course-456", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
    }

    @Test
    void handleOAuth2AuthorizationException_doesNotOverwriteAlreadyRememberedLaunchPath() {
        // This handler runs on the /login/oauth2/code/... callback request itself, whose own URI must
        // never clobber the launch path captured earlier by handleClientAuthorizationRequired.
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "course-456", LTIConstants.INSTRUCTOR_AUTHORITY);
        SecurityContextHolder.getContext().setAuthentication(token);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/login/oauth2/code/" + REGISTRATION_ID);
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY, "/app/launch");
        OAuth2AuthorizationException exception = new OAuth2AuthorizationException(new OAuth2Error("invalid_token"));

        advice.handleOAuth2AuthorizationException(request, exception);

        assertEquals("/app/launch",
                request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void handleClientAuthorizationRequired_nonOidcPrincipal_stillReturnsConnectViewButDoesNotRememberCourse() {
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken("someUser", "creds"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/app/launch");
        ClientAuthorizationRequiredException exception = new ClientAuthorizationRequiredException("some-registration-id");

        ModelAndView mav = advice.handleClientAuthorizationRequired(request, exception);

        assertEquals("connectCanvas", mav.getViewName());
        assertEquals("/oauth2/authorization/some-registration-id", mav.getModel().get("authorizationUri"));
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
        // The launch path is captured from the request itself, independent of principal type.
        assertEquals("/app/launch", request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void handleClientAuthorizationRequired_noResolvableCanvasUserId_returnsMissingUserIdViewWithoutRedirectingToCanvas() {
        // This is the actual reported bug scenario: an LTI launch (e.g. missing the
        // custom_canvas_user_id custom claim) that CanvasOAuth2AuthorizedClientRepository can never
        // resolve a Canvas user id from. Sending it through the Canvas authorize round-trip is
        // pointless - saveAuthorizedClient would just no-op - so this must fail fast instead.
        OidcAuthenticationToken token = TestUtils.buildToken("userId", "course-123", LTIConstants.INSTRUCTOR_AUTHORITY);
        SecurityContextHolder.getContext().setAuthentication(token);
        when(canvasOAuth2AuthorizedClientRepository.hasResolvableCanvasUserId(token)).thenReturn(false);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/app/launch");
        ClientAuthorizationRequiredException exception = new ClientAuthorizationRequiredException("some-registration-id");

        ModelAndView mav = advice.handleClientAuthorizationRequired(request, exception);

        assertEquals("canvasUserIdMissing", mav.getViewName());
        assertEquals(canvasOAuth2ConsentTextDefault(CanvasOAuth2ConsentText.MISSING_CANVAS_USER_ID_HEADING), mav.getModel().get("heading"));
        assertEquals(canvasOAuth2ConsentTextDefault(CanvasOAuth2ConsentText.MISSING_CANVAS_USER_ID_INSTRUCTIONS), mav.getModel().get("instructions"));
        assertEquals("/app/jsrivet", mav.getModel().get("rivetCssPathPrefix"));
        // Nothing about the launch is remembered - there's no point sending this user to Canvas at all.
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
        assertNull(request.getSession().getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    private String canvasOAuth2ConsentTextDefault(String key) {
        return new CanvasOAuth2ConsentText(null).get(key);
    }
}
