package edu.iu.uits.lms.lti.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Map;

@Data
@Builder
@Setter(AccessLevel.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Lti13Config {

   public enum PrivacyLevel {
      @JsonProperty("anonymous")ANONYMOUS, @JsonProperty("public") PUBLIC};

   @AllArgsConstructor
   @Getter
   public enum Scopes {
      SCOPE_LINEITEM("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem"),
      SCOPE_RESULT_READONLY("https://purl.imsglobal.org/spec/lti-ags/scope/result.readonly"),
      SCOPE_SCORE("https://purl.imsglobal.org/spec/lti-ags/scope/score"),
      SCOPE_MEMBERSHIP_READONLY("https://purl.imsglobal.org/spec/lti-nrps/scope/contextmembership.readonly"),
      SCOPE_LINEITEM_READONLY("https://purl.imsglobal.org/spec/lti-ags/scope/lineitem.readonly"),
      SCOPE_PUBLIC_JWK_UPDATE("https://canvas.instructure.com/lti/public_jwk/scope/update");

      private String value;
   }

   @NotEmpty
   private String title;

   @NotEmpty
   private String description;

   @NotNull
   private PrivacyLevel privacyLevel;

   @NotEmpty
   private String oidcInitiationUrl;

   @NotEmpty
   private String targetLinkUri;

   private Collection<String> scopes;

   private Collection<Canvas13Extension> extensions;

   private Object publicJwk;

   private String publicJwkUrl;

   private Map<String, String> customFields;
}
