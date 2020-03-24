package edu.iu.uits.lms.email.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ConfigurationProperties(prefix = "sis.rest")
@PropertySource(value = {"classpath:env.properties",
      "classpath:default.properties",
      "${app.fullFilePath}/lms.properties",
      "${app.fullFilePath}/protected.properties",
      "${app.fullFilePath}/security.properties"}, ignoreResourceNotFound = true)
@Getter
@Setter
public class SignedEmailServiceConfig {
   private String url;
   private String region;
   private String apiToken;
}
