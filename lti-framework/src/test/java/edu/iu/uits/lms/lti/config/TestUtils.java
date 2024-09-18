package edu.iu.uits.lms.lti.config;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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
import edu.iu.uits.lms.lti.LTIConstants;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static edu.iu.uits.lms.lti.LTIConstants.BASE_USER_AUTHORITY;

public class TestUtils extends CommonTestUtils {

   public static String defaultAuthority() {
      return BASE_USER_AUTHORITY;
   }

   public static OidcAuthenticationToken buildToken(String username, String courseId, String role) {
      Map<String, Object> customMap = new HashMap<>();
      customMap.put(LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY, courseId);

      return buildToken(username, role, new HashMap<>(), customMap);
   }

   public static OidcAuthenticationToken buildToken(String username, String authority, Map<String, Object> extraAttributes, Map<String, Object> extraCustomAttributes) {
      final String nameAttributeKey = "sub";
      Map<String, Object> attributeMap = new HashMap<>();
      attributeMap.put(nameAttributeKey, username);

      if (extraAttributes != null) {
         attributeMap.putAll(extraAttributes);
      }

      Map customMap = (Map) attributeMap.computeIfAbsent(Claims.CUSTOM, k -> new HashMap<>());
//      Set<String> keys = extraCustomAttributes.keySet();
//      for (String key : keys) {
//         customMap.put(key, extraCustomAttributes.get(key));
//      }
      customMap.putAll(extraCustomAttributes);

      OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributeMap, nameAttributeKey);
      OidcAuthenticationToken token = new OidcAuthenticationToken(oAuth2User,
            AuthorityUtils.createAuthorityList(TestUtils.defaultAuthority(), authority),
            "unit_test", "the_state");
      return token;
   }

}
