package edu.iu.uits.lms.canvasoauth2.repository;

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
import edu.iu.uits.lms.canvasoauth2.config.CanvasOAuth2ClientProperties;
import edu.iu.uits.lms.canvasoauth2.config.EnableCanvasOAuth2Client;
import edu.iu.uits.lms.canvasoauth2.model.CanvasOAuth2Authz;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ContextConfiguration(classes = {CanvasOAuth2AuthzRepositoryTest.TestConfig.class})
@TestPropertySource(properties = "canvas.env=dev")
class CanvasOAuth2AuthzRepositoryTest {

    @Autowired
    private CanvasOAuth2AuthzRepository canvasOAuth2AuthzRepository;

    @Autowired
    private CanvasOAuth2Registration canvasOAuth2Registration;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * Plain {@code @Configuration}, not {@code @TestConfiguration}: with
     * {@code CanvasOAuth2ClientConfig} no longer listed directly in {@code @ContextConfiguration}
     * above, this nested class is the only entry in that classes array. If it were
     * {@code @TestConfiguration}, {@code SpringBootTestContextBootstrapper} would treat the array as
     * containing no "real" configuration and fall back to searching for a {@code @SpringBootConfiguration}
     * class - which doesn't exist in this library module - failing the context with
     * "Unable to find a @SpringBootConfiguration". This mirrors the same plain-{@code @Configuration}
     * choice in {@code CanvasOAuth2RegistrationTest} (viewem).
     */
    @Configuration
    @EnableCanvasOAuth2Client(registrationIdSuffix = "test")
    static class TestConfig {
        @Bean
        public CanvasOAuth2ClientProperties canvasOAuth2ClientProperties() {
            CanvasOAuth2ClientProperties properties = new CanvasOAuth2ClientProperties();
            properties.setEncryptionPassword("test-password");
            properties.setEncryptionSalt("deadbeef");
            return properties;
        }

        /**
         * {@code @EnableCanvasOAuth2Client}'s own {@code @Import(CanvasOAuth2ClientConfig.class)}
         * (via this class's annotation above) also pulls in {@code OAuth2CallbackController} and
         * {@code OAuth2ConsentControllerAdvice} through its {@code @ComponentScan}, neither of which
         * this JPA-layer test exercises - but Spring still needs their dependencies satisfied to
         * boot the context. A plain no-arg instance is enough; nothing here reads its properties.
         */
        @Bean
        public CanvasConfiguration canvasConfiguration() {
            return new CanvasConfiguration();
        }
    }

    @Test
    void savesAndFindsByRegistrationEnvUser() {
        CanvasOAuth2Authz authz = new CanvasOAuth2Authz();
        authz.setRegistrationId("lms_canvas_oauth2");
        authz.setEnv("dev");
        authz.setCanvasUserId("12345");
        // placeholder/fake values, not real Canvas OAuth2 tokens
        authz.setAccessToken("enc-access-token");
        authz.setRefreshToken("enc-refresh-token");
        authz.setTokenType("bearer");
        authz.setScopes("url:GET|/api/v1/courses/:course_id/users");
        authz.setExpiresAt(new Date());

        canvasOAuth2AuthzRepository.save(authz);

        CanvasOAuth2Authz found = canvasOAuth2AuthzRepository.findByRegistrationEnvUser("lms_canvas_oauth2", "dev", "12345");

        assertNotNull(found);
        assertEquals("lms_canvas_oauth2", found.getRegistrationId());
        assertEquals("dev", found.getEnv());
        assertEquals("12345", found.getCanvasUserId());
        assertEquals("enc-access-token", found.getAccessToken());
        assertEquals("enc-refresh-token", found.getRefreshToken());
        assertEquals("bearer", found.getTokenType());
        assertEquals("url:GET|/api/v1/courses/:course_id/users", found.getScopes());
        assertNotNull(found.getCreated());
        assertNotNull(found.getModified());
    }

    @Test
    void findByRegistrationEnvUserReturnsNullWhenNoMatch() {
        CanvasOAuth2Authz found = canvasOAuth2AuthzRepository.findByRegistrationEnvUser("lms_canvas_oauth2", "dev", "does-not-exist");
        assertNull(found);
    }

    /**
     * Permanent guard against cross-test contamination in {@code CanvasOAuth2ClientConfig}'s
     * {@code @ComponentScan}: this test's own {@link TestConfig} - not some other class on the
     * classpath that also carries {@code @EnableCanvasOAuth2Client} - must be the class whose
     * {@code registrationIdSuffix} metadata reaches {@code CanvasOAuth2ClientConfig#setImportMetadata}.
     * {@link TestConfig} declares {@code registrationIdSuffix = "test"}, so the resulting
     * {@link CanvasOAuth2Registration} bean's id must be {@code "lms_canvas_oauth2_test"}. If some
     * other fixture (e.g. a test-only dummy elsewhere in the {@code edu.iu.uits.lms.canvasoauth2}
     * package tree) were ever re-discovered by that component scan and mistaken for the importing
     * class, this bean would be built with a different suffix and this assertion would fail.
     */
    @Test
    void canvasOAuth2RegistrationUsesThisTestsOwnRegistrationIdSuffix() {
        assertEquals("lms_canvas_oauth2_test", canvasOAuth2Registration.getRegistrationId());
    }
}
