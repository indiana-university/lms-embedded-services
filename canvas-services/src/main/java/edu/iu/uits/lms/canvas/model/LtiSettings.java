package edu.iu.uits.lms.canvas.model;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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
import lombok.Data;

import java.io.Serializable;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LtiSettings {

   private String clientId;
   private String name;
   private String privacyLevel;
   private String consumerKey;
   private String sharedSecret;
   private String description;
   private String url;
   private String domain;
   private String iconUrl;
   private String text;

   //TODO - how to handle customFields
   //   private Map<String, String> customFields;

   private AccountNavigation accountNavigation;
   private UserNavigation userNavigation;
   private CourseHomeSubNavigation courseHomeSubNavigation;
   private CourseNavigation courseNavigation;
   private EditorButton editorButton;
   private HomeworkSubmission homeworkSubmission;
   private LinkSelection linkSelection;
   private MigrationSelection migrationSelection;
   private ToolConfiguration toolConfiguration;
   private ResourceSelection resourceSelection;

   private String configType;
   private String configXml;
   private String configUrl;
   private boolean notSelectable;
   private boolean oauthCompliant;



   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class AccountNavigation implements Serializable {
      private String url;
      private boolean enabled;
      private String text;
      private String selectionWidth;
      private String selectionHeight;
      private String displayType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class UserNavigation implements Serializable {
      private String url;
      private boolean enabled;
      private String text;
      private String visibility;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class CourseHomeSubNavigation implements Serializable {
      private String url;
      private boolean enabled;
      private String text;
      private String iconUrl;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class CourseNavigation implements Serializable {
      private boolean enabled;
      private String text;
      private String visibility;
      private String windowTarget;

      @JsonProperty("default")
      private String defaults;
      private String displayType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class EditorButton implements Serializable {
      private String url;
      private boolean enabled;
      private String iconUrl;
      private String selectionWidth;
      private String selectionHeight;
      private String messageType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class HomeworkSubmission implements Serializable {
      private String url;
      private boolean enabled;
      private String text;
      private String messageType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class LinkSelection implements Serializable {
      private String url;
      private boolean enabled;
      private String text;
      private String messageType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class MigrationSelection implements Serializable {
      private String url;
      private boolean enabled;
      private String messageType;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class ToolConfiguration implements Serializable {
      private String url;
      private boolean enabled;
      private String messageType;
      private boolean preferSisEmail;
   }

   @Data
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class ResourceSelection implements Serializable {
      private String url;
      private boolean enabled;
      private String iconUrl;
      private String selectionWidth;
      private String selectionHeight;
   }

}
