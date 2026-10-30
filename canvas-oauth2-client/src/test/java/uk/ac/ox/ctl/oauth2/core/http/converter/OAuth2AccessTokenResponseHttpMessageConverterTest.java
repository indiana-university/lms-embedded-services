package uk.ac.ox.ctl.oauth2.core.http.converter;

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

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression coverage for the Spring Framework 7 / Jackson 3 migration of this converter (it
 * previously delegated to the now-deprecated {@code MappingJackson2HttpMessageConverter}; it now
 * uses {@code JacksonJsonHttpMessageConverter} against Spring's new {@code SmartHttpMessageConverter}
 * contract). These tests exercise {@code read}/{@code write} directly to confirm that migration
 * didn't change behavior - in particular the nested-object-valued parameter that this converter
 * exists to handle in the first place (see the class-level javadoc).
 */
class OAuth2AccessTokenResponseHttpMessageConverterTest {

    private final OAuth2AccessTokenResponseHttpMessageConverter converter =
            new OAuth2AccessTokenResponseHttpMessageConverter();

    private static MockHttpInputMessage jsonInput(String json) {
        MockHttpInputMessage inputMessage = new MockHttpInputMessage(json.getBytes(StandardCharsets.UTF_8));
        inputMessage.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return inputMessage;
    }

    @Test
    void read_parsesOrdinaryCanvasAccessTokenResponse() throws IOException {
        String json = """
                {
                  "access_token": "abc123",
                  "token_type": "Bearer",
                  "expires_in": 3600,
                  "refresh_token": "refresh-xyz",
                  "scope": "url:GET|/api/v1/users/:id url:GET|/api/v1/courses"
                }
                """;

        OAuth2AccessTokenResponse response = converter.read(OAuth2AccessTokenResponse.class, jsonInput(json));

        assertEquals("abc123", response.getAccessToken().getTokenValue());
        assertEquals(OAuth2AccessToken.TokenType.BEARER, response.getAccessToken().getTokenType());
        assertEquals("refresh-xyz", response.getRefreshToken().getTokenValue());
        assertTrue(response.getAccessToken().getScopes().contains("url:GET|/api/v1/courses"));
    }

    @Test
    void read_handlesNestedObjectValuedParameter() throws IOException {
        // This is the exact shape the class-level javadoc says motivated this custom converter:
        // a parameter whose value is a JSON object rather than a scalar, which plain
        // Map<String, String> deserialization can't handle.
        String json = """
                {
                  "access_token": "abc123",
                  "token_type": "Bearer",
                  "expires_in": 3600,
                  "user": {"id": 42, "name": "Test User"}
                }
                """;

        OAuth2AccessTokenResponse response = converter.read(OAuth2AccessTokenResponse.class, jsonInput(json));

        assertEquals("abc123", response.getAccessToken().getTokenValue());
        Object userParam = response.getAdditionalParameters().get("user");
        assertTrue(userParam instanceof Map, "expected the nested 'user' object to deserialize as a Map");
        assertEquals(42, ((Map<?, ?>) userParam).get("id"));
    }

    @Test
    void read_missingAccessTokenFallsBackToLiteralNull() throws IOException {
        // Documented in OAuth2AccessTokenResponseConverter#convert: Canvas's scope=/user/authinfo
        // response omits access_token entirely.
        String json = """
                {
                  "token_type": "Bearer",
                  "expires_in": 3600,
                  "scope": "/user/authinfo"
                }
                """;

        OAuth2AccessTokenResponse response = converter.read(OAuth2AccessTokenResponse.class, jsonInput(json));

        assertEquals("null", response.getAccessToken().getTokenValue());
    }

    @Test
    void write_thenRead_roundTripsAnAccessTokenResponse() throws IOException {
        OAuth2AccessTokenResponse original = OAuth2AccessTokenResponse.withToken("round-trip-token")
                .tokenType(OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(1800)
                .scopes(Set.of("url:GET|/api/v1/users/:id"))
                .build();

        MockHttpOutputMessage outputMessage = new MockHttpOutputMessage();
        converter.write(original, MediaType.APPLICATION_JSON, outputMessage);

        MockHttpInputMessage roundTripInput = jsonInput(outputMessage.getBodyAsString(StandardCharsets.UTF_8));
        OAuth2AccessTokenResponse roundTripped = converter.read(OAuth2AccessTokenResponse.class, roundTripInput);

        assertEquals("round-trip-token", roundTripped.getAccessToken().getTokenValue());
        assertTrue(roundTripped.getAccessToken().getScopes().contains("url:GET|/api/v1/users/:id"));
    }
}
