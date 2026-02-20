package edu.iu.uits.lms.iuonly.model.tps;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthUserInformationDto {
   private Long authUserId;
   private String username;
   private String displayName;
   private String email;
   /**
    * List of tool names for which the user has active permission(s)
    */
   private List<String> tools;
   private boolean active;
}
