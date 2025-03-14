package edu.iu.uits.lms.canvas.model.newquizzes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class Quiz implements Serializable {
    private String id;
    private String title;
    private String instructions;
    private String assignmentGroupId;
    private Integer pointsPossible;
    private Date dueAt;
    private Date lockAt;
    private Date unlockAt;
    private boolean published;
    private String gradingType;
    private QuizSettings quizSettings;
}
