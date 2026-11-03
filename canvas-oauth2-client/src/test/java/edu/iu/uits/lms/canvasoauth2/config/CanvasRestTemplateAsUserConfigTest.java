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

import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * Verifies the real {@code @ConditionalOnProperty} resolution between the two
 * {@code CanvasRestTemplateAsUser} bean definitions - no Spring context in
 * {@link CanvasOAuth2ClientConfigTest} exercises this, since that test calls bean methods
 * directly rather than booting a context.
 */
class CanvasRestTemplateAsUserConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
          .withUserConfiguration(CanvasRestTemplateAsUserConfig.class)
          .withBean(OAuth2AuthorizedClientManager.class, () -> mock(OAuth2AuthorizedClientManager.class))
          .withBean(CanvasOAuth2Registration.class, () -> new CanvasOAuth2Registration("test", "/jsrivet"));

    @Test
    void oauth2BackedRestTemplateWinsWhenEnabled() {
        contextRunner
              .withPropertyValues("canvas.oauth2.enabled=true")
              .withBean("CanvasRestTemplate", RestTemplate.class, RestTemplate::new)
              .run(context -> {
                  RestTemplate restTemplate = context.getBean("CanvasRestTemplateAsUser", RestTemplate.class);
                  assertTrue(restTemplate.getInterceptors().stream()
                        .anyMatch(interceptor -> interceptor.getClass().getSimpleName().equals("CanvasOAuth2TokenInterceptor")));
              });
    }

    @Test
    void adminRestTemplateWinsWhenDisabled() {
        RestTemplate adminRestTemplate = new RestTemplate();
        contextRunner
              .withPropertyValues("canvas.oauth2.enabled=false")
              .withBean("CanvasRestTemplate", RestTemplate.class, () -> adminRestTemplate)
              .run(context ->
                    assertSame(adminRestTemplate, context.getBean("CanvasRestTemplateAsUser", RestTemplate.class)));
    }

    @Test
    void adminRestTemplateWinsWhenPropertyMissing() {
        RestTemplate adminRestTemplate = new RestTemplate();
        contextRunner
              .withBean("CanvasRestTemplate", RestTemplate.class, () -> adminRestTemplate)
              .run(context ->
                    assertSame(adminRestTemplate, context.getBean("CanvasRestTemplateAsUser", RestTemplate.class)));
    }
}
