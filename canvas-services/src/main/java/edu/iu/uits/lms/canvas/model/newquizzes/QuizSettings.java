package edu.iu.uits.lms.canvas.model.newquizzes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class QuizSettings implements Serializable {
    private String calculatorType;
    private boolean filterIpAddress;
    private Map<String, List<List<String>>> filters;
    private String oneAtATimeType;
    private boolean allowBacktracking;
    private boolean shuffleAnswers;
    private boolean shuffleQuestions;
    private boolean requireStudentAccessCode;
    private String studentAccessCode;
    private boolean hasTimeLimit;
    private Long sessionTimeLimitInSeconds;
    private MultipleAttemptsSettings multipleAttempts;
    private ResultViewSettings resultViewSettings;
}
