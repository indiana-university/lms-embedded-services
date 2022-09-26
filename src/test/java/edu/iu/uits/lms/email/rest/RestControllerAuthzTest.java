package edu.iu.uits.lms.email.rest;

import edu.iu.uits.lms.email.config.EmailRestConfiguration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.NestedTestConfiguration;
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
@WebMvcTest(EmailRestController.class)
@Import(EmailRestConfiguration.class)
public class RestControllerAuthzTest {

   @MockBean
   private JwtDecoder jwtDecoder;

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
