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
import edu.iu.uits.lms.canvas.helpers.EnrollmentHelper;
import edu.iu.uits.lms.canvas.model.CourseSectionUpdateWrapper;
import edu.iu.uits.lms.canvas.model.Enrollment;
import edu.iu.uits.lms.canvas.model.Section;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
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

@Service
@Slf4j
public class SectionService extends SpringBaseService {

    private static final String BASE_URI = "{url}/sections";
    private static final String SECTION_URI = BASE_URI + "/{id}";
    private static final String CROSSLIST_BASE_URI = SECTION_URI + "/crosslist";
    private static final String CROSSLIST_URI = CROSSLIST_BASE_URI + "/{parent_id}";
    private static final String STUDENT_SECTION_ENROLLMENT_URI = BASE_URI + "/{sis_section_id}/enrollments";
    private static final String ALL_SECTION_ENROLLMENT_URI = BASE_URI + "/{section_id}/enrollments";

    private static final UriTemplate SECTION_TEMPLATE = new UriTemplate(SECTION_URI);
    private static final UriTemplate CROSSLIST_TEMPLATE = new UriTemplate(CROSSLIST_URI);
    private static final UriTemplate CROSSLIST_BASE_TEMPLATE = new UriTemplate(CROSSLIST_BASE_URI);
    private static final UriTemplate STUDENT_SECTION_ENROLLMENT_TEMPLATE = new UriTemplate(STUDENT_SECTION_ENROLLMENT_URI);
    private static final UriTemplate ALL_SECTION_ENROLLMENT_TEMPLATE = new UriTemplate(ALL_SECTION_ENROLLMENT_URI);

    // feel free to pass in "sis_section_id:1234" for the id if you need the SIS id instead of Canvas's section id
    public Section getSection(String id) {
        return getSection(id, null);
    }


    public Section getSection(String id, String[] includes) {
        URI baseUri = SECTION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
        if (includes != null) {
            for (String include : includes) {
                builder.queryParam("include[]", include);
            }
        }
        URI uri = builder.build().toUri();
        log.debug("uri: {}", uri);

        try {
            ResponseEntity<Section> sectionEntity = this.restTemplate.getForEntity(uri, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            return sectionEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error: ", hcee);
        }

        return null;
    }

    /**
     * Crosslist the given section into the given course
     * @param sectionId Section Id to add
     * @param parentCourseId Course that will contain the section
     * @return The updated Section
     */
    public Section crossList(String sectionId, String parentCourseId) {
        URI uri = CROSSLIST_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId, parentCourseId);
        log.debug("uri: {}", uri);

        try {
            ResponseEntity<Section> sectionEntity = this.restTemplate.postForEntity(uri, null, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            Section section = sectionEntity.getBody();

            if (section != null) {
                if (section.getId() == null) {
// We got back an object here, but everything in it was most likely null, so we didn't really get back a good section!
                    section = null;
                }
            }

            return section;
        } catch (HttpClientErrorException hcee) {
            log.error("Error: ", hcee);
        }

        return null;
    }

    /**
     * Undo cross-listing of a Section, returning it to its original course.
     * @param sectionId Section Id to remove
     * @return The updated Section
     */
    public Section decrossList(String sectionId) {
        URI uri = CROSSLIST_BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId);
        log.debug("uri: {}", uri);

        try {
            ResponseEntity<Section> sectionEntity = this.restTemplate.exchange(uri, HttpMethod.DELETE, null, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            Section section = sectionEntity.getBody();

            if (section != null) {
                if (section.getId() == null) {
// We got back an object here, but everything in it was most likely null, so we didn't really get back a good section!
                    section = null;
                }
            }

            return section;
        } catch (HttpClientErrorException hcee) {
            log.error("Error: ", hcee);
        }

        return null;
    }

    public List<Enrollment> getStudentSectionEnrollments(String sisSectionId) {
        final String sisSectionIdPath = "sis_section_id:" + sisSectionId;

        URI uri = STUDENT_SECTION_ENROLLMENT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sisSectionIdPath);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("type[]", EnrollmentHelper.TYPE_STUDENT);
        builder.queryParam("per_page", "50");
        builder.queryParam("state[]", CanvasConstants.ACTIVE_STATUS);

        return doGet(builder.build().toUri(), Enrollment[].class);
    }

    /**
     * Get all of the enrollments for a sectionId and states that are passed in, e.g. active, invited
     * @param sectionId the id of the section
     * @param states List of states. Passing in a null will return Canvas defaults.
     * @return
     */
    public List<Enrollment> getAllSectionEnrollmentsByIdAndState(String sectionId, List<String> states) {
        URI uri = ALL_SECTION_ENROLLMENT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("per_page", "50");

        if (states != null) {
            builder.queryParam("state[]", states);
        }

        return doGet(builder.build().toUri(), Enrollment[].class);
    }

    public Section updateSectionNameAndSisCourseId(String sectionId, CourseSectionUpdateWrapper courseSectionUpdateWrapper) {
        if (sectionId == null) {
            throw new IllegalArgumentException("Null id passed to updateSectionNameAndSisCourseId.");
        }

        URI uri = SECTION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), sectionId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("course_section[name]", courseSectionUpdateWrapper.getName());
        multiValueMap.add("course_section[sis_section_id]", courseSectionUpdateWrapper.getSisId());

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(multiValueMap, headers);

            ResponseEntity<Section> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, Section.class);
            log.debug("{}", responseEntity);

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + ((HttpStatus)responseEntity.getStatusCode()).getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            } else {
                return responseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating section name and sis_course_id", hcee);
        }

        return null;
    }
}
