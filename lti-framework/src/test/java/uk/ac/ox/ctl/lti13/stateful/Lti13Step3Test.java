package uk.ac.ox.ctl.lti13.stateful;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestOperations;
import org.springframework.web.context.WebApplicationContext;
import uk.ac.ox.ctl.lti13.Lti13Configurer;
import uk.ac.ox.ctl.lti13.config.Lti13Configuration;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcLaunchFlowAuthenticationProvider;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OAuth2LoginAuthenticationFilter;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OptimisticAuthorizationRequestRepository;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.StateAuthorizationRequestRepository;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static uk.ac.ox.ctl.lti13.stateful.Lti13Step3Test.CustomLti13Configuration;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@SpringJUnitWebConfig(classes = {CustomLti13Configuration.class})
public class Lti13Step3Test {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private RestOperations restOperations;

    @Autowired
    private KeyPair keyPair;

    @Autowired
    @Qualifier("http")
    private AuthorizationRequestRepository authorizationRequestRepository;


    @Configuration
    public static class CustomLti13Configuration extends Lti13Configuration {

        @Autowired
        private OptimisticAuthorizationRequestRepository authorizationRequestRepository;

        @Autowired
        private RestOperations restOperations;

        @Bean(name = "http")
        AuthorizationRequestRepository authorizationRequestRepository() {
            return mock(AuthorizationRequestRepository.class);
        }

        @Bean
        StateAuthorizationRequestRepository stateAuthorizationRequestRepository() {
            return new StateAuthorizationRequestRepository(Duration.ZERO);
        }

        @Bean
        OptimisticAuthorizationRequestRepository optmisticAuthorizationRequestRepository(
                @Qualifier("http") AuthorizationRequestRepository requestRepository,
                StateAuthorizationRequestRepository stateAuthorizationRequestRepository
        ) {
            return new OptimisticAuthorizationRequestRepository(requestRepository, stateAuthorizationRequestRepository);
        }

        @Bean
        protected SecurityFilterChain configure(HttpSecurity http) throws Exception {
            http.authorizeHttpRequests(authz -> authz
                    .anyRequest().authenticated()
            );
            Lti13Configurer lti13Configurer = new Lti13Configurer() {

                @Override
                protected OidcLaunchFlowAuthenticationProvider configureAuthenticationProvider(HttpSecurity http) {
                    // This is so that we can mock out the HTTP response for the JWKs URL.
                    OidcLaunchFlowAuthenticationProvider oidcLaunchFlowAuthenticationProvider = super.configureAuthenticationProvider(http);
                    oidcLaunchFlowAuthenticationProvider.setRestOperations(restOperations);
                    return oidcLaunchFlowAuthenticationProvider;
                }

                @Override
                protected OAuth2LoginAuthenticationFilter configureLoginFilter(ClientRegistrationRepository clientRegistrationRepository, OidcLaunchFlowAuthenticationProvider oidcLaunchFlowAuthenticationProvider, OptimisticAuthorizationRequestRepository authorizationRequestRepository) {
                    // This is so that we can put a fake original request into the repository so that the state between
                    // the fake request and out test request will match.
                    OAuth2LoginAuthenticationFilter oAuth2LoginAuthenticationFilter = super.configureLoginFilter(clientRegistrationRepository, oidcLaunchFlowAuthenticationProvider, authorizationRequestRepository);
                    // Set a custom request repository
                    oAuth2LoginAuthenticationFilter.setAuthorizationRequestRepository(
                            CustomLti13Configuration.this.authorizationRequestRepository
                    );
                    return oAuth2LoginAuthenticationFilter;
                }
            };
            http.with(lti13Configurer, lti ->
                    lti.setSecurityContextRepository(new HttpSessionSecurityContextRepository()));
            return http.build();
        }
    }

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    public void testSecured() throws Exception {
        this.mockMvc.perform(get("/"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testStep3SignedToken() throws Exception {
        JWTClaimsSet claims = createClaims().build();

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = createAuthRequest().build();

        when(authorizationRequestRepository.loadAuthorizationRequest(any(HttpServletRequest.class)))
                .thenReturn(oAuth2AuthorizationRequest);
        when(authorizationRequestRepository.removeAuthorizationRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(oAuth2AuthorizationRequest);

        when(restOperations.exchange(any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jwkSet().toString(), HttpStatus.OK));
        mockMvc.perform(post("/lti/login").param("id_token", createJWT(claims)).param("state", "state").cookie(new Cookie("WORKING_COOKIES", "true")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testStep3SignedTokenAnonymous() throws Exception {
        // When it's an anonymous request there's no subject in the claims.
        JWTClaimsSet claims = createClaims().subject(null).build();

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = createAuthRequest().build();

        when(authorizationRequestRepository.loadAuthorizationRequest(any(HttpServletRequest.class)))
                .thenReturn(oAuth2AuthorizationRequest);
        when(authorizationRequestRepository.removeAuthorizationRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(oAuth2AuthorizationRequest);

        when(restOperations.exchange(any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jwkSet().toString(), HttpStatus.OK));
        mockMvc.perform(post("/lti/login").param("id_token", createJWT(claims)).param("state", "state").cookie(new Cookie("WORKING_COOKIES", "true")))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void testStep3SignedTokenNoCookie() throws Exception {
        // Here we haven't already marked the browser as having a working session based on cookies, but we 
        // do manage to retrieve the 
        JWTClaimsSet claims = createClaims().build();

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = createAuthRequest().build();

        when(authorizationRequestRepository.loadAuthorizationRequest(any(HttpServletRequest.class)))
                .thenReturn(oAuth2AuthorizationRequest);
        when(authorizationRequestRepository.removeAuthorizationRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(oAuth2AuthorizationRequest);

        when(restOperations.exchange(any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jwkSet().toString(), HttpStatus.OK));
        mockMvc.perform(post("/lti/login").param("id_token", createJWT(claims)).param("state", "state"))
                .andExpect(status().is3xxRedirection())
                .andExpect(cookie().exists("WORKING_COOKIES"));
    }

    @Test
    public void testStep3WrongVersion() throws Exception {
        // Remove the LTI Version.
        JWTClaimsSet claims = createClaims().claim(Claims.LTI_VERSION, null).build();

        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = createAuthRequest().build();

        when(authorizationRequestRepository.removeAuthorizationRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(oAuth2AuthorizationRequest);

        when(restOperations.exchange(any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jwkSet().toString(), HttpStatus.OK));
        mockMvc.perform(post("/lti/login").param("id_token", createJWT(claims)).param("state", "state"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testStep3Error() throws Exception {
        OAuth2AuthorizationRequest oAuth2AuthorizationRequest = createAuthRequest().build();

        when(authorizationRequestRepository.removeAuthorizationRequest(any(HttpServletRequest.class), any(HttpServletResponse.class)))
                .thenReturn(oAuth2AuthorizationRequest);

        when(restOperations.exchange(any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(jwkSet().toString(), HttpStatus.OK));
        // Check that when we return an actual error it gets correctly handled.
        mockMvc.perform(post("/lti/login").param("error", "problem").param("state", "state"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testStep3Empty() throws Exception {
        this.mockMvc.perform(post("/lti/login"))
                .andExpect(status().is4xxClientError());
    }

    private OAuth2AuthorizationRequest.Builder createAuthRequest() {
        Map<String, Object> additionalParameters = new HashMap<>();
        additionalParameters.put(OAuth2ParameterNames.REGISTRATION_ID, "test");
        return OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://platform.test/auth/new")
                .redirectUri("https://tool.test/lti/login")
                .scope("openid")
                .state("state")
                .additionalParameters(additionalParameters)
                .clientId("test-id");
    }

    private String createJWT(JWTClaimsSet claims) throws JOSEException {
        JWSHeader header = new JWSHeader(JWSAlgorithm.RS256);

        SignedJWT jwt = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(keyPair.getPrivate());
        jwt.sign(signer);
        return jwt.serialize();
    }

    private JWTClaimsSet.Builder createClaims() {
        return new JWTClaimsSet.Builder()
                .issuer("https://platform.test")
                .subject("subject")
                .claim("scope", "openid")
                .audience("test-id")
                .issueTime(new Date())
                .expirationTime(Date.from(Instant.now().plusSeconds(300)))
                .claim("nonce", "test-nonce")
                .claim(Claims.LTI_VERSION, "1.3.0")
                .claim(Claims.MESSAGE_TYPE, "unchecked")
                .claim(Claims.ROLES, "")
                .claim(Claims.LTI_DEPLOYMENT_ID, "1");
    }

    private JWKSet jwkSet() {
        RSAKey.Builder builder = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
                .keyUse(KeyUse.SIGNATURE)
                .algorithm(JWSAlgorithm.RS256)
                .keyID("jwt-id");
        return new JWKSet(builder.build());
    }


}
