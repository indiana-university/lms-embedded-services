package edu.iu.uits.lms.lti;

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

/**
 * Created by gjthomas on 12/4/15.
 */
public class LTIConstants {

    public final static String READ = "lti:read";
    public final static String WRITE = "lti:write";
    public final static String READ_SCOPE = "SCOPE_" + READ;
    public final static String WRITE_SCOPE = "SCOPE_" + WRITE;
    public static final String AUTHORITY_ROLE_PREFIX = "ROLE_";
    public static final String ADMIN_ROLE = "LTI_ADMIN";
    public static final String INSTRUCTOR_ROLE = "LTI_INSTRUCTOR";
    public static final String STUDENT_ROLE = "LTI_STUDENT";
    public static final String TA_ROLE = "LTI_TA";
    public static final String OBSERVER_ROLE = "LTI_OB";
    public static final String DESIGNER_ROLE = "LTI_DESIGNER";
    public static final String BASE_USER_ROLE = "USER";

    public static final String ADMIN_AUTHORITY = AUTHORITY_ROLE_PREFIX + ADMIN_ROLE;
    public static final String INSTRUCTOR_AUTHORITY = AUTHORITY_ROLE_PREFIX + INSTRUCTOR_ROLE;
    public static final String STUDENT_AUTHORITY = AUTHORITY_ROLE_PREFIX + STUDENT_ROLE;
    public static final String TA_AUTHORITY = AUTHORITY_ROLE_PREFIX + TA_ROLE;
    public static final String OBSERVER_AUTHORITY = AUTHORITY_ROLE_PREFIX + OBSERVER_ROLE;
    public static final String DESIGNER_AUTHORITY = AUTHORITY_ROLE_PREFIX + DESIGNER_ROLE;

    public static final String BASE_USER_AUTHORITY = AUTHORITY_ROLE_PREFIX + BASE_USER_ROLE;

    public static final String UI_DATE_FORMAT = "MMM dd 'at' h:mm a";


    public static final String AUTH_HEADER_KEY_GRANT_TYPE = "grant_type";
    public static final String AUTH_HEADER_KEY_CLIENT_ASSERTION_TYPE = "client_assertion_type";
    public static final String AUTH_HEADER_KEY_CLIENT_ASSERTION = "client_assertion";
    public static final String AUTH_HEADER_KEY_SCOPE = "scope";

    public static final String AUTH_HEADER_CLIENT_CREDENTIALS = "client_credentials";


    public static final String JWT_ASSERTION_TYPE = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";

    public static final String REQUEST_KEY_ID_TOKEN = "id_token";

    public static final String SCOPE_MEMBERSHIP_READONLY = "https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly";

    public static final String CLAIMS_KEY_CUSTOM = "https://purl.imsglobal.org/spec/lti/claim/custom";
    public static final String CLAIMS_KEY_ROLES = "https://purl.imsglobal.org/spec/lti/claim/roles";
    public static final String CLAIMS_KEY_NAMESROLESERVICE = "https://purl.imsglobal.org/spec/lti-nrps/claim/namesroleservice";

    public static final String CLAIMS_NAMEROLESERVICE_MEMBERSHIPS_URL_KEY = "context_memberships_url";

    public static final String CLAIMS_FAMILY_NAME_KEY = "family_name";
    public static final String CLAIMS_GIVEN_NAME_KEY = "given_name";

    public static final String CLAIMS_FULL_NAME_KEY = "name";
    public static final String CLAIMS_EMAIL_KEY = "email";

    public static final String CLAIMS_CONTEXT_TITLE_KEY = "title";
    public static final String CLAIMS_CONTEXT_LABEL_KEY = "label";

    public static final String CLAIMS_LIS_COURSE_OFFERING_SOURCEDID_KEY = "course_offering_sourcedid";

    public static final String CLAIMS_PLATFORM_GUID_KEY = "guid";

    public static final String CUSTOM_CANVAS_ACCOUNT_ID_KEY = "canvas_account_id";
    public static final String CUSTOM_CANVAS_COURSE_ID_KEY = "canvas_course_id";
    public static final String CUSTOM_CANVAS_USER_ID_KEY = "canvas_user_id";
    public static final String CUSTOM_CANVAS_USER_LOGIN_ID_KEY = "canvas_user_login_id";
    public static final String CUSTOM_CANVAS_USER_SIS_ID_KEY = "canvas_user_sis_id";
    public static final String CUSTOM_CANVAS_MEMBERSHIP_ROLES_KEY = "canvas_membership_roles";
    public static final String CUSTOM_INSTRUCTURE_MEMBERSHIP_ROLES_KEY = "instructure_membership_roles";

    public static final String CUSTOM_CANVAS_COURSE_ID_VAL = "$Canvas.course.id";
    public static final String CUSTOM_CANVAS_USER_ID_VAL = "$Canvas.user.id";
    public static final String CUSTOM_CANVAS_USER_LOGIN_ID_VAL = "$Canvas.user.loginId";
    public static final String CUSTOM_CANVAS_USER_SIS_ID_VAL = "$Canvas.user.sisSourceId";

    public static final String WELL_KNOWN_PATH = "/.well-known";
    public static final String WELL_KNOWN_ALL = WELL_KNOWN_PATH + "/**";
    public static final String JWKS_CONFIG_URI = WELL_KNOWN_PATH + "/jwks.json";
    public static final String JWKS_PUB_CONFIG_URI = WELL_KNOWN_PATH + "/jwks_pub.json";

    /**
     * Constant used for the profile to enable the LTI related REST operations
     */
    public final static String LTIREST_PROFILE = "ltirest";

    /**
     * Group code wrapping up the LTI related REST operations in the generated API specs
     */
    public final static String LTI_GROUP_CODE = "lti-framework";

    /**
     * Group code path wrapping up the LTI related REST operations in the generated API specs
     */
    public final static String LTI_GROUP_CODE_PATH = "/" + LTI_GROUP_CODE;

    public static final String CANVAS_INSTRUCTOR_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";
    public static final String CANVAS_LEARNER_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";
    public static final String CANVAS_TA_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/membership/Instructor#TeachingAssistant";
    public static final String CANVAS_DESIGNER_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper";
    public static final String CANVAS_OBSERVER_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor";
    public static final String CANVAS_ADMIN_ROLE = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator";

    private LTIConstants() {
    }
}
