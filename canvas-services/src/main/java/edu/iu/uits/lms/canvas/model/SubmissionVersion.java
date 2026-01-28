package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class SubmissionVersion {
    private String id;
    private String userId;
    private String submittedAt;
    private String currentGrade;
    private String score;
    private String workflowState;
    private String assignmentId;
    private String assignmentVisible;
    private String gradedAt;

}
