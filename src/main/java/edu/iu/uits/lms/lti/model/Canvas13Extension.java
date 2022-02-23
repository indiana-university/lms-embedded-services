package edu.iu.uits.lms.lti.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

@Data
@Builder
@Setter(AccessLevel.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Canvas13Extension {

    // Constant for the instructure platform
    public static final String INSTRUCTURE = "canvas.instructure.com";

    private String domain;

    private String toolId;

    private String platform;

    private Lti13Config.PrivacyLevel privacyLevel;

    private Canvas13Settings settings;

}
