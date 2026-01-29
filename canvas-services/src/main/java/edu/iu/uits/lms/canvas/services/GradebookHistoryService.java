package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import edu.iu.uits.lms.canvas.helpers.CanvasDateFormatUtil;
import edu.iu.uits.lms.canvas.model.SubmissionVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class GradebookHistoryService extends SpringBaseService {

    private static final String CANVAS_BASE_URI = "{url}";
    private static final String GRADEBOOK_HISTORY_URI = CANVAS_BASE_URI +  "/courses/{course_id}/gradebook_history/feed";

    private static final UriTemplate GRADEBOOK_HISTORY_TEMPLATE = new UriTemplate(GRADEBOOK_HISTORY_URI);


    /**
     * Retrieve submission versions for a given course, assignment, and/or user.
     *
     * @param courseId The Canvas course ID to query.
     * @param assignmentId (Optional) The Canvas assignment ID to filter results. If null, all assignments are included.
     * @param userId (Optional) The Canvas user ID to filter results. If null, all users are included.
     * @return List of SubmissionVersion objects matching the provided filters.
     */
    protected List<SubmissionVersion> getSubmissionVersions(String courseId, String assignmentId, String userId) {
        URI baseUri = GRADEBOOK_HISTORY_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(baseUri);
        if (assignmentId != null) {
            builder.queryParam("assignment_id", assignmentId);
        }

        if (userId != null) {
            builder.queryParam("user_id", userId);
        }

        URI uri = builder.build().toUri();
        log.debug("{}", uri);
        return doGet(uri, SubmissionVersion[].class);
    }

    /**
     * Retrieve submission versions for a given course, assignment, and/or user, with an option to return only the latest (by graded_at) submission per assignment/user/id.
     *
     * @param courseId The Canvas course ID to query.
     * @param assignmentId (Optional) The Canvas assignment ID to filter results. If null, all assignments are included.
     * @param userId (Optional) The Canvas user ID to filter results. If null, all users are included.
     * @param latestSubmissionsOnly If true, only the latest (by graded_at) submission version per assignment/user/id is returned; if false, all versions are returned.
     * @return List of SubmissionVersion objects matching the provided filters and recency criteria.
     */
    public List<SubmissionVersion> getSubmissionVersions(String courseId, String assignmentId, String userId, boolean latestSubmissionsOnly) {
        List<SubmissionVersion> submissionVersions = getSubmissionVersions(courseId, assignmentId, userId);

        if (latestSubmissionsOnly) {
            // When grouped by assignmentId, userId, id, there could be multiple versions. We want only the most recent version basedo n the gradedAt date.
            return  submissionVersions.stream()
                    .collect(
                            Collectors.groupingBy(
                                    sv -> sv.getAssignmentId() + "-" + sv.getUserId() + "-" + sv.getId(),
                                    Collectors.maxBy(
                                            Comparator.comparing(
                                                sv -> CanvasDateFormatUtil.safeString2OffsetDateTime(sv.getGradedAt()),
                                                Comparator.nullsFirst(Comparator.naturalOrder())
                                            )
                                    )
                            )
                    )
                    .values()
                    .stream()
                    .filter(java.util.Optional::isPresent)
                    .map(java.util.Optional::get)
                    .toList();
        } else {
            return submissionVersions;
        }

    }

}
