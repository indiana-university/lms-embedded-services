package edu.iu.uits.lms.lti.service;

import com.nimbusds.jose.shaded.json.JSONArray;
import edu.iu.uits.lms.lti.LTIConstants;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import uk.ac.ox.ctl.lti13.lti.Role;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static uk.ac.ox.ctl.lti13.lti.Claims.ROLES;

public class LmsDefaultGrantedAuthoritiesMapper implements GrantedAuthoritiesMapper {
   @Override
   public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
      List<GrantedAuthority> remappedAuthorities = new ArrayList<>();
      remappedAuthorities.addAll(authorities);
      for (GrantedAuthority authority: authorities) {
         OidcUserAuthority userAuth = (OidcUserAuthority) authority;
         Map<String, Object> attributeMap = userAuth.getAttributes();
         JSONArray roles = (JSONArray) attributeMap.get(ROLES);
         String newAuthString = returnEquivalentAuthority(roles, getDefaultInstructorRoles());
         OidcUserAuthority newUserAuth = new OidcUserAuthority(newAuthString, userAuth.getIdToken(), userAuth.getUserInfo());

         remappedAuthorities.add(newUserAuth);
      }

      return remappedAuthorities;
   }


   /**
    * Given a list of user roles, return the internal equivalent role
    * Can be overridden by an implementing class if the role needs are different.
    * @param userRoles List of user roles coming from the lti launch
    * @param instructorRoles List of roles deemed as "Instructor" equivalents
    * @return Return the appropriate authority
    */
   protected String returnEquivalentAuthority(JSONArray userRoles, List<String> instructorRoles) {
      for (String instructorRole : instructorRoles) {
         if (userRoles.contains(instructorRole)) {
            return LTIConstants.INSTRUCTOR_AUTHORITY;
         }
      }
      return LTIConstants.STUDENT_AUTHORITY;
   }

   protected List<String> getDefaultInstructorRoles() {
      return Arrays.asList(Role.System.ADMINISTRATOR, Role.System.ACCOUNT_ADMIN, Role.System.SYS_ADMIN,
            Role.Institution.ADMINISTRATOR, Role.Institution.INSTRUCTOR, Role.Institution.FACULTY,
            Role.Context.ADMINISTRATOR, Role.Context.INSTRUCTOR);
   }
}
