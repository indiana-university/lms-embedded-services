package edu.iu.uits.lms.canvas.model;

/*-
 * #%L
 * LMS Canvas Services
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ContentMigrationCreateWrapper implements Serializable {

   private String migrationType;
   private Settings settings;
   private DateShiftOptions dateShiftOptions;
   private Boolean selectiveImport;
   private Map<String, List<String>> select;

   @JsonIgnoreProperties(ignoreUnknown = true)
   @JsonInclude(JsonInclude.Include.NON_NULL)
   @AllArgsConstructor
   @NoArgsConstructor
   @Getter
   @Setter
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class Settings implements Serializable {
      private String fileUrl;
      private String contentExportId;
      private String sourceCourseId;
      private String folderId;
      private Boolean overwriteQuizzes;
      private Integer questionBankId;
      private String questionBankName;
      private Integer insertIntoModuleId;
      private String insertIntoModuleType;
      private Integer insertIntoModulePosition;
      private Integer moveToAssignmentGroupId;
      private List<String> importerSkips;

   }

   @JsonIgnoreProperties(ignoreUnknown = true)
   @JsonInclude(JsonInclude.Include.NON_NULL)
   @AllArgsConstructor
   @NoArgsConstructor
   @Getter
   @Setter
   @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
   public static class DateShiftOptions implements Serializable {
      private Boolean shiftDates;
      private String oldStartDate;
      private String oldEndDate;
      private String newStartDate;
      private String newEndDate;
      private Map<String, String> daySubstitutions;
      private boolean removeDates;
   }
}
