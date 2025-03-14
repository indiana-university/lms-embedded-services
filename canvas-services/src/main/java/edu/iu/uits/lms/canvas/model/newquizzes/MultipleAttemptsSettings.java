package edu.iu.uits.lms.canvas.model.newquizzes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class MultipleAttemptsSettings {
    private boolean multipleAttemptsEnabled;
    private boolean attemptLimit;
    private Integer maxAttempts;
    private String scoreToKeep;
    private boolean coolingPeriod;
    private Long coolingPeriodSeconds;
}
