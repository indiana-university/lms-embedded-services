package edu.iu.uits.lms.lti.model;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

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
