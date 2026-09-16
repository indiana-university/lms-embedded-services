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

import edu.iu.uits.lms.canvas.config.CanvasConfiguration;
import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link OAuth2CallbackController}. Instantiated directly (no Spring context, no
 * MockMvc) - same style as {@link OAuth2ConsentControllerAdviceTest} - since the class under test has
 * only four collaborators, all trivial to construct by hand.
 */
class OAuth2CallbackControllerTest {

    private static final String REGISTRATION_ID = "lms_canvas_oauth2_viewem";

    private CanvasConfiguration canvasConfiguration;
    private OAuth2CallbackController controller;

    @BeforeEach
    void setUp() throws Exception {
        canvasConfiguration = new CanvasConfiguration();
        canvasConfiguration.setBaseUrl("https://canvas.test");

        OAuth2ConsentControllerAdvice advice = new OAuth2ConsentControllerAdvice();
        ReflectionTestUtils.setField(advice, "canvasOAuth2Registration",
                new CanvasOAuth2Registration("viewem", "/app/jsrivet"));
        ReflectionTestUtils.setField(advice, "canvasOAuth2ConsentText", new CanvasOAuth2ConsentText(null));

        controller = new OAuth2CallbackController();
        ReflectionTestUtils.setField(controller, "canvasConfiguration", canvasConfiguration);
        ReflectionTestUtils.setField(controller, "oAuth2ConsentControllerAdvice", advice);
        ReflectionTestUtils.setField(controller, "canvasOAuth2ConsentText", new CanvasOAuth2ConsentText(null));
        ReflectionTestUtils.setField(controller, "canvasOAuth2Registration",
                new CanvasOAuth2Registration("viewem", "/app/jsrivet"));
    }

    @Test
    void connected_noErrorNoPendingCourse_rendersCanvasConnectedWithBareBaseUrl() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, null);

        assertEquals("canvasConnected", mav.getViewName());
        assertEquals("https://canvas.test", mav.getModel().get("returnUrl"));
        assertEquals("/app/jsrivet", mav.getModel().get("rivetCssPathPrefix"));
    }

    @Test
    void connected_noErrorWithPendingCourseOnly_rendersCanvasConnectedWithCourseUrl_andClearsSessionAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY, "1234");

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, null);

        assertEquals("canvasConnected", mav.getViewName());
        // No pending tool id (e.g. an LTI registration that hasn't been updated to request
        // $Canvas.externalTool.id yet) - falls back to the course page, same as before this tool-id
        // relaunch feature existed.
        assertEquals("https://canvas.test/courses/1234", mav.getModel().get("returnUrl"));
        assertNull(request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
    }

    @Test
    void connected_noErrorWithPendingCourseAndToolId_rendersCanvasConnectedWithExternalToolRelaunchUrl_andClearsBothSessionAttributes() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY, "1234");
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY, "789");

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, null);

        assertEquals("canvasConnected", mav.getViewName());
        assertEquals("https://canvas.test/courses/1234/external_tools/789", mav.getModel().get("returnUrl"));
        assertNull(request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        assertNull(request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
    }

    @Test
    void connected_noErrorWithPendingLaunchPath_rendersCanvasConnectedWithLaunchPath_andClearsSessionAttribute() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY, "/app/launch");

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, null);

        assertEquals("canvasConnected", mav.getViewName());
        assertEquals("/app/launch", mav.getModel().get("returnUrl"));
        assertNull(request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void connected_pendingLaunchPathTakesPriorityOverCourseAndToolId() {
        // The captured launch path needs no Canvas-side configuration and works for any tool's own
        // launch endpoint - it should win even when the Canvas relaunch URL could also be built.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY, "/app/launch");
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY, "1234");
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY, "789");

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, null);

        assertEquals("/app/launch", mav.getModel().get("returnUrl"));
    }

    @Test
    void connected_errorParamPresent_rendersConnectInterstitialInsteadOfCanvasConnected() {
        // Simulates OAuth2AuthorizationCodeGrantFilter's own redirect-back-to-itself behavior when
        // Canvas rejects the authorization code: it never reaches this controller with a successful
        // exchange, it re-enters with error/error_description/error_uri query parameters instead.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("error_description", "The authorization code has expired");

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, "invalid_grant");

        assertEquals("connectCanvas", mav.getViewName());
        assertEquals("/oauth2/authorization/" + REGISTRATION_ID, mav.getModel().get("authorizationUri"));
    }

    @Test
    void connected_errorParamPresent_usesInjectedRegistrationIdNotPathVariable() {
        // Spring Security doesn't guarantee the {registrationId} path segment resolved to a real
        // ClientRegistration before reaching this mapping, so the "connect" interstitial must be built
        // from the injected CanvasOAuth2Registration bean rather than the raw, potentially
        // attacker-influenced path variable - this test uses a deliberately mismatched path value to
        // pin that down.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("error_description", "The authorization code has expired");

        ModelAndView mav = controller.connected("not_a_real_registration", request, "invalid_grant");

        assertEquals("connectCanvas", mav.getViewName());
        assertEquals("/oauth2/authorization/" + REGISTRATION_ID, mav.getModel().get("authorizationUri"));
    }

    @Test
    void connected_errorParamPresent_doesNotConsumePendingCourseOrToolIdSession() {
        // A failed attempt shouldn't discard the course/tool/launch path the user is trying to get
        // back to - the next, hopefully successful, attempt still needs them to build the right
        // returnUrl.
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY, "5678");
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY, "789");
        request.getSession(true).setAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY, "/app/launch");
        request.setParameter("error", "access_denied");

        controller.connected(REGISTRATION_ID, request, "access_denied");

        assertEquals("5678",
                request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_COURSE_ID_SESSION_KEY));
        assertEquals("789",
                request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_TOOL_ID_SESSION_KEY));
        assertEquals("/app/launch",
                request.getSession(false).getAttribute(OAuth2ConsentControllerAdvice.PENDING_LAUNCH_PATH_SESSION_KEY));
    }

    @Test
    void connected_blankErrorParam_isTreatedAsSuccessNotError() {
        // Spring's @RequestParam(required = false) can bind an empty string (e.g. "?error=") rather
        // than null; StringUtils.hasText(...) must treat that the same as "no error".
        MockHttpServletRequest request = new MockHttpServletRequest();

        ModelAndView mav = controller.connected(REGISTRATION_ID, request, "  ");

        assertEquals("canvasConnected", mav.getViewName());
    }
}
