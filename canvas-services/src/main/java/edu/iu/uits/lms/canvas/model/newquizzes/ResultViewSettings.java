package edu.iu.uits.lms.canvas.model.newquizzes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.util.Date;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class ResultViewSettings {
    private boolean resultViewRestricted;
    private boolean displayPointsAwarded;
    private boolean displayPointsPossible;
    private boolean displayItems;
    private boolean displayItemResponse;
    private String displayItemResponseQualifier;
    private Date showItemResponsesAt;
    private Date hideItemResponsesAt;
    private boolean displayItemResponseCorrectness;
    private String displayItemResponseCorrectnessQualifier;
    private Date showItemResponseCorrectnessAt;
    private Date hideItemResponseCorrectnessAt;
    private boolean displayItemCorrectAnswer;
    private boolean displayItemFeedback;
}
