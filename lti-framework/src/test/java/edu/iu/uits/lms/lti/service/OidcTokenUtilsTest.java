package edu.iu.uits.lms.lti.service;

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

import edu.iu.uits.lms.lti.LTIConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.ctl.lti13.lti.Claims;

import java.util.HashMap;
import java.util.Map;

import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY;

@Slf4j
public class OidcTokenUtilsTest {

   @Test
   void testRoleCleaning() {
      OidcTokenUtils tokenUtils = new OidcTokenUtils(new HashMap<>());

      // This case is for when a user would have a legit TA and Instructor role in the same course.
      Assertions.assertArrayEquals(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_TA_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE},
            tokenUtils.cleanRoleList(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE, LTIConstants.CANVAS_TA_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE}));

      // TA only
      Assertions.assertArrayEquals(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_TA_ROLE},
            tokenUtils.cleanRoleList(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_TA_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE}));

      Assertions.assertArrayEquals(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_TA_ROLE},
            tokenUtils.cleanRoleList(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_TA_ROLE}));

      // Instructor only
      Assertions.assertArrayEquals(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE},
            tokenUtils.cleanRoleList(new String[] {LTIConstants.CANVAS_ADMIN_ROLE, LTIConstants.CANVAS_INSTRUCTOR_ROLE}));

   }

   @Test
   void testEmptySplit() {
      Map<String, Object> attrMap = new HashMap<>();
      attrMap.put(Claims.CUSTOM, new HashMap());

      OidcTokenUtils tokenUtils = new OidcTokenUtils(attrMap);
      String[] roles = tokenUtils.getCustomInstructureMembershipRolesRaw();
      Assertions.assertArrayEquals(new String[] {}, roles);

      Map<String, Object> customMap = new HashMap<>();
      customMap.put(CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY, "foobar");
      attrMap.put(Claims.CUSTOM, customMap);
      tokenUtils = new OidcTokenUtils(attrMap);
      roles = tokenUtils.getCustomInstructureMembershipRolesRaw();
      Assertions.assertArrayEquals(new String[]{"foobar"}, roles);
   }
}
