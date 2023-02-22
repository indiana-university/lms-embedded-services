package edu.iu.uits.lms.common.it12logging;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextListener;

@Configuration
public class AuditConfig {
   @Bean
   public RequestContextListener requestContextListener(){
      return new RequestContextListener();
   }
}
