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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.BlueprintAssociatedCourse;
import edu.iu.uits.lms.canvas.model.BlueprintCourseUpdateStatus;
import edu.iu.uits.lms.canvas.model.BlueprintMigration;
import edu.iu.uits.lms.canvas.model.BlueprintMigrationStatus;
import edu.iu.uits.lms.canvas.model.BlueprintRestriction;
import edu.iu.uits.lms.canvas.model.BlueprintTemplate;
import edu.iu.uits.lms.canvas.model.BlueprintUpdateStatus;
import edu.iu.uits.lms.canvas.model.Course;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BlueprintService extends SpringBaseService {

    private String BASE_URI = "{url}/courses/{course_id}";
    private String BLUEPRINT_URI = BASE_URI + "/blueprint_templates/{template_id}";
    private String BLUEPRINT_SUBSCRIPTIONS_URI = BASE_URI + "/blueprint_subscriptions/{template_id}";


    private UriTemplate GET_BY_COURSE_AND_TEMPLATE = new UriTemplate(BLUEPRINT_URI);
    private UriTemplate GET_COURSES = new UriTemplate(BLUEPRINT_URI + "/associated_courses");
    private UriTemplate UPDATE_BP_COURSES = new UriTemplate(BLUEPRINT_URI + "/update_associations");
    private UriTemplate BEGIN_MIGRATION = new UriTemplate(BLUEPRINT_URI + "/migrations");
    private UriTemplate UPDATE_COURSE = new UriTemplate(BASE_URI);

    private UriTemplate SUBSCRIPTIONS = new UriTemplate(BLUEPRINT_SUBSCRIPTIONS_URI + "/migrations");

    /**
     * Get the blueprint template
     * @param courseId CourseId
     * @param templateId templateId
     * @return BlueprintTemplate
     */
    public BlueprintTemplate getTemplate(String courseId, String templateId) {
        URI uri = GET_BY_COURSE_AND_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, templateId);
        log.debug("uri: {}", uri);
        HttpEntity<BlueprintTemplate> template = this.restTemplate.getForEntity(uri, BlueprintTemplate.class);
        log.debug("Template: {}", template);
        return template.getBody();
    }

    /**
     * Get all course associated with the blueprint course and template
     * @param courseId Id of blueprint course
     * @param templateId Id of template
     * @return Associated courses
     */
    public List<BlueprintAssociatedCourse> getAssociatedCourses(String courseId, String templateId) {
        URI uri = GET_COURSES.expand(canvasConfiguration.getBaseApiUrl(), courseId, templateId);
        log.debug("uri: {}", uri);
        return doGet(uri, BlueprintAssociatedCourse[].class);
    }

    /**
     * Update the course associations for the given blueprint course and template
     * @param courseId Blueprint course
     * @param templateId Template to use
     * @param courseAdds Course Ids to add as associations
     * @param courseRemoves Course Ids to remove as associations
     * @return The status or the update
     */
    public BlueprintUpdateStatus updateAssociatedCourses(String courseId, String templateId, List<String> courseAdds,
                                                         List<String> courseRemoves) {
        URI uri = UPDATE_BP_COURSES.expand(canvasConfiguration.getBaseApiUrl(), courseId, templateId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        courseAdds.forEach(s -> builder.queryParam("course_ids_to_add[]", s));
        courseRemoves.forEach(s -> builder.queryParam("course_ids_to_remove[]", s));

        BlueprintUpdateStatus status = null;
        try {
            HttpEntity<BlueprintUpdateStatus> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, BlueprintUpdateStatus.class);
            log.debug("{}", response);
            status = response.getBody();
        } catch (HttpStatusCodeException rce) {
            log.error("uh oh", rce);
            ObjectMapper mapper = new ObjectMapper();

            try {
                status = mapper.readValue(rce.getResponseBodyAsString(), BlueprintUpdateStatus.class);
            } catch (IOException e) {
                log.error("uh oh", e);
            }
        }
        return status;
    }

    /**
     * Enable/Disable this course as a blueprint, providing the necessary restrictions
     * @param courseId CourseId
     * @param blueprintConfiguration {@link BlueprintConfiguration} details to save
     */
    public BlueprintCourseUpdateStatus saveBlueprintConfiguration(String courseId, BlueprintConfiguration blueprintConfiguration) {
        URI uri = UPDATE_COURSE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("uri: {}", uri);

        boolean hasObjectRestrictions = blueprintConfiguration.isHasObjectRestrictions();
        boolean enabled = blueprintConfiguration.isEnabled();
        Map<String, BlueprintRestriction> objectRestrictions = blueprintConfiguration.getObjectRestrictions();
        BlueprintRestriction restrictions = blueprintConfiguration.getRestrictions();

        BlueprintRestriction blueprintRestriction;
        if (restrictions != null) {
            blueprintRestriction = restrictions;
        } else if (hasObjectRestrictions) {
            blueprintRestriction = null;
        } else {
            blueprintRestriction = new BlueprintRestriction();
        }

        //If nothing is in the map, seed it with default options (all false)
        if (hasObjectRestrictions && (objectRestrictions == null || objectRestrictions.isEmpty())) {
            objectRestrictions = Arrays.stream(blueprintConfiguration.getDefaultRestrictionTypes())
                    .collect(Collectors.toMap(type -> type, type -> new BlueprintRestriction(), (a, b) -> b));
        }

        CourseBlueprintDetails cbd = new CourseBlueprintDetails(enabled, blueprintRestriction, hasObjectRestrictions, objectRestrictions);
        BlueprintCourseWrapper course = new BlueprintCourseWrapper(cbd);

        HttpEntity<BlueprintCourseWrapper> requestEntity = new HttpEntity<>(course);
        log.debug("Request: {}", requestEntity);

        BlueprintCourseUpdateStatus bcus = new BlueprintCourseUpdateStatus();

        try {
            HttpEntity<Course> response = restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Course.class);
            log.debug("Response: {}", response);
            bcus.setCourse(response.getBody());
        } catch (HttpStatusCodeException hsce) {
            log.error("uh oh", hsce);
            ObjectMapper mapper = new ObjectMapper();

            try {
                bcus = mapper.readValue(hsce.getResponseBodyAsString(), BlueprintCourseUpdateStatus.class);
            } catch (IOException e) {
                log.error("uh oh", e);
            }
        }
        return bcus;
    }

    /**
     * Initiate a blueprint migration
     * @param courseId Blueprint course
     * @param templateId Template to use
     * @param copySettings set to true if you want course settings copied over to associated courses.
     * @param sendNotifications set to true if you want Canvas to send a notification to the calling user when the sync completes.
     * @param asUser optional - masquerade as this user when performing the migration. Required for sending notifications. If you wish to use an sis_login_id,
     * 	         prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @param publishAfterSync - set to true if the courses are to be published after sync
     * @return A BlueprintMigrationStatus object that contains the BlueprintMigration or a status message
     */
    public BlueprintMigrationStatus performMigration(String courseId, String templateId, boolean copySettings,
                                                     boolean sendNotifications, String asUser, boolean publishAfterSync) {
        URI uri = BEGIN_MIGRATION.expand(canvasConfiguration.getBaseApiUrl(), courseId, templateId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

//        builder.queryParam("comment", "");
        builder.queryParam("send_notification", sendNotifications);
        builder.queryParam("copy_settings", copySettings);

        if (publishAfterSync) {
            builder.queryParam("publish_after_initial_sync", publishAfterSync);
        }

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        BlueprintMigrationStatus status = new BlueprintMigrationStatus();
        try {
            HttpEntity<BlueprintMigration> response = restTemplate.postForEntity(builder.build().toUri(), null, BlueprintMigration.class);
            status.setBlueprintMigration(response.getBody());
        } catch (HttpStatusCodeException rce) {
            log.error("uh oh", rce);
            ObjectMapper mapper = new ObjectMapper();

            try {
                status = mapper.readValue(rce.getResponseBodyAsString(), BlueprintMigrationStatus.class);
                log.debug("status: {}", status);
                log.error("Unable to begin a migration: " + status.getMessage(), rce);
            } catch (IOException e) {
                log.error("uh oh", e);
            }
        }
        return status;
    }

    /**
     * Get all migrations for a given course/template
     * @param courseId Blueprint course
     * @param templateId Template to use
     * @return List of BlueprintMigrations
     */
    public List<BlueprintMigration> getMigrations(String courseId, String templateId) {
        URI uri = BEGIN_MIGRATION.expand(canvasConfiguration.getBaseApiUrl(), courseId, templateId);
        return doGet(uri, BlueprintMigration[].class);
    }

    /**
     * Gat all migrations for a given associated course
     * @param courseId "Child" course
     * @param subscriptionId Template to use
     * @return List of BlueprintMigration objects
     */
    public List<BlueprintMigration> getSubscriptions(String courseId, String subscriptionId) {
        URI uri = SUBSCRIPTIONS.expand(canvasConfiguration.getBaseApiUrl(), courseId, subscriptionId);
        return doGet(uri, BlueprintMigration[].class);
    }

    @Data
    @AllArgsConstructor
    private class BlueprintCourseWrapper {
        private CourseBlueprintDetails course;
    }

    @Data
    @AllArgsConstructor
    private class CourseBlueprintDetails {
        private boolean blueprint;
        private BlueprintRestriction blueprint_restrictions;
        private boolean use_blueprint_restrictions_by_object_type;
        private Map<String, BlueprintRestriction> blueprint_restrictions_by_object_type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BlueprintConfiguration {
        boolean enabled;
        BlueprintRestriction restrictions;
        boolean hasObjectRestrictions;
        Map<String, BlueprintRestriction> objectRestrictions;
        String[] defaultRestrictionTypes;
    }
}
