package edu.iu.uits.lms.email.model.sis;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result implements Serializable {
   private boolean success;
   private String message;

}
