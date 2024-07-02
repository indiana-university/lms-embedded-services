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

import edu.iu.uits.lms.canvas.model.DiscussionTopic;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Service
@Slf4j
public class DiscussionService extends SpringBaseService {
    private static final String BASE_URI = "{url}/courses/{course_id}/discussion_topics";

    private static final UriTemplate BASE_TEMPLATE = new UriTemplate(BASE_URI);

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
}
