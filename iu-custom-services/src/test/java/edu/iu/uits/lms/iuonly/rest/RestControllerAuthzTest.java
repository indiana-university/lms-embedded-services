package edu.iu.uits.lms.iuonly.rest;

/*-
 * #%L
 * lms-canvas-iu-custom-services
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

import edu.iu.uits.lms.common.test.CommonTestUtils;
import edu.iu.uits.lms.iuonly.IuCustomConstants;
import edu.iu.uits.lms.iuonly.config.IuCustomRestConfiguration;
import edu.iu.uits.lms.iuonly.repository.LmsBatchEmailRepository;
import edu.iu.uits.lms.iuonly.services.rest.BatchEmailRestController;
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

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.context.NestedTestConfiguration.EnclosingConfiguration.INHERIT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@NestedTestConfiguration(INHERIT)
@WebMvcTest(controllers = {BatchEmailRestController.class})
@ContextConfiguration(classes = {BatchEmailRestController.class, IuCustomRestConfiguration.class})
public class RestControllerAuthzTest {

   @MockitoBean
   private JwtDecoder jwtDecoder;

   @MockitoBean
   public LmsBatchEmailRepository lmsBatchEmailRepository;

   @Nested
   @ActiveProfiles({IUCUSTOMREST_PROFILE})
   class Enabled {

      @Autowired
      private MockMvc mvc;

      @Test
      public void appNoAuthnLaunch() throws Exception {
         //This is a secured endpoint and should not allow access without authn
         SecurityContextHolder.getContext().setAuthentication(null);
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
      }

      @Test
      public void restAuthnLaunch() throws Exception {
         Jwt jwt = CommonTestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(IuCustomConstants.READ_SCOPE, "ROLE_LMS_REST_ADMINS");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isOk());
      }

      @Test
      public void restAuthnLaunchWithWrongScope() throws Exception {
         Jwt jwt = CommonTestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
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
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON))
               .andExpect(status().isUnauthorized());
      }

      @Test
      public void restAuthnLaunch() throws Exception {
         Jwt jwt = CommonTestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList(IuCustomConstants.READ_SCOPE, "ROLE_LMS_REST_ADMINS");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         // This is a secured endpoint and should not allow access without authn.
         // But, since we have authn, we get through security to find that the endpoint isn't available
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isNotFound());
      }

      @Test
      public void restAuthnLaunchWithWrongScope() throws Exception {
         Jwt jwt = CommonTestUtils.createJwtToken("asdf");

         Collection<GrantedAuthority> authorities = AuthorityUtils.createAuthorityList("SCOPE_read", "ROLE_NONE_YA");
         JwtAuthenticationToken token = new JwtAuthenticationToken(jwt, authorities);

         //This is a secured endpoint and should not allow access without authn
         mvc.perform(get("/rest/iu/batchemail/all")
                     .header(HttpHeaders.USER_AGENT, CommonTestUtils.defaultUseragent())
                     .contentType(MediaType.APPLICATION_JSON)
                     .with(authentication(token)))
               .andExpect(status().isForbidden());
      }
   }
}
