package edu.iu.uits.lms.canvasoauth2;

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

/**
 * This tool's own Canvas OAuth2 registration id - {@code "lms_canvas_oauth2_" + registrationIdSuffix},
 * where {@code registrationIdSuffix} comes from {@code @EnableCanvasOAuth2Client}'s required
 * {@code registrationIdSuffix} attribute (read via {@code CanvasOAuth2ClientConfig}'s
 * {@code ImportAware} wiring, not a Spring property). Each adopting tool gets its own registration id
 * (and its own Canvas Developer Key) rather than sharing one across the whole tool suite; see
 * docs/superpowers/specs/2026-08-20-canvas-oauth2-per-tool-registration-design.md and
 * docs/superpowers/specs/2026-08-21-canvas-oauth2-explicit-registration-suffix-design.md.
 * <p>
 * The tool's own {@code application.yml} still has to spell out the same literal by hand as the
 * registration-id key under {@code spring.security.oauth2.client.registration.*} - Spring's
 * config-binding map keys aren't placeholder-resolvable, so the annotation attribute's value can't
 * be read back out to build that YAML key automatically.
 * <p>
 * Also carries {@code rivetCssPathPrefix} - {@code @EnableCanvasOAuth2Client}'s other required
 * attribute - so {@code OAuth2ConsentControllerAdvice} and {@code OAuth2CallbackController} have a
 * single bean to pull both per-tool values from when building the consent/connected/error page
 * models.
 */
public class CanvasOAuth2Registration {

    private final String registrationId;
    private final String rivetCssPathPrefix;

    /**
     * Convenience constructor for callers that only care about the registration id (most existing
     * tests). {@code rivetCssPathPrefix} is left {@code null} - fine as long as nothing built from
     * this instance renders one of the consent/connected/error pages.
     */
    public CanvasOAuth2Registration(String registrationIdSuffix) {
        this(registrationIdSuffix, null);
    }

    public CanvasOAuth2Registration(String registrationIdSuffix, String rivetCssPathPrefix) {
        this.registrationId = "lms_canvas_oauth2_" + registrationIdSuffix;
        this.rivetCssPathPrefix = rivetCssPathPrefix;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public String getRivetCssPathPrefix() {
        return rivetCssPathPrefix;
    }
}
