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

import edu.iu.uits.lms.canvas.model.User;
import edu.iu.uits.lms.canvas.model.groups.CourseGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class GroupService extends SpringBaseService {
    private static final String BASE_URI = "{url}/";
    private static final String GROUP_URI = BASE_URI + "groups/{group_id}";
    private static final String GROUP_MEMBERSHIP_URI = GROUP_URI + "/users";
    private static final String COURSE_GROUPS_URI = BASE_URI + "courses/{course_id}/groups";
    private static final String ACCOUNT_GROUPS_URI = BASE_URI + "accounts/{account_id}/groups";

    private static final UriTemplate GROUP_TEMPLATE = new UriTemplate(GROUP_URI);
    private static final UriTemplate GROUP_MEMBERSHIP_TEMPLATE = new UriTemplate(GROUP_MEMBERSHIP_URI);
    private static final UriTemplate COURSE_GROUPS_TEMPLATE = new UriTemplate(COURSE_GROUPS_URI);
    private static final UriTemplate ACCOUNT_GROUPS_TEMPLATE = new UriTemplate(ACCOUNT_GROUPS_URI);

    /**
     *
     * @param groupId
     * @return the {@link CourseGroup} with the specified groupId
     */
    public CourseGroup getCourseGroup(String groupId) {
        if (groupId == null) {
            return null;
        }

        URI uri = GROUP_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), groupId);
        log.debug("{}", uri);

        try {
            HttpEntity<CourseGroup> groupEntity = this.restTemplate.getForEntity(uri, CourseGroup.class);
            log.debug("{}", groupEntity);

            if (groupEntity != null) {
                return groupEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("error getting a course group: ", hcee);
        }

        return null;
    }

    /**
     *
     * @param courseId
     * @return a list of the {@link CourseGroup}s in the given course
     */
    public List<CourseGroup> getGroupsForCourse(String courseId) {
        List<CourseGroup> groups = new ArrayList<>();
        if (courseId != null) {
            URI uri = COURSE_GROUPS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
            log.debug("{}", uri);
            groups = doGet(uri, CourseGroup[].class);
        }

        return groups;
    }

    /**
     *
     * @param groupId
     * @param excludeInactive Whether to filter out inactive users from the results
     * @return a list of {@link User}s who are members of the given group
     */
    public List<User> getUsersInGroup(String groupId, boolean excludeInactive) {
        if (groupId == null) {
            return new ArrayList<>();
        }

        URI uri = GROUP_MEMBERSHIP_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), groupId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("exclude_inactive", excludeInactive);

        return doGet(builder.build().toUri(), User[].class);

    }
}
