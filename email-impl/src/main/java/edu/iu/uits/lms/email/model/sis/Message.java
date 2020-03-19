package edu.iu.uits.lms.email.model.sis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Message implements Serializable {

   public enum PRIORITY {
      LOW, NORMAL, HIGH
   }

   @NonNull
   private String from;
   private String signatureAddress;
   @NonNull
   private String subject;
   @NonNull
   private String body;

   /**
    * Defaults to text/html if not supplied
    */
   private String contentType;
   private String testEmailAddress;
   @NonNull
   private List<Recipient> recipients;
   private List<Attachment> attach;

   private PRIORITY priority = PRIORITY.NORMAL;

}
