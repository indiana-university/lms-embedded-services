package edu.iu.uits.lms.canvas.services;

import edu.iu.uits.lms.canvas.config.CanvasConfiguration;
import edu.iu.uits.lms.canvas.model.SubmissionVersion;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

@SpringBootTest(classes = {GradebookHistoryService.class})
public class GradebookHistoryServiceTest {

    @MockitoSpyBean
    private GradebookHistoryService gradebookHistoryService;

    @MockitoBean
    @Qualifier("CanvasRestTemplate")
    protected RestTemplate restTemplate;

    @MockitoBean
    @Qualifier("restTemplateNoBuffer")
    protected RestTemplate restTemplateNoBuffer;

    @MockitoBean
    @Qualifier("RestTemplateHttpComponent")
    protected RestTemplate restTemplateHttpComponent;

    @MockitoBean
    protected CanvasConfiguration canvasConfiguration;

    /**
     * Helper method to create a SubmissionVersion object.
     * @param assignmentId
     * @param userId
     * @param id
     * @param score
     * @param gradedAt
     * @return
     */
    private SubmissionVersion createSubmissionVersion(String assignmentId, String userId, String id, String score, String gradedAt) {
        SubmissionVersion sv = new SubmissionVersion();
        sv.setAssignmentId(assignmentId);
        sv.setUserId(userId);
        sv.setId(id);
        sv.setScore(score);
        sv.setGradedAt(gradedAt);
        return sv;
    }

    @Test
    void testGradebookSubmissionVersions() {
        // Create 3 SubmissionVersion objects with same assignmentId, userId, and id, but different scores and gradedAt values
        String assignmentId = "A1";
        String user1 = "u1";
        String user2 = "u2";
        String user3 = "u3";
        String submission1 = "as1";
        String submission2 = "as2";
        String submission3 = "as3";

        SubmissionVersion sv1 = createSubmissionVersion(assignmentId, user1, submission1, "85.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv2 = createSubmissionVersion(assignmentId, user1, submission1, "90.0", "2024-01-02T11:00:00Z");
        SubmissionVersion sv3 = createSubmissionVersion(assignmentId, user1, submission1, "95.0", "2024-01-03T12:00:00Z");

        SubmissionVersion sv4 = createSubmissionVersion(assignmentId, user2, submission2, "85.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv5 = createSubmissionVersion(assignmentId, user2, submission2, "90.0", "2024-01-02T11:00:00Z");
        SubmissionVersion sv6 = createSubmissionVersion(assignmentId, user2, submission2, "95.0", "2024-01-03T12:00:00Z");

        SubmissionVersion sv7 = createSubmissionVersion(assignmentId, user3, submission3, "85.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv8 = createSubmissionVersion(assignmentId, user3, submission3, "90.0", "2024-01-02T11:00:00Z");
        SubmissionVersion sv9 = createSubmissionVersion(assignmentId, user3, submission3, "95.0", "2024-01-03T12:00:00Z");

        List<SubmissionVersion> rawSubmissionVersions = List.of(sv1, sv2, sv3, sv4, sv5, sv6, sv7, sv8, sv9);

        doReturn(rawSubmissionVersions).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());

        List<SubmissionVersion> allResults = gradebookHistoryService.getSubmissionVersions(assignmentId, null, null, false);
        Assertions.assertNotNull(allResults);
        Assertions.assertEquals(9, allResults.size());

        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions(assignmentId, null, null, true);
        Assertions.assertNotNull(recentResults);
        Assertions.assertEquals(3, recentResults.size());
        // Make sure that the results have the expected score and gradedAt values (the most recent ones)
        for (SubmissionVersion recentResult : recentResults) {
            Assertions.assertEquals("95.0", recentResult.getScore());
            Assertions.assertEquals("2024-01-03T12:00:00Z", recentResult.getGradedAt());
        }
    }

    @Test
    void testNoSubmissionsReturned() {
        doReturn(List.of()).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> allResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, false);
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertNotNull(allResults);
        Assertions.assertNotNull(recentResults);
        Assertions.assertTrue(allResults.isEmpty());
        Assertions.assertTrue(recentResults.isEmpty());
    }

    @Test
    void testSingleSubmissionPerGroup() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv2 = createSubmissionVersion("A1", "u2", "s2", "90.0", "2024-01-02T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> allResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, false);
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(2, allResults.size());
        Assertions.assertEquals(2, recentResults.size());
        List<String> scores = recentResults.stream().map(SubmissionVersion::getScore).toList();
        Assertions.assertTrue(scores.containsAll(List.of("80.0", "90.0")));
    }

    @Test
    void testMultipleAssignmentsAndUsers() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv2 = createSubmissionVersion("A2", "u1", "s2", "85.0", "2024-01-02T10:00:00Z");
        SubmissionVersion sv3 = createSubmissionVersion("A1", "u2", "s3", "90.0", "2024-01-03T10:00:00Z");
        SubmissionVersion sv4 = createSubmissionVersion("A2", "u2", "s4", "95.0", "2024-01-04T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2, sv3, sv4);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(4, recentResults.size());
        List<SubmissionVersion> allResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, false);
        Assertions.assertEquals(4, allResults.size());
    }

    @Test
    void testNullGradedAtValues() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", null);
        SubmissionVersion sv2 = createSubmissionVersion("A1", "u1", "s1", "90.0", "2024-01-02T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(1, recentResults.size());
        Assertions.assertEquals("90.0", recentResults.get(0).getScore());
    }

    @Test
    void testNonChronologicalInput() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "2024-01-03T10:00:00Z");
        SubmissionVersion sv2 = createSubmissionVersion("A1", "u1", "s1", "90.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv3 = createSubmissionVersion("A1", "u1", "s1", "95.0", "2024-01-02T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2, sv3);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(1, recentResults.size());
        Assertions.assertEquals("80.0", recentResults.get(0).getScore());
        Assertions.assertEquals("2024-01-03T10:00:00Z", recentResults.get(0).getGradedAt());
    }

    @Test
    void testDuplicateGradedAtValues() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "2024-01-01T10:00:00Z");
        SubmissionVersion sv2 = createSubmissionVersion("A1", "u1", "s1", "90.0", "2024-01-01T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(1, recentResults.size());
        // Should be one of the two, as both have the same gradedAt
        Assertions.assertTrue(recentResults.get(0).getScore().equals("80.0") || recentResults.get(0).getScore().equals("90.0"));
    }

//    @Test
//    void testFilteringByAssignmentIdOrUserId() {
//        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "2024-01-01T10:00:00Z");
//        SubmissionVersion sv2 = createSubmissionVersion("A2", "u2", "s2", "90.0", "2024-01-02T10:00:00Z");
//        List<SubmissionVersion> raw = List.of(sv1, sv2);
//        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
//        List<SubmissionVersion> resultsA1 = gradebookHistoryService.getSubmissionVersions("C1", "A1", null, true);
//        Assertions.assertEquals(1, resultsA1.size());
//        Assertions.assertEquals("A1", resultsA1.get(0).getAssignmentId());
//        List<SubmissionVersion> resultsU2 = gradebookHistoryService.getSubmissionVersions("C1", null, "u2", true);
//        Assertions.assertEquals(1, resultsU2.size());
//        Assertions.assertEquals("u2", resultsU2.get(0).getUserId());
//    }

    @Test
    void testMalformedGradedAtValues() {
        SubmissionVersion sv1 = createSubmissionVersion("A1", "u1", "s1", "80.0", "not-a-date");
        SubmissionVersion sv2 = createSubmissionVersion("A1", "u1", "s1", "90.0", "2024-01-02T10:00:00Z");
        List<SubmissionVersion> raw = List.of(sv1, sv2);
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(1, recentResults.size());
        Assertions.assertEquals("90.0", recentResults.get(0).getScore());
    }

    @Test
    void testLargeDatasetPerformance() {
        List<SubmissionVersion> raw = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            raw.add(createSubmissionVersion("A1", "u" + i, "s" + i, String.valueOf(50 + i), "2024-01-01T10:00:00Z"));
            raw.add(createSubmissionVersion("A1", "u" + i, "s" + i, String.valueOf(60 + i), "2024-01-02T10:00:00Z"));
        }
        doReturn(raw).when(gradebookHistoryService).getSubmissionVersions(anyString(), any(), any());
        List<SubmissionVersion> recentResults = gradebookHistoryService.getSubmissionVersions("C1", null, null, true);
        Assertions.assertEquals(1000, recentResults.size());
        for (SubmissionVersion sv : recentResults) {
            Assertions.assertTrue(Integer.parseInt(sv.getScore()) >= 60 && Integer.parseInt(sv.getScore()) < 1060);
        }
    }
}
