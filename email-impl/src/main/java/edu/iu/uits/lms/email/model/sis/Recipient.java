package edu.iu.uits.lms.email.model.sis;

import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;

@Data
public class Recipient implements Serializable {

   public enum TYPE {
      TO, CC, BCC, REPLY
   }

   @NonNull
   private TYPE type;
//   private String emplid;
//   private String oprid;
   @NonNull
   private String address;

}
