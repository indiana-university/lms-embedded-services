package edu.iu.uits.lms.common.it12logging;

import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

public class RestSecurityLoggingConfig extends AbstractHttpConfigurer<RestSecurityLoggingConfig, HttpSecurity> {

   @Override
   public void init(HttpSecurity http) throws Exception {
      // https://docs.spring.io/spring-security/site/docs/5.0.x/reference/html/jc.html
      http.authorizeRequests()
            .anyRequest().authenticated()
            .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
               public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
                  fsi.setPublishAuthorizationSuccess(true);
                  return fsi;
               }
            });
   }
}
