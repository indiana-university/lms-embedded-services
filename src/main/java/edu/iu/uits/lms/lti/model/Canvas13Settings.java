package edu.iu.uits.lms.lti.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.Collection;

@Data
@Builder
@Setter(AccessLevel.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Canvas13Settings {

    private String text;
    private String iconUrl;
    private String selectionHeight;
    private String selectionWidth;
    private Lti13Config.PrivacyLevel privacyLevel;
    private Collection<Canvas13Placement> placements;

}
