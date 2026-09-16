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

import edu.iu.uits.lms.canvas.config.CanvasEnvironmentConfiguration;
import edu.iu.uits.lms.canvas.security.CanvasOAuth2TokenInterceptor;
import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestTemplate;

/**
 * Defines the {@code CanvasRestTemplateAsUser} bean as two mutually-exclusive
 * {@code @ConditionalOnProperty} definitions under the same name, selected by
 * {@code canvas.oauth2.enabled}: the real per-user OAuth2-backed template when on, or a plain
 * alias of the shared admin {@code CanvasRestTemplate} bean when off (dark-launch). Split out of
 * {@link CanvasOAuth2ClientConfig} (which auto-discovers this class via its own
 * {@code @ComponentScan}) so this narrow, property-driven concern can be tested with
 * {@code ApplicationContextRunner} without that class's JPA/DataSource machinery.
 */
@Configuration
public class CanvasRestTemplateAsUserConfig {

   /**
    * Creates a RestTemplate bean that authorizes Canvas API calls as the currently logged-in LTI
    * user (via Canvas OAuth2 delegated access) instead of the shared admin token. Only created
    * when the host tool has opted in with {@code canvas.oauth2.enabled=true}.
    *
    * @return a RestTemplate instance carrying a CanvasOAuth2TokenInterceptor
    */
   @Bean(name = "CanvasRestTemplateAsUser")
   @ConditionalOnProperty(prefix = "canvas.oauth2", name = "enabled", havingValue = "true")
   public RestTemplate canvasRestTemplateAsUserOAuth2(OAuth2AuthorizedClientManager authorizedClientManager,
                                                        CanvasOAuth2Registration canvasOAuth2Registration) {
      RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
      CanvasEnvironmentConfiguration.configureJackson(restTemplate);
      restTemplate.getInterceptors().add(
            new CanvasOAuth2TokenInterceptor(authorizedClientManager, canvasOAuth2Registration.getRegistrationId()));
      return restTemplate;
   }

   /**
    * Dark-launch fallback: when {@code canvas.oauth2.enabled} is off (or unset), calls through
    * {@code @Qualifier("CanvasRestTemplateAsUser")} resolve to the same shared admin-token
    * RestTemplate every other Canvas call already uses, so the host tool needs no code changes to
    * fall back to pre-migration behavior. Requires the tool to also have {@code @EnableCanvasClient}
    * (which provides the admin {@code CanvasRestTemplate} bean this aliases).
    */
   @Bean(name = "CanvasRestTemplateAsUser")
   @ConditionalOnProperty(prefix = "canvas.oauth2", name = "enabled", havingValue = "false", matchIfMissing = true)
   public RestTemplate canvasRestTemplateAsUserFallback(@Qualifier("CanvasRestTemplate") RestTemplate adminRestTemplate) {
      return adminRestTemplate;
   }
}
