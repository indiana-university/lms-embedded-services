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
import edu.iu.uits.lms.canvas.model.Assignment;
import edu.iu.uits.lms.canvas.model.AssignmentCreateWrapper;
import edu.iu.uits.lms.canvas.model.AssignmentGroup;
import edu.iu.uits.lms.canvas.model.AssignmentSubmission;
import edu.iu.uits.lms.canvas.model.GradeDataWrapper;
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

import java.net.URI;
import java.util.List;

/**
 * Service to get various assignment
 * related things
 */
@Service
@Slf4j
public class AssignmentService extends SpringBaseService {
    private static final String CANVAS_BASE_URI = "{url}";
    private static final String BASE_COURSE_URI = CANVAS_BASE_URI +  "/courses/{course_id}/assignments";
    private static final String BASE_SECTION_URI = CANVAS_BASE_URI +  "/sections/{section_id}/assignments";
    private static final String COURSE_ASSIGNMENT_URI = BASE_COURSE_URI +  "/{assignment_id}";
    private static final String SECTION_ASSIGNMENT_URI = BASE_SECTION_URI +  "/{assignment_id}";
    private static final String ASSIGNMENT_GROUPS_URI = CANVAS_BASE_URI +  "/courses/{course_id}/assignment_groups";
    private static final String COURSE_SUBMISSION_URI = COURSE_ASSIGNMENT_URI + "/submissions/{user_id}";
    private static final String SECTION_SUBMISSION_URI = SECTION_ASSIGNMENT_URI + "/submissions/{user_id}";

    private static final UriTemplate BASE_TEMPLATE = new UriTemplate(BASE_COURSE_URI);
    private static final UriTemplate ASSIGNMENT_TEMPLATE = new UriTemplate(COURSE_ASSIGNMENT_URI);
    private static final UriTemplate ASSIGNMENT_GROUPS_TEMPLATE = new UriTemplate(ASSIGNMENT_GROUPS_URI);
    private static final UriTemplate COURSE_SUBMISSION_TEMPLATE = new UriTemplate(COURSE_SUBMISSION_URI);
    private static final UriTemplate SECTION_SUBMISSION_TEMPLATE = new UriTemplate(SECTION_SUBMISSION_URI);

    /**
     * get Assignment by assignment id
     * @param courseId Canvas course id -12345
     * @param assignmentId Canvas assignment id -12345
     * @return Assignment
     */
    public Assignment getAssignmentByAssignmentId(String courseId, String assignmentId) {
        URI uri = ASSIGNMENT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId);
        log.debug("{}", uri);

        try {
            ResponseEntity<Assignment> assignmentEntity = this.restTemplate.getForEntity(uri, Assignment.class);
            log.debug("{}", assignmentEntity);

            return assignmentEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting assignment " + assignmentId + " for course " + courseId, hcee);
        }

        return null;
    }

    /**
     * get Assignment submission by user id
     * @param courseId Canvas course id -12345
     * @param assignmentId Canvas assignment id -12345
     * @param userId Canvas sis_login_id
     * @return Assignment submission
     */
    public AssignmentSubmission getAssignmentSubmissionByUserId(String courseId, String assignmentId, String userId) {
        final String userIdPath = "sis_login_id:" + userId;

        URI uri = COURSE_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId, userIdPath);
        log.debug("{}", uri);

        try {
            ResponseEntity<AssignmentSubmission> assignmentSubmissionEntity = this.restTemplate.getForEntity(uri, AssignmentSubmission.class);
            log.debug("{}", assignmentSubmissionEntity);

            return assignmentSubmissionEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting submission for assignment " + assignmentId + " for user " + userId + " in course " + courseId, hcee);
        }

        return null;
    }

    /**
     * Get all assignments for a given course
     * @param courseId Canvas course id
     * @return List of Assignments
     */
    public List<Assignment> getAssignments(String courseId) {
        return getAssignments(courseId, null);
    }

    /**
     * Get all assignments for a given course
     * @param courseId Canvas course id
     * @param includes List of optional includes to add to the call
     * @return List of Assignments
     */
    public List<Assignment> getAssignments(String courseId, String[] includes) {
        URI baseUri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
        if (includes != null) {
            for (String include : includes) {
                builder.queryParam("include[]", include);
            }
        }
        URI uri = builder.build().toUri();
        log.debug("{}", uri);
        return doGet(uri, Assignment[].class);
    }

    /**
     * Creates a new assignment for a given course
     * @param courseId Canvas course id
     * @param newAssignment new Assignment
     * @param asUser optional - masquerade as this user when creating the assignment. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @return a newly created Assignment from Canvas
     */
    public Assignment createAssignment(String courseId, AssignmentCreateWrapper newAssignment, String asUser) {
        if (courseId == null || newAssignment == null) {
            throw new IllegalArgumentException("Null courseId or newAssignment passed to createAssignment.");
        }

        Assignment savedAssignment = null;

        URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<AssignmentCreateWrapper> newAssignmentRequest = new HttpEntity<>(newAssignment, headers);
            ResponseEntity<Assignment> newAssignmentResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, newAssignmentRequest, Assignment.class);
            log.debug("{}", newAssignmentResponse);

            savedAssignment = newAssignmentResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating assignment", hcee);
            throw new RuntimeException("Error creating assignment", hcee);
        }

        return savedAssignment;
    }

    /**
     * Gets all assignment groups for a given course
     * @param courseId Canvas course Id
     * @return a list of Assignment Groups for the course
     */
    public List<AssignmentGroup> getAssignmentGroups(String courseId) {
        URI uri = ASSIGNMENT_GROUPS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        return doGet(builder.build().toUri(), AssignmentGroup[].class);
    }

    /**
     * Creates a new assignment group named with the supplied name for a given course
     * @param courseId Canvas course id
     * @param assignmentGroupName name for created assignment group
     * @param asUser optional - masquerade as this user when creating the assignment group. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @return a newly created AssignmentGroup from Canvas
     */
    public AssignmentGroup createAssignmentGroup(String courseId, String assignmentGroupName, String asUser) {
        if (courseId == null || assignmentGroupName == null) {
            throw new IllegalArgumentException("Null courseId or name passed to createAssignmentGroup.");
        }

        AssignmentGroup savedAssignmentGroup = null;

        URI uri = ASSIGNMENT_GROUPS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("name", assignmentGroupName);

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        try {
            ResponseEntity<AssignmentGroup> createNewAssignmentGroupResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, null, AssignmentGroup.class);
            log.debug("{}", createNewAssignmentGroupResponse);

            savedAssignmentGroup = createNewAssignmentGroupResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating assignment group", hcee);
            throw new RuntimeException("Error creating assignment group", hcee);
        }
        return savedAssignmentGroup;
    }

  /**
   * Update the description of an assignment.
   *
   * @param courseId Canvas course id
   * @param assignmentId Canvas assignment id
   * @param asUser optional - masquerade as this user when updating the assignment. If you wish to use an sis_login_id,
   *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
   * @param description new description for the assignment
   * @return the updated Assignment
   */
  public Assignment updateAssignmentDescription(String courseId, String assignmentId, String asUser, String description) {
        URI uri = ASSIGNMENT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("assignment[description]", description);

            HttpEntity<MultiValueMap<String, String>> updateRequest = new HttpEntity<>(multiValueMap, headers);
            ResponseEntity<Assignment> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, updateRequest, Assignment.class);
            log.debug("responseEntity: {}", responseEntity);

            if (responseEntity != null) {
                return responseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    public AssignmentSubmission getAssignmentSubmissionBySection(String sectionId, String assignmentId, String userId) {
        URI uri = SECTION_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId, assignmentId, userId);
        return getAssignmentSubmission(uri);
    }

    public AssignmentSubmission getAssignmentSubmissionByCourse(String courseId, String assignmentId, String userId) {
        URI uri = COURSE_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId, userId);
        return getAssignmentSubmission(uri);
    }

    private AssignmentSubmission getAssignmentSubmission(URI uri) {
        log.debug("{}", uri);

        try {
            ResponseEntity<AssignmentSubmission> assignmentEntity = this.restTemplate.getForEntity(uri, AssignmentSubmission.class);
            log.debug("{}", assignmentEntity);

            return assignmentEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting assignment submission: " + uri.toString(), hcee);
        }

        return null;
    }



    /**
     * Submits a grade or comment for an assignment submission.
     *
     * @param sectionId the Canvas section id
     * @param assignmentId the Canvas assignment id
     * @param userId the Canvas user id (or sis_login_id)
     * @param grade the grade to submit
     * @param comment the comment to submit
     * @param asUser optional - masquerade as this user when submitting the grade or comment. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @return the response body from the submission
     */
    public String submitGradeOrCommentForSectionAssignment(String sectionId, String assignmentId, String userId, String grade, String comment, String asUser) {
        URI uri = SECTION_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId, assignmentId, userId);
        return submitGradeOrComment(uri, grade, comment, asUser);
    }

    public String submitGradeOrCommentForSectionAssignment(String sectionId, String assignmentId, GradeDataWrapper gradeDataWrapper, String asUser) {
        URI uri = SECTION_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId, assignmentId, "update_grades");
        return submitGradeOrComment(uri, gradeDataWrapper, asUser);
    }

    /**
     * Submits a grade or comment for an assignment submission.
     *
     * @param courseId the Canvas course id
     * @param assignmentId the Canvas assignment id
     * @param userId the Canvas user id (or sis_login_id)
     * @param grade the grade to submit
     * @param comment the comment to submit
     * @param asUser optional - masquerade as this user when submitting the grade or comment. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @return the response body from the submission
     */
    public String submitGradeOrCommentForCourseAssignment(String courseId, String assignmentId, String userId, String grade, String comment, String asUser) {
        URI uri = COURSE_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId, userId);
        return submitGradeOrComment(uri, grade, comment, asUser);
    }

    public String submitGradeOrCommentForCourseAssignment(String courseId, String assignmentId, GradeDataWrapper gradeDataWrapper, String asUser) {
        URI uri = COURSE_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, assignmentId, "update_grades");
        return submitGradeOrComment(uri, gradeDataWrapper, asUser);
    }

    /**
     * Submits a grade or comment for an assignment submission.
     *
     * @param uri the URI for the assignment submission
     * @param grade the grade to submit
     * @param comment the comment to submit
     * @param asUser optional - masquerade as this user when submitting the grade or comment. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @return the response body from the submission
     */
    private String submitGradeOrComment(URI uri, String grade, String comment, String asUser) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("comment[text_comment]", comment);
            multiValueMap.add("submission[posted_grade]", grade);

            HttpEntity<MultiValueMap<String, String>> updateRequest = new HttpEntity<>(multiValueMap, headers);
            // TODO: need the correct response type here, as well as return value
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, updateRequest, String.class);
            log.debug("responseEntity: {}", responseEntity);

            if (responseEntity != null) {
                return responseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    private String submitGradeOrComment(URI uri, GradeDataWrapper gradeDataWrapper, String asUser) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<GradeDataWrapper> updateRequest = new HttpEntity<>(gradeDataWrapper, headers);
            // TODO: need the correct response type here, as well as return value
            ResponseEntity<String> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, updateRequest, String.class);
            log.debug("responseEntity: {}", responseEntity);

            if (responseEntity != null) {
                return responseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }
}