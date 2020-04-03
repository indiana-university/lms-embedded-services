package edu.iu.uits.lms.email.model.sis;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Attachment implements Serializable {
   public enum TYPE {
      text, binary
   }

   @NonNull
   private TYPE type;
   private String fileName;
   @NonNull
   private String contentType;
   @NonNull
   private String content;
}
