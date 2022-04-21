package edu.iu.uits.lms.email;

import edu.iu.uits.lms.email.config.EmailServiceConfig;
import edu.iu.uits.lms.email.service.SignedEmailService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;

@TestConfiguration
public class EmailClientTestConfig {

   @MockBean
   private JavaMailSender javaMailSender;

   @MockBean
   private EmailServiceConfig emailServiceConfig;

   @MockBean
   private SignedEmailService signedEmailService;

}
