package edu.iu.uits.lms.email.rest;

import edu.iu.uits.lms.email.service.EmailService;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

@SpringBootConfiguration
@ComponentScan(basePackageClasses = EmailRestController.class)
public class RestTestConfig {

   @MockBean
   public EmailService emailService;

}
