package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
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
import edu.iu.uits.lms.canvas.model.DiscussionTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
public class DiscussionService extends SpringBaseService {
    private static final String BASE_URI = "{url}/courses/{course_id}/discussion_topics";
    private static final String TOPIC_URI = BASE_URI + "/{id}";

    private static final UriTemplate BASE_TEMPLATE = new UriTemplate(BASE_URI);
    private static final UriTemplate TOPIC_TEMPLATE = new UriTemplate(TOPIC_URI);

    /**
     *
     * @param courseId courseId to create discussion topic in
     * @param newDiscussionTopic the discussion topic to create
     * @param asUser optional - masquerade as this user when creating the discussion topic. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @return
     */
    public DiscussionTopic createDiscussionTopic(String courseId, DiscussionTopic newDiscussionTopic, String asUser) {
        if (courseId == null || newDiscussionTopic == null) {
            throw new IllegalArgumentException("Null courseId or newDiscussionTopic passed to createDiscussionTopic.");
        }

        DiscussionTopic savedDiscussionTopic = null;

        URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        if (asUser != null) {
            builder.queryParam("as_user_id", asUser);
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            newDiscussionTopic.setAnnouncement(false);

            HttpEntity<DiscussionTopic> newDiscussionTopicRequest = new HttpEntity<>(newDiscussionTopic, headers);
            HttpEntity<DiscussionTopic> newDiscussionTopicResponse = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.POST, newDiscussionTopicRequest, DiscussionTopic.class);
            log.debug("{}", newDiscussionTopicResponse);

            savedDiscussionTopic = newDiscussionTopicResponse.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error creating discussion topic", hcee);
            throw new RuntimeException("Error creating discussion topic", hcee);
        }

        return savedDiscussionTopic;
    }

    /**
     * Get all discussion topics for the given course
     * @param courseId Course id
     * @return a list of all the discussion topics in a given course
     */
    public List<DiscussionTopic> getDiscussionsForCourse(String courseId) {
        URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
//        builder.queryParam("only_announcements", false);

        return doGet(builder.build().toUri(), DiscussionTopic[].class);
    }

    public DiscussionTopic updateDiscussionTopic(String courseId, String topicId, String sis_login_id, String message) {
        URI uri = TOPIC_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, topicId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + sis_login_id);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("message", message);

            HttpEntity<MultiValueMap<String, String>> updateRequest = new HttpEntity<>(multiValueMap, headers);
            HttpEntity<DiscussionTopic> responseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, updateRequest, DiscussionTopic.class);
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
