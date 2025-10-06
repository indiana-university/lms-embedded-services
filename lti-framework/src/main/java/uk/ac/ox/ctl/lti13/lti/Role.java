package uk.ac.ox.ctl.lti13.lti;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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
 * @see <a href="https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies">https://www.imsglobal.org/spec/lti/v1p3/#role-vocabularies</a>
 */
public class Role {

    public static class System {

        // Core system roles
        public static final String ADMINISTRATOR = "http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator";
        public static final String NONE = "http://purl.imsglobal.org/vocab/lis/v2/system/person#None";

        // Non-core system roles
        public static final String ACCOUNT_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/system/person#AccountAdmin";
        public static final String CREATOR = "http://purl.imsglobal.org/vocab/lis/v2/system/person#Creator";
        public static final String SYS_ADMIN = "http://purl.imsglobal.org/vocab/lis/v2/system/person#SysAdmin";
        public static final String SYS_SUPPORT = "http://purl.imsglobal.org/vocab/lis/v2/system/person#SysSupport";
        public static final String USER = "http://purl.imsglobal.org/vocab/lis/v2/system/person#User";
    }

    public static class Institution {

        // Core institution roles
        public static final String ADMINISTRATOR = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Administrator";
        public static final String FACULTY = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Faculty";
        public static final String GUEST = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Guest";
        public static final String NONE = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#None";
        public static final String OTHER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Other";
        public static final String STAFF = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Staff";
        public static final String STUDENT = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Student";

        // Non‑core institution roles
        public static final String ALUMNI = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Alumni";
        public static final String INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor";
        public static final String LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Learner";
        public static final String MEMBER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Member";
        public static final String MENTOR = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Mentor";
        public static final String OBSERVER = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#Observer";
        public static final String PROSPECTIVE_STUDENT = "http://purl.imsglobal.org/vocab/lis/v2/institution/person#ProspectiveStudent";
    }

    public static class Context {

        // Core context roles
        public static final String ADMINISTRATOR = "http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator";
        public static final String CONTENT_DEVELOPER = "http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper";
        public static final String INSTRUCTOR = "http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor";
        public static final String LEARNER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Learner";
        public static final String MENTOR = "http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor";

        // Non‑core context roles
        public static final String MANAGER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Manager";
        public static final String MEMBER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Member";
        public static final String OFFICER = "http://purl.imsglobal.org/vocab/lis/v2/membership#Officer";
    }
}
