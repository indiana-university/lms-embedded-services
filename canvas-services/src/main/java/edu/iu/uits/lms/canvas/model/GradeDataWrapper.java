package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
public class GradeDataWrapper implements Serializable {
    private Map<String, GradeDetails> gradeData;

    public void addGradeDetail(String studentId, String postedGrade, String textComment) {
        if (gradeData == null) {
            gradeData = new HashMap<>();
        }
        GradeDetails details = new GradeDetails(postedGrade, textComment);
        gradeData.put(studentId, details);
    }

    @Data
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class GradeDetails implements Serializable {
        private String postedGrade;
        private String textComment;
    }
}
