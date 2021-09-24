package edu.iu.uits.lms.canvas.helpers;

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

/**
 * Created by chmaurer on 1/14/15.
 */
public class CanvasConstants {
   public static final String INSTRUCTOR_ROLE = "Instructor";
   public static final String LEARNER_ROLE = "Learner";
   public static final String TA_ROLE = "urn:lti:role:ims/lis/TeachingAssistant";
   public static final String DESIGNER_ROLE = "ContentDeveloper";
   public static final String OBSERVER_ROLE = "urn:lti:instrole:ims/lis/Observer";
   public static final String ADMIN_ROLE = "urn:lti:instrole:ims/lis/Administrator";

   public static final String ACTIVE_STATUS = "active";


   public static final String SUCCESS_STRING = "Success";
   public static final String FAILURE_STRING = "Failure";
   public static final String AUTHORIZATION_HEADER_NAME = "Authorization";
   public static final String AUTHORIZATION_HEADER_TYPE = "Basic";

   public static final String ENTITY_NOTFOUND_STRING = "NOT FOUND";

   // The Canvas API allows retrieving objects by alternate identifiers instead of just
   // the internal ID, which is the default. These are other optional identifiers that may
   // be used in many of the API calls.
   public static final String API_FIELD_SIS_COURSE_ID = "sis_course_id";
   public static final String API_FIELD_SIS_LOGIN_ID = "sis_login_id";
   public static final String API_FIELD_SIS_TERM_ID = "sis_term_id";
   public static final String API_FIELD_SIS_USER_ID = "sis_user_id";
   public static final String API_FIELD_SIS_ACCOUNT_ID = "sis_account_id";
   public static final String API_FIELD_SIS_SECTION_ID = "sis_section_id";
   public static final String API_FIELD_SIS_GROUP_ID = "sis_group_id";

}
