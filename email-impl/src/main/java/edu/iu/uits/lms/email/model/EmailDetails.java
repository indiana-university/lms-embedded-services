package edu.iu.uits.lms.email.model;

import lombok.Data;

import java.util.List;

@Data
public class EmailDetails {
   private String[] recipients;
   private String subject;
   private String body;
   private List<EmailServiceAttachment> emailServiceAttachmentList;
   private boolean enableHtml;
   private Priority priority = Priority.NORMAL;
   private String from;
}
