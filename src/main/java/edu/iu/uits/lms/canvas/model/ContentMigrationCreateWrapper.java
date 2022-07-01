package edu.iu.uits.lms.canvas.model;

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
