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

    private static final UriTemplate SECTION_TEMPLATE = new UriTemplate(SECTION_URI);
    private static final UriTemplate CROSSLIST_TEMPLATE = new UriTemplate(CROSSLIST_URI);
    private static final UriTemplate CROSSLIST_BASE_TEMPLATE = new UriTemplate(CROSSLIST_BASE_URI);
    private static final UriTemplate STUDENT_SECTION_ENROLLMENT_TEMPLATE = new UriTemplate(STUDENT_SECTION_ENROLLMENT_URI);

    // feel free to pass in "sis_section_id:1234" for the id if you need the SIS id instead of Canvas's section id
    public Section getSection(String id) {
        URI uri = SECTION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);
        log.debug("uri: {}", uri);

        try {
            HttpEntity<Section> sectionEntity = this.restTemplate.getForEntity(uri, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            if (sectionEntity != null) {
                return sectionEntity.getBody();
            }
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
            HttpEntity<Section> sectionEntity = this.restTemplate.postForEntity(uri, null, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            if (sectionEntity != null) {
                Section section = sectionEntity.getBody();

                if (section != null) {
                    if (section.getId() == null) {
// We got back an object here, but everything in it was most likely null, so we didn't really get back a good section!
                        section = null;
                    }
                }

                return section;
            }
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
            HttpEntity<Section> sectionEntity = this.restTemplate.exchange(uri, HttpMethod.DELETE, null, Section.class);
            log.debug("sectionEntity: {}", sectionEntity);

            if (sectionEntity != null) {
                Section section = sectionEntity.getBody();

                if (section != null) {
                    if (section.getId() == null) {
// We got back an object here, but everything in it was most likely null, so we didn't really get back a good section!
                        section = null;
                    }
                }

                return section;
            }
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

        builder.queryParam("type[]", "StudentEnrollment");
        builder.queryParam("per_page", "50");
        builder.queryParam("state[]", "active");

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

            HttpEntity<Section> updateSectionNameAndSisCourseIdResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, requestEntity, Section.class);
            log.debug("{}", updateSectionNameAndSisCourseIdResponse);

            ResponseEntity<Section> responseEntity = (ResponseEntity<Section>) updateSectionNameAndSisCourseIdResponse;

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode() + ", reason: " + responseEntity.getStatusCode().getReasonPhrase()
                        + ", body: " + responseEntity.getBody());
            } else {
                return updateSectionNameAndSisCourseIdResponse.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error updating section name and sis_course_id", hcee);
        }

        return null;
    }
}
