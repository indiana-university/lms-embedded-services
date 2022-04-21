package edu.iu.uits.lms.email.rest;

import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.email.service.LmsEmailTooBigException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/rest/email")
@Slf4j
public class EmailRestController {

   @Autowired
   private EmailService emailService;

   @PostMapping("/sendViaSecondary")
   @PreAuthorize("hasAuthority('SCOPE_email:send')")
   public void sendUnsignedEmail(@RequestBody EmailDetails emailDetails,
                                 @RequestParam(name = "unsignedToEmailToUseInPreProd", required = false, defaultValue = "") String unsignedToEmailToUseInPreProd) throws LmsEmailTooBigException, MessagingException {


      emailService.sendEmail(emailDetails, false, unsignedToEmailToUseInPreProd, EmailService.SENDING_METHOD.SECONDARY);
   }

   @PostMapping("/send")
   @PreAuthorize("hasAuthority('SCOPE_email:send')")
   public void sendEmail(@RequestBody EmailDetails emailDetails,
                         @RequestParam(name = "digitallySign", required = false, defaultValue = "true") boolean digitallySign,
                         @RequestParam(name = "unsignedToEmailToUseInPreProd", required = false, defaultValue = "") String unsignedToEmailToUseInPreProd) throws LmsEmailTooBigException, MessagingException {

      emailService.sendEmail(emailDetails, digitallySign, unsignedToEmailToUseInPreProd, EmailService.SENDING_METHOD.PRIMARY);
   }

}
