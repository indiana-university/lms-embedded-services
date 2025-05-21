package edu.iu.uits.lms.email.rest;

/*-
 * #%L
 * lms-email-service
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

import edu.iu.uits.lms.email.config.EmailRestConfiguration;
import edu.iu.uits.lms.email.service.EmailService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.NestedTestConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collection;

import static edu.iu.uits.lms.email.EmailConstants.EMAILREST_PROFILE;
import static edu.iu.uits.lms.email.EmailConstants.SEND_SCOPE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration.INHERIT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NestedTestConfiguration(INHERIT)
@WebMvcTest(controllers = EmailRestController.class)
@ContextConfiguration(classes = {EmailRestController.class, EmailRestConfiguration.class})
public class RestControllerAuthzTest {

   @MockitoBean
   private JwtDecoder jwtDecoder;

   @MockitoBean
   private EmailService emailService;

   @Nested
   @ActiveProfiles({EMAILREST_PROFILE})
   class Enabled {

      @Autowired
      private MockMvc mvc;

      @Test
      public void appNoAuthnLaunch() throws Exception {
         //This is a secured endpoint and should not allow access without authn
         SecurityContextHolder.getContext().setAuthentication(null);
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
      }

      @Test
      public void restAuthnLaunch() throws Exception {
         Jwt jwt = TestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(SEND_SCOPE, "ROLE_LMS_REST_ADMINS");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token))
                     .content(TestUtils.getEmailDetailsForContentBody()))
               .andExpect(status().isOk());
      }

      @Test
      public void restAuthnLaunchWithWrongScope() throws Exception {
         Jwt jwt = TestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isForbidden());
      }
   }

   @Nested
   class Disabled {

      @Autowired
      private MockMvc mvc;

      @Test
      public void appNoAuthnLaunch() throws Exception {
         //This is a secured endpoint and should not allow access without authn
         SecurityContextHolder.getContext().setAuthentication(null);
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
      }

      @Test
      public void restAuthnLaunch() throws Exception {
         Jwt jwt = TestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(SEND_SCOPE, "ROLE_LMS_REST_ADMINS");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isNotFound());
      }

      @Test
      public void restAuthnLaunchWithWrongScope() throws Exception {
         Jwt jwt = TestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(post("/rest/email/send").with(csrf())
                     .header(HttpHeaders.USER_AGENT, TestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isForbidden());
      }
   }
}
