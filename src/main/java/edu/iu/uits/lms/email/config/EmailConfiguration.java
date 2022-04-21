package edu.iu.uits.lms.email.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = "edu.iu.uits.lms.email")
public class EmailConfiguration {

   @Bean
   public RestTemplate sisRestTemplate() {
      RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));

//        restTemplate.getInterceptors().add(new LoggingRequestInterceptor());
      return restTemplate;
   }

}
