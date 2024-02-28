package edu.iu.uits.lms.canvas.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Module implements Serializable {

  private String id;
  private String workflowState;
  private String position;
  private String name;
  private Date unlockAt;
  private boolean requireSequentialProgress;
  private List<String> prerequisiteModuleIds;
  private String itemsCount;
  private String itemsUrl;
  private List<ModuleItemCreateWrapper> items;
  private String state;
  private Date completedAt;
  private Boolean publishFinalGrade;
  private boolean published;

}
