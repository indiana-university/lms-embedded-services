package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.CanvasLogin;
import edu.iu.uits.lms.canvas.model.CommunicationChannel;
import edu.iu.uits.lms.canvas.model.JsonUser;
import edu.iu.uits.lms.canvas.model.License;
import edu.iu.uits.lms.canvas.model.LoginWrapper;
import edu.iu.uits.lms.canvas.model.PostedLogin;
import edu.iu.uits.lms.canvas.model.PostedUser;
import edu.iu.uits.lms.canvas.model.Profile;
import edu.iu.uits.lms.canvas.model.QuotaInfo;
import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.model.UserCustomDataRequest;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.Serializable;
import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class UserService extends SpringBaseService {
   private static final String USERS_URI = "{url}/users/{id}";
   private static final String CUSTOM_DATA_URI = USERS_URI + "/custom_data";
   private static final String ACCOUNTS_USERS_URI = "{url}/accounts/{accountId}/users";
   private static final String LICENSE_URI = USERS_URI + "/content_licenses";
   private static final String LOGINS_BASE_URI = "{url}/accounts/{accountId}/logins";
   private static final String LOGIN_URI = LOGINS_BASE_URI + "/{loginId}";
   private static final String LOGINS_URI = USERS_URI + "/logins";
   private static final String PROFILE_URI = USERS_URI + "/profile";
   private static final String QUOTA_URI = USERS_URI + "/files/quota";

   private static final UriTemplate CUSTOM_DATA_TEMPLATE = new UriTemplate(CUSTOM_DATA_URI);
   private static final UriTemplate ACCOUNTS_USERS_TEMPLATE = new UriTemplate(ACCOUNTS_USERS_URI);
   private static final UriTemplate LICENSE_TEMPLATE = new UriTemplate(LICENSE_URI);
   private static final UriTemplate LOGINS_BASE_TEMPLATE = new UriTemplate(LOGINS_BASE_URI);
   private static final UriTemplate LOGINS_TEMPLATE = new UriTemplate(LOGINS_URI);
   private static final UriTemplate LOGIN_TEMPLATE = new UriTemplate(LOGIN_URI);
   private static final UriTemplate USERS_TEMPLATE = new UriTemplate(USERS_URI);
   private static final UriTemplate PROFILE_TEMPLATE = new UriTemplate(PROFILE_URI);
   private static final UriTemplate QUOTA_TEMPLATE = new UriTemplate(QUOTA_URI);

   /**
    *
    * @param userId If you wish to use an sis_login_id, prefix your user with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID}
    *         plus a colon (ie sis_login_id:octest1)
    * @return the file quota information for the given user (ie used, allowed, and available space) in bytes
    */
   public QuotaInfo getUserQuotaInfo(@NonNull String userId) {
      URI uri = QUOTA_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.queryParam("as_user_id", userId);

      try {
         return restTemplate.getForObject(builder.build().toUri(), QuotaInfo.class);
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
                 + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
                 + ", entity: " + hcee.getResponseBodyAsString());
      }
      return null;
   }

   /**
    * Get the custom data stored at the particular "scope" (identified by the pathParts)
    * @param userCustomDataRequest
    * @return The object (most likely a Map) stored at the requested scope
    */
   public Object getUserCustomData(UserCustomDataRequest userCustomDataRequest) {
      String userPath = buildAlternateId(userCustomDataRequest.getUserId(), userCustomDataRequest.getField());
      URI uri = CUSTOM_DATA_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userPath);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.queryParam("ns", "edu.iu.uits.lms");

      for (String path: userCustomDataRequest.getPathParts()) {
         builder.pathSegment(path);
      }
      log.debug("{}", builder.build().toUri());

      try {
         ResponseEntity<Object> response = restTemplate.getForEntity(builder.build().toUri(), Object.class);
         log.debug("{}", response);
         return response.getBody();
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString(), hcee);
      }
      return null;
   }

   /**
    * Set the custom data into the particular scope
    * @param userCustomDataRequest
    * @return
    */
   public Object setUserCustomData(UserCustomDataRequest userCustomDataRequest) {
      String userPath = buildAlternateId(userCustomDataRequest.getUserId(), userCustomDataRequest.getField());
      URI uri = CUSTOM_DATA_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userPath);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

      for (String path: userCustomDataRequest.getPathParts()) {
         builder.pathSegment(path);
      }

      try {
         MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
         map.add("ns", "edu.iu.uits.lms");
         map.add("data", userCustomDataRequest.getData());

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

         HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(map, headers);
         log.debug("{}", requestEntity);
         HttpEntity<Object> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, Object.class);
         log.debug("{}", response);
         return response.getBody();

      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString(), hcee);
      }
      return null;
   }

   /**
    * Delete the custom data stored at the particular scope
    * @param userCustomDataRequest
    * @return
    */
   public Object deleteUserCustomData(UserCustomDataRequest userCustomDataRequest) {
      String userPath = buildAlternateId(userCustomDataRequest.getUserId(), userCustomDataRequest.getField());
      URI uri = CUSTOM_DATA_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userPath);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

      for (String path: userCustomDataRequest.getPathParts()) {
         builder.pathSegment(path);
      }

      try {
         MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
         map.add("ns", "edu.iu.uits.lms");

         HttpHeaders headers = new HttpHeaders();
         headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

         HttpEntity<MultiValueMap> requestEntity = new HttpEntity<>(map, headers);
         log.debug("{}", requestEntity);
         HttpEntity<Object> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, requestEntity, Object.class);
         log.debug("{}", response);
         return response.getBody();

      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString(), hcee);
      }
      return null;
   }

   /**
    * retrieves logins for a guest user from Canvas
    * @param loginId
    * @return List of CanvasLogin objects. Or null if user logins not found rather than
    * throwing exception since it's expected that some users won't exist.
    */
   public List<CanvasLogin> getGuestUserLogins(String loginId) {
      String firstIdCheck = "sis_user_id:" + loginId;
      String secondIdCheck = "sis_login_id:" + loginId;
      URI uri = LOGINS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), firstIdCheck);
      log.debug("{}", uri);

      List<CanvasLogin> logins = doGet(uri, CanvasLogin[].class);
      if (logins == null || logins.isEmpty()) {
         uri = LOGINS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), secondIdCheck);
         log.debug("{}", uri);
         logins = doGet(uri, CanvasLogin[].class);

      }
      return logins;
   }

   /**
    * creates a login for a user in Canvas
    * @param accountId Create a new login for an existing user in the given account
    * @param userId The ID of the user to create the login for.
    * @param loginUniqueId The unique ID for the new login
    * @param password The new login's password
    * @param sisUserId SIS ID for the login. The caller must be able to manage SIS permissions on the account
    * @return The id of the created login
    */
   public String createLogin(String accountId, String userId, String loginUniqueId, String password, String sisUserId) {
      URI uri = LOGINS_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);

      LoginWrapper loginWrapper = new LoginWrapper();

      PostedUser postedUser = new PostedUser();
      postedUser.setId(userId);
      loginWrapper.setUser(postedUser);

      PostedLogin postedLogin = new PostedLogin(loginUniqueId, password, sisUserId, false);
      loginWrapper.setLogin(postedLogin);

      HttpHeaders headers = new HttpHeaders();
      HttpEntity<LoginWrapper> requestEntity = new HttpEntity<>(loginWrapper, headers);

      try {
         CanvasLogin createdLogin = restTemplate.postForObject(uri, requestEntity, CanvasLogin.class);
         return createdLogin != null ? createdLogin.getId() : null;
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString(), hcee);
      }
      return null;
   }

   /**
    * creates a user in Canvas
    * @param accountId Canvas account
    * @param firstName First name
    * @param lastName Last name
    * @param loginUniqueId
    * @param sisUserId Id of the user represented by the sis system
    * @param email Email address
    * @return The id of the created canvas user
    */
   public String createUser(String accountId, String firstName, String lastName, String loginUniqueId, String sisUserId, String email) {
      JsonUser jsonUser = new JsonUser();
      PostedUser postedUser = new PostedUser();
      postedUser.setName(firstName + " " + lastName);
      postedUser.setShortName(firstName + " " + lastName);
      postedUser.setSortableName(lastName + ", " + firstName);
      if (email != null) {
         CommunicationChannel cc = new CommunicationChannel();
         cc.setType(CommunicationChannel.EMAIL_TYPE);
         cc.setAddress(email);
         cc.setSkipConfirmation(true);
         jsonUser.setCommunicationChannel(cc);
      }
      postedUser.setTermsOfUse("true");
      postedUser.setSkipRegistration(true);

      jsonUser.setUser(postedUser);

      PostedLogin pseudonym = new PostedLogin(loginUniqueId, null, sisUserId, false);
      jsonUser.setPseudonym(pseudonym);

      return createUser(accountId, jsonUser);
   }

   /**
    * This is one is used for batch Canvas guest account creation. This will set the email address for both
    * login_id and sis_user_id to be the same in Canvas
    */
   public String createUserWithUser(String accountId, User user) {
      JsonUser jsonUser = new JsonUser();
      PostedUser postedUser = new PostedUser();
      postedUser.setName(user.getName());
      postedUser.setShortName(user.getShortName());
      postedUser.setSortableName(user.getSortableName());
      postedUser.setTermsOfUse("true");

      jsonUser.setUser(postedUser);

      PostedLogin pseudonym = new PostedLogin(user.getLoginId(), null, user.getLoginId(), false);
      jsonUser.setPseudonym(pseudonym);

      return createUser(accountId, jsonUser);
   }

   /**
    * Create a user
    * @param accountId Canvas account
    * @param jsonUser Wrapped user object to create
    * @return The id of the created canvas User
    */
   private String createUser(String accountId, JsonUser jsonUser) {
      URI uri = ACCOUNTS_USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);

      HttpHeaders headers = new HttpHeaders();
      HttpEntity<JsonUser> requestEntity = new HttpEntity<>(jsonUser, headers);

      try {
         User createdUser = restTemplate.postForObject(uri, requestEntity, User.class);
         return createdUser != null ? createdUser.getId() : null;
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString());
      }
      return null;
   }

   /**
    * Lookup a user by their sis_user_id
    * @param sis_user_id Id of the user represented by the sis system
    * @return The user, or null if none found
    */
   public User getUserBySisId(String sis_user_id) {
      return getUser("sis_user_id", sis_user_id);
   }

   /**
    * Lookup a user by their sis_login_id
    * @param sis_login_id Login Id of the user represented by the sis system
    * @return The user, or null if none found
    */
   public User getUserBySisLoginId(String sis_login_id) {
      return getUser("sis_login_id", sis_login_id);
   }

   /**
    * Lookup a user by their canvas id
    * @param canvasId Canvas Id of the user
    * @return The user, or null if none found
    */
   public User getUserByCanvasId(String canvasId) {
      if (canvasId == null || canvasId.trim().length() == 0) {
         return null;
      }

      return getUser(null, canvasId);
   }

   /**
    * Get the specified user
    * @param field What kind of value is going to be specified
    * @param value Value (sis user id, sis login id, canvas user id, etc)
    * @return User
    */
   private User getUser(String field, String value) {
      String userPath = buildAlternateId(value, field);
      URI uri = USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userPath);
      try {
         return restTemplate.getForObject(uri, User.class);
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString());
      }
      return null;
   }

   /**
    * Get all user licenses
    * @param sisLoginId Login Id of the user represented by the sis system
    * @return The list of found License objects
    */
   public List<License> getLicenses(String sisLoginId) {
      String loginPath = "sis_login_id:" + sisLoginId ;
      URI uri = LICENSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), loginPath);
      return doGet(uri, License[].class);
   }

   /**
    * retrieves profile for a user from Canvas
    * @param sisUserId Id of the user represented by the sis system
    * @return Profile of the user.
    */
   public Profile getProfile(String sisUserId) {
      String profilePath = "sis_user_id:" + sisUserId;
      URI uri = PROFILE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), profilePath);
      try {
         return restTemplate.getForObject(uri, Profile.class);
      } catch (HttpClientErrorException hcee) {
         log.error("Request to Canvas was not successful. Response code: "
               + hcee.getStatusCode() + ", reason: " + hcee.getStatusText()
               + ", entity: " + hcee.getResponseBodyAsString());
      }
      return null;
   }

   /**
    * Get the user login that is tied to the sis_user_id
    * @param sisUserId Id of the user represented by the sis system
    * @return The CanvasLogin
    */
   public CanvasLogin getLogin( String sisUserId) {
      List<CanvasLogin> logins = getLogins(sisUserId);

      if (logins != null && logins.size() > 0) {
         for (CanvasLogin login : logins) {
            //Find the login that is associated with the sisUserId
            if (sisUserId.equals(login.getSisUserId())) {
               return login;
            }
         }
      }
      return null;
   }

   /**
    * Get all user logins
    * @param sisUserId Id of the user represented by the sis system
    * @return The list of found CanvasLogin objects
    */
   public List<CanvasLogin> getLogins(String sisUserId) {
      String loginPath = "sis_user_id:" + sisUserId;
      URI uri = LOGINS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), loginPath);
      return doGet(uri, CanvasLogin[].class);
   }

   /**
    * Update name details of a canvas user
    * @param existingCanvasUserId Existing User id to lookup user for comparison
    * @param nameWrapper Wrapper object that contains name details
    * @return The updated User object
    */
   public User updateUserNameDetails(String existingCanvasUserId, NameWrapper nameWrapper) {
      URI uri = USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), existingCanvasUserId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.queryParam("user[name]", nameWrapper.getName());
      builder.queryParam("user[short_name]", nameWrapper.getShortName());
      builder.queryParam("user[sortable_name]", nameWrapper.getSortableName());

      HttpEntity<User> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, User.class);
      return response.getBody();
   }

   /**
    * Update a user's default email address.
    * If it's an email address that hasn't been used in the system before (i.e. no confirmed communication channel),
    * the address will be added, but unconfirmed.
    * @param canvasUserId Id of the user in canvas
    * @param email Email address to use as the new default
    */
   public void updateEmail(String canvasUserId, String email) {
      URI uri = USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasUserId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.queryParam("user[email]", email);

      restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, User.class);
   }

   /**
    * Update a canvas login
    * @param accountId Canvas account
    * @param loginId Id of the canvas login to update
    * @param newLoginUniqueId New unique login it to set for the user
    * @return The updated CanvasLogin
    */
   public CanvasLogin updateLogin(String accountId, String loginId, String newLoginUniqueId) {
      URI uri = LOGIN_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId, loginId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      builder.queryParam("login[unique_id]", newLoginUniqueId);

      HttpEntity<CanvasLogin> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, CanvasLogin.class);
      return response.getBody();
   }

   @Data
   private static class NameWrapper implements Serializable {
      private String name;
      private String shortName;
      private String sortableName;
   }
}
