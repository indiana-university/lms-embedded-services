package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown=true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class UserProgress implements Serializable {
    private String id;
    private String displayName;
    private String avatarImageUrl;
    private String pronouns;
    private CourseProgress progress;
}
