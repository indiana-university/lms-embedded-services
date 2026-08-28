package edu.iu.uits.lms.canvasoauth2.config;

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

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add this annotation to a {@code @Configuration} class (alongside {@code @EnableLtiClient}, which
 * this depends on for its {@code ClientRegistrationRepository} bean) to expose per-user Canvas
 * OAuth2 delegated access: the shared {@code LMS_CANVAS_OAUTH2_AUTHZ}-backed token storage, the
 * {@code @RegisteredOAuth2AuthorizedClient} controller-argument resolver, and the generic
 * consent/callback UI ({@code OAuth2CallbackController}, {@code OAuth2ConsentControllerAdvice}).
 * <p>
 * Required setup for each adopting tool:
 * <ul>
 *     <li>{@code canvas.oauth2.encryption-password} and {@code canvas.oauth2.encryption-salt} must
 *     be set (they have no defaults); without them, the
 *     {@code CanvasOAuth2AuthorizedClientRepository} bean fails to construct at startup.</li>
 *     <li>{@link #registrationIdSuffix()} is required (no default) - the tool's own
 *     {@code application.yml} registration block under
 *     {@code spring.security.oauth2.client.registration.*} (and the matching
 *     {@code spring.security.oauth2.client.provider.*} block) must use
 *     {@code "lms_canvas_oauth2_" + registrationIdSuffix()} as its map key by hand - Spring's
 *     config-binding map keys aren't placeholder-resolvable, so the annotation attribute's value
 *     can't be read back out to build that YAML key automatically. For example,
 *     {@code @EnableCanvasOAuth2Client(registrationIdSuffix = "mytool")} pairs with an
 *     {@code application.yml} registration key of {@code lms_canvas_oauth2_mytool}.</li>
 *     <li>{@link #rivetCssPathPrefix()} is required (no default) - the generic consent/connected/error
 *     pages ({@code connectCanvas.html}, {@code canvasConnected.html}, {@code canvasUserIdMissing.html})
 *     are standalone documents styled entirely with real Rivet classes (e.g. {@code rvt-button},
 *     {@code rvt-card--raised}), and they build the stylesheet URL by appending
 *     {@code /rivet-core/rivet.min.css} to this prefix. Every tool maps the {@code lms-canvas-rivet}
 *     webjar to a different prefix (e.g. {@code /app/jsrivet} vs. {@code /jsrivet}), so there's no
 *     single default that would work everywhere; pass whatever prefix this tool serves
 *     {@code rivet-core/rivet.min.css} under, with no trailing slash.</li>
 * </ul>
 * Optional: a tool can override the default consent-page wording by defining a
 * {@code @Bean @Qualifier("canvasOAuth2ConsentTextOverrides") Map<String, String>} - see
 * {@code CanvasOAuth2ConsentText}. No message-bundle/{@code MessageSource} setup is required.
 *
 * @since 7.0.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CanvasOAuth2ClientConfig.class)
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(CanvasOAuth2ClientProperties.class)
public @interface EnableCanvasOAuth2Client {

    /**
     * This tool's Canvas OAuth2 registration id suffix - the full registration id is
     * {@code "lms_canvas_oauth2_" + registrationIdSuffix}. Required (no default): a tool that
     * omits this fails to compile, rather than failing at runtime the way an unset
     * {@code spring.application.name} placeholder would have under the prior design.
     */
    String registrationIdSuffix();

    /**
     * The URL prefix this tool serves its {@code lms-canvas-rivet} webjar resources under - e.g.
     * {@code "/app/jsrivet"} for a tool mapped at {@code /app/jsrivet/**}, or {@code "/jsrivet"} for
     * one mapped at {@code /jsrivet/**}. No trailing slash. The consent/connected/error pages append
     * {@code /rivet-core/rivet.min.css} to this to build their stylesheet {@code <link>}. Required
     * (no default): those pages have no styling of their own without it - an omitted value fails to
     * compile rather than silently rendering unstyled at runtime.
     */
    String rivetCssPathPrefix();
}
