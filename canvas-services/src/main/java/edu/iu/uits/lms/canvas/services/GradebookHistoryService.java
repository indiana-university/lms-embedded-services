package edu.iu.uits.lms.canvas.services;

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
     * Retrieve submission versions for a given course, assignment, and/or user, with an option to return only the most recent submission per assignment/user/id.
     *
     * @param courseId The Canvas course ID to query.
     * @param assignmentId (Optional) The Canvas assignment ID to filter results. If null, all assignments are included.
     * @param userId (Optional) The Canvas user ID to filter results. If null, all users are included.
     * @param recentSubmissionsOnly If true, only the most recent submission version per assignment/user/id is returned; if false, all versions are returned.
     * @return List of SubmissionVersion objects matching the provided filters and recency criteria.
     */
    public List<SubmissionVersion> getSubmissionVersions(String courseId, String assignmentId, String userId, boolean recentSubmissionsOnly) {
        List<SubmissionVersion> submissionVersions = getSubmissionVersions(courseId, assignmentId, userId);

        if (recentSubmissionsOnly) {
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
