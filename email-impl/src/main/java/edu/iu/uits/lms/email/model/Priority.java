package edu.iu.uits.lms.email.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Priority {

   LOW(5),
   NORMAL(3),
   HIGH(1);

   private int priority;
}
