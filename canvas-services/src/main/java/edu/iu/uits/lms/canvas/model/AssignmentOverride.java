package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class AssignmentOverride implements Serializable {
    private String id;
    private String assignmentId;
    private String quizId;
    private String contextModuleId;
    private String discussionTopicId;
    private String wikiPageId;
    private String attachmentId;
    private String[] studentIds;
    private String groupId;
    private String courseSectionId;
    private String title;
    private String dueAt;
    private Boolean allDay;
    private String allDayDate;
    private String unlockAt;
    private String lockAt;

}
