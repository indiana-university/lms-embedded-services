package edu.iu.uits.lms.lti.service;

import com.nimbusds.jose.shaded.json.JSONObject;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.Map;

import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_USER_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY;

public class OidcTokenUtils {



   /*
           return Arrays.asList(CUSTOM_REDIRECT_URL_PROP, CUSTOM_CANVAS_COURSE_ID,
              CUSTOM_CANVAS_USER_ID, CUSTOM_CANVAS_USER_LOGIN_ID, BasicLTIConstants.LIS_PERSON_NAME_FAMILY,
              BasicLTIConstants.LIS_PERSON_NAME_GIVEN, BasicLTIConstants.LIS_PERSON_SOURCEDID, BasicLTIConstants.ROLES);
    */

   public static String getCourseId(OidcAuthenticationToken token) {
      return getCustomValue(token, CUSTOM_CANVAS_COURSE_ID_KEY);
   }

   public static String getUserId(OidcAuthenticationToken token) {
      return getCustomValue(token, CUSTOM_CANVAS_USER_ID_KEY);
   }

   public static String getUserLoginId(OidcAuthenticationToken token) {
      return getCustomValue(token, CUSTOM_CANVAS_USER_LOGIN_ID_KEY);
   }

//   public static String getSisUserId() {
//
//   }

   public static String getCustomValue(OidcAuthenticationToken token, String key) {
      Map<String, Object> attrMap = token.getPrincipal().getAttributes();
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CUSTOM);
      return jsonObj.getAsString(key);
   }
}
