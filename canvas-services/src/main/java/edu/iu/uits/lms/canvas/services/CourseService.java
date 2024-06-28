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
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.model.CourseCreateWrapper;
import edu.iu.uits.lms.canvas.model.CourseSectionUpdateWrapper;
import edu.iu.uits.lms.canvas.model.Enrollment;
import edu.iu.uits.lms.canvas.model.EnrollmentCreateWrapper;
import edu.iu.uits.lms.canvas.model.ExternalTool;
import edu.iu.uits.lms.canvas.model.Favorite;
import edu.iu.uits.lms.canvas.model.FeatureFlag;
import edu.iu.uits.lms.canvas.model.Page;
import edu.iu.uits.lms.canvas.model.PageCreateWrapper;
import edu.iu.uits.lms.canvas.model.QuotaInfo;
import edu.iu.uits.lms.canvas.model.Section;
import edu.iu.uits.lms.canvas.model.SectionCreateWrapper;
import edu.iu.uits.lms.canvas.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API for accessing courses
 * @see <a href="https://canvas.instructure.com/doc/api/courses.html">Courses API</a>
 */
@Service
@Slf4j
public class CourseService extends SpringBaseService {
    private static final String ACCOUNTS_BASE_URI = "{url}/accounts/{id}";
    private static final String ACCOUNTS_COURSES_URI = ACCOUNTS_BASE_URI + "/courses";
    private static final String COURSES_BASE_URI = "{url}/courses";
    private static final String FAVORITES_URI = "{url}/users/self/favorites/courses";
    private static final String COURSE_URI = COURSES_BASE_URI + "/{id}";
    private static final String COURSE_USERS_URI = COURSE_URI + "/users";
    private static final String COURSE_ENROLLMENTS_URI = COURSE_URI + "/enrollments";
    private static final String COURSE_DELETE_ENROLLMENTS_URI = COURSE_ENROLLMENTS_URI + "/{enrollmentId}";
    private static final String USERS_URI = "{url}/users/{id}";
    private static final String COURSE_SECTIONS_BASE_URI = COURSE_URI + "/sections";
    private static final String SECTIONS_BASE_URI = "{url}/sections";
    private static final String SECTION_ENROLLMENTS_URI = SECTIONS_BASE_URI + "/{id}/enrollments";
    private static final String COURSE_PAGES_URI = COURSE_URI + "/pages";

    private UriTemplate ACCOUNTS_COURSES_TEMPLATE = new UriTemplate(ACCOUNTS_COURSES_URI);
    private UriTemplate COURSE_BASE_TEMPLATE = new UriTemplate(COURSES_BASE_URI);
    private UriTemplate COURSE_ENROLLMENTS_TEMPLATE = new UriTemplate(COURSE_ENROLLMENTS_URI);
    private UriTemplate COURSE_DELETE_ENROLLMENTS_TEMPLATE = new UriTemplate(COURSE_DELETE_ENROLLMENTS_URI);
    private UriTemplate COURSE_TEMPLATE = new UriTemplate(COURSE_URI);
    private UriTemplate FAVORITES_TEMPLATE = new UriTemplate(FAVORITES_URI + "/{id}");
    private UriTemplate COURSE_USERS_TEMPLATE = new UriTemplate(COURSE_USERS_URI);
    private UriTemplate USERS_TEMPLATE = new UriTemplate(USERS_URI);
    private UriTemplate COURSE_SECTIONS_TEMPLATE = new UriTemplate(COURSE_SECTIONS_BASE_URI);
    private UriTemplate SECTION_ENROLLMENTS_TEMPLATE = new UriTemplate(SECTION_ENROLLMENTS_URI);
    private UriTemplate COURSE_PAGES_TEMPLATE = new UriTemplate(COURSE_PAGES_URI);

    // feel free to pass in "sis_course_id:1234" for the courseId if you need the SIS id instead of Canvas's course id
    public Course getCourse(String courseId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("include[]", "term");

        try {
            HttpEntity<Course> courseResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), Course.class);

            if (courseResponseEntity != null) {
                return courseResponseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error: ", hcee);
        }

        return null;
    }

    public List<User> getInstructorsForCourse(String courseId) {
        URI uri = COURSE_USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("enrollment_type", "teacher");

        return doGet(builder.build().toUri(), User[].class);
    }

    /**
     * Get all of the courses where the given user is an instructor
     * @param IUNetworkId IU Network Id
     * @param excludeBlueprint set to true if you don't want to include blueprint courses in this list
     * @param includeSections Flag indicating whether or not to return sections under the course
     * @param includeTerm Flag indicating whether or not to return terms under the course
     * @return List of Courses
     */
    public List<Course> getCoursesTaughtBy(String IUNetworkId, boolean excludeBlueprint,
                                           boolean includeSections, boolean includeTerm) {


        List<String> workflowStates = Arrays.asList("available", "unpublished", "completed");

//        List<Course.WORKFLOW_STATE> workflowStates = Arrays.asList(Course.WORKFLOW_STATE.AVAILABLE,
//                Course.WORKFLOW_STATE.UNPUBLISHED, Course.WORKFLOW_STATE.COMPLETED);
        return getCoursesForUserByEnrollmentType(IUNetworkId, "teacher", excludeBlueprint, includeSections, includeTerm, workflowStates);
    }

    public List<Course> getCoursesForUserByEnrollmentType(String iuNetworkId, String enrollmentType, boolean excludeBlueprint,
                                                          boolean includeSections, boolean includeTerm, List<String> states) {
        // {url}/api/v1/users/sis_login_id:{networkId}/courses?
        // exclude_blueprint_courses=false&state[]=available&state[]=unpublished&state[]=completed&enrollment_type=teacher&per_page=100
        String bonusPath = "sis_login_id:" + iuNetworkId + "/courses";
        URI uri = USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), bonusPath);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("exclude_blueprint_courses", excludeBlueprint);

        if (includeSections) {
            builder.queryParam("include[]", "sections");
        }

        if (includeTerm) {
            builder.queryParam("include[]", "term");
        }

        if (states != null) {
            for (String state : states) {
                builder.queryParam("state[]", state);
            }
        }

        builder.queryParam("enrollment_type", enrollmentType);

        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Course[].class);
    }

    /**
     * Get courses the supplied user can see
     * @param iuNetworkId User to find courses for
     * @param includeSections set to true if you want to return sections under the course
     * @param includeTerm set to true if you want to return the course's term info
     * @param excludeBlueprint set to true if you don't want to include blueprint courses in this list
     * @param states Workflow states used to filter results
     * @return
     */
    public List<Course> getCoursesForUser(String iuNetworkId, boolean includeSections, boolean includeTerm, boolean excludeBlueprint,
                                          List<String> states) {
        //courses?as_user_id=sis_login_id:username&
        // state[]=unpublished, available&
        // enrollment_state[]=active,invited_or_pending,completed
        URI uri = COURSE_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl());

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("as_user_id", "sis_login_id:" + iuNetworkId);
        builder.queryParam("exclude_blueprint_courses", excludeBlueprint);
        builder.queryParam("include[]", "favorites");

        if (includeSections) {
            builder.queryParam("include[]", "sections");
        }

        if (includeTerm) {
            builder.queryParam("include[]", "term");
        }

        if (states != null) {
            for (String state : states) {
                builder.queryParam("state[]", state);
            }
        }

        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Course[].class);
    }

    /**
     *
     * @param courseId
     * @return the Enrollments for "active" students enrolled in the given course
     */
    public List<Enrollment> getStudentCourseEnrollment(@NonNull String courseId) {
        URI uri = COURSE_ENROLLMENTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("type[]", "StudentEnrollment");
        builder.queryParam("per_page", "50");
        builder.queryParam("state[]", "active");

        return doGet(builder.build().toUri(), Enrollment[].class);
    }

    /**
     * Get all sections in a course
     * @param courseId
     * @return List of Section objects
     */
    public List<Section> getCourseSections(@NonNull String courseId) {
        URI uri = COURSE_SECTIONS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        return doGet(uri, Section[].class);
    }

    /**
     *
     * @param courseId
     * @return the file quota information for the given course (ie used, allowed, and available space) in bytes
     */
    public QuotaInfo getCourseQuotaInfo(@NonNull String courseId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/files/quota");

        try {
            HttpEntity<QuotaInfo> courseQuotaInfoResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), QuotaInfo.class);
            log.debug("courseQuotaInfoResponseEntity: {}", courseQuotaInfoResponseEntity);

            if (courseQuotaInfoResponseEntity != null) {
                return courseQuotaInfoResponseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    /**
     * Add a course to the user's favorites
     * @param asUserLogin User's sis login
     * @param courseId Course id to add
     * @return
     */
    public Favorite addCourseToFavorites(String asUserLogin, String courseId) {
        URI uri = FAVORITES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + asUserLogin);

        try {
            ResponseEntity<Favorite> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, null, Favorite.class);
            log.debug("{}", response);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + response.getStatusCode() + ", reason: " + response.getStatusCode().getReasonPhrase()
                      + ", body: " + response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("error adding course to favorites", hcee);
            throw new RuntimeException("Error adding course to favorites", hcee);
        }
    }

    /**
     * Remove a course from the user's favorites
     * @param asUserLogin User's sis login
     * @param courseId Course id to remove
     * @return
     */
    public Favorite removeCourseAsFavorite(String asUserLogin, String courseId) {
        URI uri = FAVORITES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + asUserLogin);

        try {
            ResponseEntity<Favorite> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, Favorite.class);
            log.debug("{}", response);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + response.getStatusCode() + ", reason: " + response.getStatusCode().getReasonPhrase()
                      + ", body: " + response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error removing course from favorites", hcee);
            throw new RuntimeException("Error removing course from favorites", hcee);
        }
    }

    /**
     * Get users for the given course of the given enrollment_type
     * @param courseId
     * @param enrollmentTypes
     * @param enrollmentStates
     * @return
     */
    public List<User> getUsersForCourseByType(String courseId, List<String> enrollmentTypes, List<String> enrollmentStates) {
        return getUsersForCourseByTypeOptionalEnrollments(courseId, enrollmentTypes, enrollmentStates, false);
    }

    /**
     * Get users for the given course of the given enrollment_type
     * @param courseId
     * @param enrollmentTypes
     * @param enrollmentStates
     * @param includeEnrollments
     * @return
     */
    public List<User> getUsersForCourseByTypeOptionalEnrollments(String courseId, List<String> enrollmentTypes,
                                              List<String> enrollmentStates, boolean includeEnrollments) {
        URI uri = COURSE_USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (includeEnrollments) {
            builder.queryParam("include[]", "enrollments");
        }

        if (enrollmentTypes != null) {
            for (String type : enrollmentTypes) {
                builder.queryParam("enrollment_type[]", type);
            }
        }

        if (enrollmentStates != null) {
            for (String enrollmentState : enrollmentStates) {
                builder.queryParam("enrollment_state[]", enrollmentState);
            }
        }

        return doGet(builder.build().toUri(), User[].class);
    }

    /**
     *
     * @param id the identifier for the course you are updating
     * @param gradingStandardId If null, will remove any existing grading standard. So be careful!
     */
    public void updateCourseGradingStandard(String id, String gradingStandardId) {
        if (id == null) {
            throw new IllegalArgumentException("Null id passed to updateCourseGradingStandard.");
        }

        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("course[grading_standard_id]", gradingStandardId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<String> updateCourseGradingStandardResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, String.class);
            log.debug("{}", updateCourseGradingStandardResponse);

            ResponseEntity<String> responseEntity = (ResponseEntity<String>) updateCourseGradingStandardResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating course grading standard", hcee);
        }
    }

    public Course updateCourseNameAndSisCourseId(String canvasCourseId, CourseSectionUpdateWrapper courseSectionUpdateWrapper) {
        if (canvasCourseId == null) {
            throw new IllegalArgumentException("Null id passed to updateCourseNameAndSisCourseId.");
        }

        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasCourseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("course[name]", courseSectionUpdateWrapper.getName());
        multiValueMap.add("course[sis_course_id]", courseSectionUpdateWrapper.getSisId());
        multiValueMap.add("course[course_code]", courseSectionUpdateWrapper.getCourseCode());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(multiValueMap, headers);

            HttpEntity<Course> updateCourseNameAndSisCourseIdResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, Course.class);
            log.debug("{}", updateCourseNameAndSisCourseIdResponse);

            ResponseEntity<Course> responseEntity = (ResponseEntity<Course>) updateCourseNameAndSisCourseIdResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            } else {
                return updateCourseNameAndSisCourseIdResponse.getBody();
            }

        } catch (HttpClientErrorException hcee) {
            log.error("Error updating course name and sis_course_id", hcee);
        }

        return null;
    }

    /**
     * Update the given course with a new end date
     * @param id the identifier for the course you are updating
     * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
     * the Canvas Course API allows retrieving Course objects by identifiers other than the
     * internal id. See {@link CanvasConstants} for API_FIELD options
     * @param courseStartDate
     * @param courseEndDate If null, will remove the date, otherwise will set it to the date
     * @param restrictEnrollmentsToCourseDates Flag indicating that we want to restrict based on the course dates
     */
    public void updateCourseEndDate(String id, String idFieldName,
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime courseStartDate,
                                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime courseEndDate,
                                    boolean restrictEnrollmentsToCourseDates) {
        if (id == null) {
            throw new IllegalArgumentException("Null id passed to updateCourseEndDate.");
        }

        updateTermAndCourseEndDate(id, idFieldName, null, courseStartDate, courseEndDate, restrictEnrollmentsToCourseDates);
    }

    /**
     * Update the given course with a new end date and term
     * @param id the identifier for the course you are updating
     * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
     * the Canvas Course API allows retrieving Course objects by identifiers other than the
     * internal id. See {@link CanvasConstants} for API_FIELD options
     * @param termId If null, term will not be updated
     * @param courseStartDate
     * @param courseEndDate If null, will remove the date, otherwise will set it to the date
     * @param restrictEnrollmentsToCourseDates Flag indicating that we want to restrict based on the course dates
     */
    public Course updateTermAndCourseEndDate(String id, String idFieldName, String termId,
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime courseStartDate,
                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime courseEndDate,
                                             boolean restrictEnrollmentsToCourseDates) {

        if (id == null) {
            throw new IllegalArgumentException("Null id passed to updateTermAndCourseEndDate.");
        }

        id = buildAlternateId(id, idFieldName);

        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);
        log.debug(uri.toString());

        // Something (Canvas or Jersey) didn't really like a null date, so turning it into an empty string instead.
        String courseEndDateStr = "";
        if (courseEndDate != null) {
            courseEndDateStr = courseEndDate.toString();
        }

        String courseStartDateStr = "";
        if (courseStartDate != null) {
            courseStartDateStr = courseStartDate.toString();
        }

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("course[start_at]", courseStartDateStr);
        builder.queryParam("course[end_at]", courseEndDateStr);
        builder.queryParam("course[restrict_enrollments_to_course_dates]", restrictEnrollmentsToCourseDates);

        if (termId != null && !termId.isEmpty()) {
            builder.queryParam("course[term_id]", termId);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Course> updateTermAndCourseEndDateResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, Course.class);
            log.debug(updateTermAndCourseEndDateResponse.toString());

            ResponseEntity<Course> responseEntity = (ResponseEntity<Course>) updateTermAndCourseEndDateResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            } else {
                return updateTermAndCourseEndDateResponse.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating term and course end dates", hcee);
        }

        return null;
    }

    /**
     * Used to change the default_view for the home page of a course
     * @param id the identifier for the course you are updating
     * @param defaultViewType the value of what you to you want for the default view, e.g. modules
     */
    public void updateCourseFrontPage(String id, String defaultViewType) {
        if (id == null) {
            throw new IllegalArgumentException("Null id passed to updateCourseFrontPage.");
        }

        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("course[default_view]", defaultViewType);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<String> updateCourseFrontPageResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, String.class);
            log.debug("{}", updateCourseFrontPageResponse);

            ResponseEntity<String> responseEntity = (ResponseEntity<String>) updateCourseFrontPageResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating course front page", hcee);
        }
    }

    /**
     * Create a new course in canvas
     * @param newCourse
     * @return a Canvas Course object
     */
    public Course createCourse(CourseCreateWrapper newCourse) {
        Course savedCourse = null;

        // in case we don't have an accountId in the object, make sure the course is at least associated with the base account
        String accountId = (newCourse.getAccountId() == null || newCourse.getAccountId().isEmpty()) ? canvasConfiguration.getAccountId() : newCourse.getAccountId();

        URI uri = ACCOUNTS_COURSES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("{}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<CourseCreateWrapper> createNewCourseRequest = new HttpEntity<>(newCourse, headers);
            HttpEntity<Course> createNewCourseResponse = this.restTemplate.exchange(uri, HttpMethod.POST, createNewCourseRequest, Course.class);
            log.debug("{}", createNewCourseResponse);

            savedCourse = createNewCourseResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating course", hcee);
            throw new RuntimeException("Error creating course", hcee);
        }

        return savedCourse;
    }

    /**
     * Create a new course section in canvas
     * @param newSection
     * @return a Canvas Section object
     */
    public Section createCourseSection(SectionCreateWrapper newSection) {
        URI uri = COURSE_SECTIONS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), newSection.getCourseSection().getCourse_id());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SectionCreateWrapper> sectionCreateWrapperRequestEntity = new HttpEntity<>(newSection, headers);
            HttpEntity<Section> sectionCreateWrapperResponseEntity = this.restTemplate.exchange(uri, HttpMethod.POST, sectionCreateWrapperRequestEntity, Section.class);
            log.debug("{}", sectionCreateWrapperResponseEntity);

            if (sectionCreateWrapperResponseEntity != null) {
                Section savedSection = sectionCreateWrapperResponseEntity.getBody();

                return savedSection;
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating course section", hcee);
        }

        return null;
    }

    /**
     * Create a new enrollment in canvas
     * @param enrollmentWrapper Enrollment to create
     * @return Created enrollment
     */
    public Enrollment createEnrollment(EnrollmentCreateWrapper enrollmentWrapper) {
        Enrollment savedEnrollment = null;

        String sectionId = enrollmentWrapper.getEnrollment().getCourseSectionId();

        URI uri = SECTION_ENROLLMENTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId);
        log.debug("{}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<EnrollmentCreateWrapper> createNewCourseSectionEnrollmentRequest = new HttpEntity<>(enrollmentWrapper, headers);
            HttpEntity<Enrollment> createNewCourseSectionEnrollmentResponse = this.restTemplate.exchange(uri, HttpMethod.POST, createNewCourseSectionEnrollmentRequest, Enrollment.class);
            log.debug("{}", createNewCourseSectionEnrollmentResponse);

            ResponseEntity<Enrollment> responseEntity = (ResponseEntity<Enrollment>) createNewCourseSectionEnrollmentResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }

            savedEnrollment = createNewCourseSectionEnrollmentResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating enrollment", hcee);
            throw new RuntimeException("Error creating course section enrollment", hcee);
        }

        return savedEnrollment;
    }

    /**
     * Get the feature flag information for the given course and feature
     * @param courseId Canvas course id
     * @param featureId Feature Id
     * @return The created FeatureFlag
     */
    public FeatureFlag getCourseFeature(String courseId, String featureId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/features");
        builder.path("/flags");
        builder.path("/" + featureId);

        try {
            HttpEntity<FeatureFlag> featureFlagResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), FeatureFlag.class);
            log.debug("{}", featureFlagResponseEntity);

            ResponseEntity<FeatureFlag> responseEntity = (ResponseEntity<FeatureFlag>) featureFlagResponseEntity;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }

            if (featureFlagResponseEntity != null) {
                return featureFlagResponseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting course features", hcee);
            throw new RuntimeException("Error getting course feature", hcee);
        }

        return null;
    }

    /**
     * Set the state of the given feature for the given course
     * @param courseId Canvas course id
     * @param featureId Feature Id
     * @param state State the feature should be set to
     * @return The updated FeatureFlag
     */
    public FeatureFlag setCourseFeature(String courseId, String featureId, String state) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/features");
        builder.path("/flags");
        builder.path("/" + featureId);
        builder.queryParam("state", state);


        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<FeatureFlag> featureFlagResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, FeatureFlag.class);
            log.debug("{}", featureFlagResponse);

            ResponseEntity<FeatureFlag> responseEntity = (ResponseEntity<FeatureFlag>) featureFlagResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }

            if (featureFlagResponse != null) {
                return featureFlagResponse.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating course feature", hcee);
            throw new RuntimeException("Error setting course feature", hcee);
        }

        return null;
    }

    /**
     * Remove the feature from the given course
     * @param courseId Canvas course id
     * @param featureId Feature Id
     * @return The removed FeatureFlag object
     */
    public FeatureFlag removeCourseFeature(String courseId, String featureId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/features");
        builder.path("/flags");
        builder.path("/" + featureId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<FeatureFlag> featureFlagResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, FeatureFlag.class);
            log.debug("{}", featureFlagResponse);

            ResponseEntity<FeatureFlag> responseEntity = (ResponseEntity<FeatureFlag>) featureFlagResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }

            if (featureFlagResponse != null) {
                return featureFlagResponse.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error deleting course feature", hcee);
            throw new RuntimeException("Error setting course feature", hcee);
        }

        return null;
    }

    /**
     * Get the roster for a course, as seen by asUser.
     * @see <a href="https://canvas.instructure.com/doc/api/file.masquerading.html">Canvas API Docs on Masquerading</a>
     *
     * @param courseId
     * @param asUser
     * @param enrollmentStates
     * @return
     */
    public List<User> getRosterForCourseAsUser(String courseId, String asUser, List<String> enrollmentStates) {
        URI uri = COURSE_USERS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("include[]", "email");
        builder.queryParam("include[]", "avatar_url");
        builder.queryParam("include[]", "enrollments");

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        if (enrollmentStates != null) {
            for (String enrollmentState : enrollmentStates) {
                builder.queryParam("enrollment_state[]", enrollmentState);
            }
        }

        return doGet(builder.build().toUri(), User[].class);
    }

    /**
     * Delete an enrollment for a course
     * @param courseId Canvas course id
     * @param enrollmentId Enrollment Id
     * @return The removed Enrollment object
     */
    public Enrollment deleteEnrollment(String courseId, String enrollmentId) {
        URI uri = COURSE_DELETE_ENROLLMENTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, enrollmentId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("task", "delete");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<Enrollment> deleteEnrollmentResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, Enrollment.class);
            log.debug("{}", deleteEnrollmentResponse);

            ResponseEntity<Enrollment> responseEntity = (ResponseEntity<Enrollment>) deleteEnrollmentResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            }

            if (deleteEnrollmentResponse != null) {
                return deleteEnrollmentResponse.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error deleting enrollment", hcee);
            throw new RuntimeException("Error deleting enrollment", hcee);
        }

        return null;
    }

    /**
     * Hide a course tool/tab for a given courseId and toolId
     * @param courseId
     * @param courseToolId
     */
    public void hideCourseTool(String courseId, String courseToolId) {
        toggleCourseTool(courseId, courseToolId, true);
    }

    /**
     * Show a course tool/tab for a given courseId and toolId
     * @param courseId
     * @param courseToolId
     */
    public void showCourseTool(String courseId, String courseToolId) {
        toggleCourseTool(courseId, courseToolId, false);
    }

    private void toggleCourseTool(@NonNull String courseId, @NonNull String courseToolId, boolean isHidden) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/tabs");
        builder.path("/" + courseToolId);
        builder.queryParam("hidden", isHidden);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<String> toggleCourseToolResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, String.class);
            log.debug("{}", toggleCourseToolResponse);

            ResponseEntity<String> responseEntity = (ResponseEntity<String>) toggleCourseToolResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                      + ", body: " + responseEntity.getBody());
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error toggling the course tool", hcee);
            throw new RuntimeException("Error modifying tab/tool", hcee);
        }
    }

    public ExternalCourseToolResult getCourseTool(String courseId, String courseToolId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/external_tools");
        builder.path("/" + courseToolId);

        ResponseErrorHandler oldResponseErrorHandler = restTemplate.getErrorHandler();

        ExternalCourseToolResult externalCourseToolResult = null;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            restTemplate.setErrorHandler(new ClientErrorHandler());

            HttpEntity<ExternalTool> httpEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, null, ExternalTool.class);

            ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) httpEntity;

            externalCourseToolResult = new ExternalCourseToolResult(responseEntity.getStatusCode(), responseEntity.getStatusCodeValue(), responseEntity.getBody());

        } catch (Exception e) {
            log.error("Error ", e);
        } finally {
            restTemplate.setErrorHandler(oldResponseErrorHandler);
        }

        return externalCourseToolResult;
    }

    public ExternalCourseToolResult renameCourseTool(String courseId, String courseToolId, String newToolName) {
        if (newToolName == null || newToolName.trim().length() == 0) {
            return null;
        }

        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/external_tools");
        builder.path("/" + courseToolId);

        ResponseErrorHandler oldResponseErrorHandler = restTemplate.getErrorHandler();
        ExternalCourseToolResult externalCourseToolResult = null;

        try {
            restTemplate.setErrorHandler(new ClientErrorHandler());

            Map<String, String> valueMap = new HashMap<>();
            valueMap.put("name", newToolName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(valueMap, headers);

            HttpEntity<ExternalTool> httpEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, ExternalTool.class);

            ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) httpEntity;

            externalCourseToolResult = new ExternalCourseToolResult(responseEntity.getStatusCode(), responseEntity.getStatusCodeValue(), responseEntity.getBody());

        } catch (Exception e) {
            log.error("Error ", e);
        } finally {
            restTemplate.setErrorHandler(oldResponseErrorHandler);
        }

        return externalCourseToolResult;
    }

    public ExternalCourseToolResult deleteCourseTool(String courseId, String courseToolId) {
        URI uri = COURSE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.path("/external_tools");
        builder.path("/" + courseToolId);

        ResponseErrorHandler oldResponseErrorHandler = restTemplate.getErrorHandler();
        ExternalCourseToolResult externalCourseToolResult = null;

        try {
            restTemplate.setErrorHandler(new ClientErrorHandler());

            HttpEntity<ExternalTool> httpEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, ExternalTool.class);

            ResponseEntity<ExternalTool> responseEntity = (ResponseEntity<ExternalTool>) httpEntity;

            externalCourseToolResult = new ExternalCourseToolResult(responseEntity.getStatusCode(), responseEntity.getStatusCodeValue(), responseEntity.getBody());

        } catch (Exception e) {
            log.error("Error ", e);
        } finally {
            restTemplate.setErrorHandler(oldResponseErrorHandler);
        }

        return externalCourseToolResult;
    }

    public List<Page> getPages(String courseId) {
        URI uri = COURSE_PAGES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        return doGet(builder.build().toUri(), Page[].class);
    }

    public Page createPage(PageCreateWrapper newPage) {
        Page savedPage = null;

        URI uri = COURSE_PAGES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), newPage.getCourseId());
        log.debug("{}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<PageCreateWrapper> createNewPageRequest = new HttpEntity<>(newPage, headers);
            HttpEntity<Page> createNewPageResponse = this.restTemplate.exchange(uri, HttpMethod.POST, createNewPageRequest, Page.class);
            log.debug("{}", createNewPageResponse);

            savedPage = createNewPageResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating course", hcee);
            throw new RuntimeException("Error creating course", hcee);
        }

        return savedPage;
    }

    private class ClientErrorHandler extends DefaultResponseErrorHandler {
        @Override
        public void handleError(ClientHttpResponse response) throws IOException
        {
            log.info("Handling error!!!");
        }
    }

    @Data
    @AllArgsConstructor
    public static class ExternalCourseToolResult implements Serializable {
        public HttpStatus httpStatus;
        public int httpStatusCode;
        public ExternalTool externalTool;
    }
}
