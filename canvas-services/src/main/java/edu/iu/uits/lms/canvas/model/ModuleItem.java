package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModuleItem implements Serializable {

    private String id;
    private String moduleId;
    private String position;
    private String title;
    private String indent;
    private String type;
    private String contentId;
    private String htmlUrl;
    private String url;
    private String pageUrl;
    private String externalUrl;
    private boolean newTab;
    private CompletionRequirement completionRequirement;
    private ContentDetails contentDetails;
    private boolean published;

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CompletionRequirement implements Serializable {
        private String type;
        private String minScore;
        private boolean completed;
    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class ContentDetails implements Serializable {
        private String pointsPossible;
        private String dueAt;
        private String unlockAt;
        private String lockAt;

    }

    @Data
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class Iframe implements Serializable {
        private String width;
        private String height;
    }
}
