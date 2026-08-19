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

import edu.iu.uits.lms.canvasoauth2.config.CanvasOAuth2ClientConfig;
import edu.iu.uits.lms.canvasoauth2.config.CanvasOAuth2ClientProperties;
import edu.iu.uits.lms.canvasoauth2.model.CanvasOAuth2Authz;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@DataJpaTest
@ContextConfiguration(classes = {CanvasOAuth2ClientConfig.class, CanvasOAuth2AuthzRepositoryTest.TestConfig.class})
@TestPropertySource(properties = "canvas.env=dev")
class CanvasOAuth2AuthzRepositoryTest {

    @Autowired
    private CanvasOAuth2AuthzRepository canvasOAuth2AuthzRepository;

    @MockitoBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CanvasOAuth2ClientProperties canvasOAuth2ClientProperties() {
            CanvasOAuth2ClientProperties properties = new CanvasOAuth2ClientProperties();
            properties.setEncryptionPassword("test-password");
            properties.setEncryptionSalt("deadbeef");
            return properties;
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
}
