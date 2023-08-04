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

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import edu.iu.uits.lms.lti.LTIConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.lti.LTIConstants.CLAIMS_EMAIL_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CLAIMS_FAMILY_NAME_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CLAIMS_FULL_NAME_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CLAIMS_GIVEN_NAME_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CLAIMS_PLATFORM_GUID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_COURSE_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_MEMBERSHIP_ROLES_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_USER_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_USER_LOGIN_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_CANVAS_USER_SIS_ID_KEY;
import static edu.iu.uits.lms.lti.LTIConstants.CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY;

@Slf4j
public class OidcTokenUtils {

   private Map<String, Object> attrMap;

   /**
    * Constructor to initialize using the token.  Will typically be used by tool controllers.
    * @param token
    */
   public OidcTokenUtils(OidcAuthenticationToken token) {
      this.attrMap = token.getPrincipal().getAttributes();
   }

   /**
    * Constructor to initialize with an attribute map
    * @param attributeMap
    */
   public OidcTokenUtils(Map<String, Object> attributeMap) {
      this.attrMap = attributeMap;
   }

   /**
    * Get the Canvas Course ID out of the custom claims
    * @return Canvas Course ID
    */
   public String getCourseId() {
      return getCustomValue(CUSTOM_CANVAS_COURSE_ID_KEY);
   }

   /**
    * Get the Canvas User ID out of the custom claims
    * @return Canvas User ID
    */
   public String getUserId() {
      return getCustomValue(CUSTOM_CANVAS_USER_ID_KEY);
   }

   /**
    * Get the Canvas User Login ID out of the custom claims
    * @return Canvas User Login ID
    */
   public String getUserLoginId() {
      return getCustomValue(CUSTOM_CANVAS_USER_LOGIN_ID_KEY);
   }

   /**
    * Get the Canvas User SIS ID out of the custom claoms
    * @return Canvas User SIS ID
    */
   public String getSisUserId() {
      return getCustomValue(CUSTOM_CANVAS_USER_SIS_ID_KEY);
   }

   /**
    * Get the Person Family name out of the claim
    * @return Person Family name
    */
   public String getPersonFamilyName() {
      String name = (String) attrMap.get(CLAIMS_FAMILY_NAME_KEY);
      return name;
   }

   /**
    * Get the Person Given name out of the claim
    * @return Person Given name
    */
   public String getPersonGivenName() {
      String name = (String) attrMap.get(CLAIMS_GIVEN_NAME_KEY);
      return name;
   }

   /**
    * Get the full Name out of the claom
    * @return Name
    */
   public String getPersonFullName() {
      String name = (String) attrMap.get(CLAIMS_FULL_NAME_KEY);
      return name;
   }

   /**
    * Get the Email out of the claim
    * @return Email
    */
   public String getPersonEmail() {
      String name = (String) attrMap.get(CLAIMS_EMAIL_KEY);
      return name;
   }

   /**
    * Get values from the Roles claim.
    * FYI, these contain ALL roles for the user, not just the ones for the launched context
    * @return Roles
    */
   public String[] getAllRoles() {
      JSONArray jsonObj = (JSONArray) attrMap.get(Claims.ROLES);
      return jsonObj.toArray(String[]::new);
   }

   /**
    * Get Canvas Membership Roles from the Custom claim (More like Canvas Enrollment Types)
    * @return Canvas Membership Roles
    */
   public String[] getCustomCanvasMembershipRoles() {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CUSTOM);
      String roleStr = jsonObj.getAsString(CUSTOM_CANVAS_MEMBERSHIP_ROLES_KEY);
      return null2Empty(StringUtils.split(roleStr, ","));
   }

   /**
    * Get (unfiltered) Instructure Membership Roles from the Custom claim
    * @return Instructure Membership Roles
    */
   public String[] getCustomInstructureMembershipRolesRaw() {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CUSTOM);
      String roleStr = jsonObj.getAsString(CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY);
      return null2Empty(StringUtils.split(roleStr, ","));
   }

   /**
    * Get filtered Instructure Membership Roles from the Custom claim
    * @return Filtered Instructure Membership Roles
    */
   public String[] getCustomInstructureMembershipRoles() {
      String[] rawRoles = getCustomInstructureMembershipRolesRaw();
      return cleanRoleList(rawRoles);
   }

   /**
    * Get the GUID out of the Platform claim
    * @return Platform GUID
    */
   public String getPlatformGuid() {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.PLATFORM_INSTANCE);
      return jsonObj.getAsString(CLAIMS_PLATFORM_GUID_KEY);
   }

   /**
    * Get the specified key out of the Custom claim
    * @param key Key
    * @return Value of given key from the Custom claim
    */
   public String getCustomValue(String key) {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CUSTOM);
      return jsonObj.getAsString(key);
   }

   /**
    * Get the specified key out of the Custom claim
    * @param key Key
    * @return String[] of given key from the Custom claim.  Returns an empty array if key is not found.
    */
   public String[] getCustomArray(String key) {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CUSTOM);
      JSONArray jsonArray = (JSONArray) jsonObj.get(key);
      return jsonArray != null ? jsonArray.toArray(String[]::new) : new String[]{};
   }

   /**
    * Get the specified key out of the Context claim
    * @param key Key
    * @return Value of given key from the Context claim
    */
   public String getContextValue(String key) {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.CONTEXT);
      return jsonObj.getAsString(key);
   }

   /**
    * Get the specified key out of the LIS claim
    * @param key Key
    * @return Value of given key from the LIS claim
    */
   public String getLisValue(String key) {
      JSONObject jsonObj = (JSONObject) attrMap.get(Claims.LIS);
      return jsonObj.getAsString(key);
   }

   /**
    * Get the specified key out of the root claim
    * @param key Key
    * @return Value of given key from the root claim
    */
   public String getClaimValue(String key) {
      String claimValue = (String) attrMap.get(key);
      return claimValue;
   }

   /**
    * According to the LTI spec, when a user has the TA role, since it is a sub-type of Instructor, both roles are sent.
    * This makes it impossible to distinguish between the two.  So, this will strip out the "extra" occurrence of the Instructor role.
    * It should still support legit cases where a user is a TA in one section and an Instructor in a different section.
    * @param listArray Input array of roles
    * @return Filtered array or roles
    */
   protected String[] cleanRoleList(String[] listArray) {
      List<String> list = new ArrayList<>(Arrays.asList(listArray));
      log.debug("Original list: {}", list);

      // Group by the counts for each role
      Map<Object, Long> collect = list.stream().collect(Collectors.groupingBy(l -> l, Collectors.counting()));
      log.debug("Grouped: {}", collect);

      // If there is at least 1 TA role and 1 Instructor role, remove the instructor role
      if (collect.containsKey(LTIConstants.CANVAS_TA_ROLE) && collect.get(LTIConstants.CANVAS_TA_ROLE) > 0 &&
            collect.containsKey(LTIConstants.CANVAS_INSTRUCTOR_ROLE) && collect.get(LTIConstants.CANVAS_INSTRUCTOR_ROLE) > 0) {
         log.info("Remove a {}...", LTIConstants.CANVAS_INSTRUCTOR_ROLE);
         list.remove(LTIConstants.CANVAS_INSTRUCTOR_ROLE);
      }
      log.debug("Final list: {}", list);
      return list.toArray(String[]::new);
   }

   /**
    * Return the String[], or an empty array if null
    * @param input
    * @return
    */
   private String[] null2Empty(String[] input) {
      return input == null ? new String[] {} : input;
   }

}
