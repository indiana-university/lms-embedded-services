package edu.iu.uits.lms.lti.service;

import com.nimbusds.jose.shaded.json.JSONObject;
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
      attrMap.put(Claims.CUSTOM, new JSONObject());

      OidcTokenUtils tokenUtils = new OidcTokenUtils(attrMap);
      String[] roles = tokenUtils.getCustomInstructureMembershipRolesRaw();
      Assertions.assertArrayEquals(new String[] {}, roles);

      Map<String, Object> customStuff = new HashMap<>();
      customStuff.put(CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY, "foobar");
      attrMap.put(Claims.CUSTOM, new JSONObject(customStuff));
      tokenUtils = new OidcTokenUtils(attrMap);
      roles = tokenUtils.getCustomInstructureMembershipRolesRaw();
      Assertions.assertArrayEquals(new String[]{"foobar"}, roles);
   }
}
