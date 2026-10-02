package edu.iu.uits.lms.lti.controller;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.model.KeyPair;
import edu.iu.uits.lms.lti.service.KeyServiceUtil;
import edu.iu.uits.lms.lti.service.Lti13Service;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.InputStream;

import static edu.iu.uits.lms.lti.LTIConstants.JWKS_CONFIG_URI;
import static edu.iu.uits.lms.lti.LTIConstants.JWKS_PUB_CONFIG_URI;
import static edu.iu.uits.lms.lti.LTIConstants.WELL_KNOWN_ALL;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = JWKSController.class)
@ContextConfiguration(classes = {JWKSController.class, JWKSControllerTest.WellKnownSecurityConfig.class})
public class JWKSControllerTest {

   private static final String[] PRIVATE_JWK_MEMBERS = {"d", "p", "q", "dp", "dq", "qi", "oth"};

   @Autowired
   private MockMvc mvc;

   @MockitoBean
   private Lti13Service lti13Service;

   private RSAKey rsaKey;

   @BeforeEach
   public void setUp() throws Exception {
      KeyPair keyPair = new KeyPair();
      keyPair.setPrivateKey(readResource("/private_key.txt"));
      keyPair.setPublicKey(readResource("/pub_key.txt"));
      rsaKey = KeyServiceUtil.convert(keyPair);

      // Sanity check: the key the service hands out really does carry the private half,
      // so these tests would catch it leaking
      Assertions.assertTrue(rsaKey.isPrivate());

      when(lti13Service.getJKS()).thenReturn(rsaKey);
   }

   @Test
   public void jwksDoesNotExposePrivateKey() throws Exception {
      assertPublicOnly(JWKS_CONFIG_URI);
   }

   @Test
   public void pubJwksDoesNotExposePrivateKey() throws Exception {
      assertPublicOnly(JWKS_PUB_CONFIG_URI);
   }

   private void assertPublicOnly(String uri) throws Exception {
      // No authentication: Canvas fetches this anonymously via the developer key's public_jwk_url
      var result = mvc.perform(get(uri))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.kty").value("RSA"))
            .andExpect(jsonPath("$.n").value(rsaKey.getModulus().toString()))
            .andExpect(jsonPath("$.e").value(rsaKey.getPublicExponent().toString()))
            .andExpect(jsonPath("$.kid").value(rsaKey.getKeyID()))
            .andExpect(jsonPath("$.alg").value("RS256"))
            .andExpect(jsonPath("$.use").value("sig"));

      for (String member : PRIVATE_JWK_MEMBERS) {
         result.andExpect(jsonPath("$." + member).doesNotExist());
      }
   }

   private String readResource(String path) throws Exception {
      try (InputStream in = getClass().getResourceAsStream(path)) {
         return IOUtils.toString(in, "UTF-8");
      }
   }

   /**
    * The framework doesn't secure /.well-known itself; each tool's SecurityConfig permits it.
    * Mirror that here so the request goes through Spring Security the same way it does in a tool.
    */
   @TestConfiguration
   @EnableWebSecurity
   static class WellKnownSecurityConfig {
      @Bean
      public SecurityFilterChain wellKnownFilterChain(HttpSecurity http) throws Exception {
         http.securityMatcher(WELL_KNOWN_ALL)
               .authorizeHttpRequests(authz -> authz.anyRequest().permitAll());
         return http.build();
      }
   }

}
