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
import edu.iu.uits.lms.canvas.model.Announcement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.File;
import java.net.URI;
import java.util.List;

/**
 * Service for interacting with Announcements via the Canvas API.  Announcements are actually
 * DiscussionTopics with some special handling in Canvas. This service utilizes the
 * DiscussionTopic API for only announcement handling
 */
@Service
@Slf4j
public class AnnouncementService extends SpringBaseService {
    private static final String BASE_URI = "{url}/courses/{course_id}/discussion_topics";
    private static final String ANNOUNCEMENT_URI = BASE_URI + "/{announcement_id}";

    private static final UriTemplate BASE_TEMPLATE = new UriTemplate(BASE_URI);
    private static final UriTemplate ANNOUNCEMENT_TEMPLATE = new UriTemplate(ANNOUNCEMENT_URI);

    /**
     * Get a specific announcement from a specific course
     * @param announcementId Announcement id
     * @param courseId Course id
     * @return the Canvas announcement with the given ID in the given course
     */
    public Announcement getAnnouncement(String announcementId, String courseId) {
        URI uri = ANNOUNCEMENT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, announcementId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("only_announcements", true);

        HttpEntity<Announcement> announcementEntity = this.restTemplate.getForEntity(builder.build().toUri(), Announcement.class);
        log.debug("announcementEntity: {}", announcementEntity);
        return announcementEntity.getBody();
    }

    /**
     * Get all announcements for the given course
     * @param courseId Course id
     * @return a list of all the Announcements in a given course
     */
    public List<Announcement> getAnnouncementsForCourse(String courseId) {
        URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("only_announcements", true);

        return doGet(builder.build().toUri(), Announcement[].class);
    }

    /**
     *
     * @param courseId Course id where announcement will be created
     * @param announcement nnouncement data to create
     * @param isPodcastEnabled
     * @param asUser optional - masquerade as this user when creating the announcement. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @param fileName the name of your file (ie report.txt)
     * @param file optional if you want to add an attachment to the announcement. Leave null if not
     * @return create a new announcement in Canvas for the given course
     */
    public Announcement createAnnouncement(String courseId, Announcement announcement, boolean isPodcastEnabled,
                                           String asUser, String fileName, File file) {

        URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("title", announcement.getTitle());
        map.add("message", announcement.getMessage());
        map.add("podcast_enabled", isPodcastEnabled);
        map.add("podcast_has_student_posts", announcement.isPodcastHasStudentPosts());
        map.add("allow_rating", announcement.isAllowRating());
        map.add("only_graders_can_rate", announcement.isOnlyGradersCanRate());
        map.add("sort_by_rating", announcement.isSortByRating());
        map.add("published", announcement.isPublished());
        map.add("is_announcement", true);

        if (asUser != null) {
            map.add("as_user_id", asUser);
        }

        if (file != null && file.length() > 0) {
            String attachmentFileName = fileName != null ? fileName : file.getName();

            FileSystemResource fileAsResource = new FileSystemResource(file) {
                @Override
                public String getFilename() {
                    return attachmentFileName;
                }
            };

            map.add("attachment", fileAsResource);
        }

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        try {
            HttpEntity<Announcement> announcementsEntity = restTemplate.postForEntity(builder.build().toUri(), requestEntity, Announcement.class);
            HttpStatusCode responseStatus = ((ResponseEntity<Announcement>) announcementsEntity).getStatusCode();

            if (HttpStatus.OK.equals(responseStatus)) {
                return announcementsEntity.getBody();
            } else {
                throw new RuntimeException("Error creating announcement. Request to Canvas was not successful. Response code: "
                        + responseStatus + ", reason: " + ((HttpStatus)responseStatus).getReasonPhrase()
                        + ", entity: " + announcementsEntity);
            }

        } catch (Exception ex) {
            // Most likely, this exception will be related to the attachment. Canvas seems to have a hard time
            // with large attachments.
            log.error("An error occurred attempting to create an announcement in Canvas for course: " + courseId, ex);
            throw new RuntimeException("Announcement was not created for course: " + courseId);
        }
    }
}
