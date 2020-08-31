package edu.iu.uits.lms.email.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
@Slf4j
public class SecurityConfig {

    @Configuration
    @Order(1)
    public static class EmailSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/**")
                  .access("hasAuthority('SCOPE_email:send')")
                  .and()
                  .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                  .and()
                  .oauth2ResourceServer()
                  .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }

        @Override
        public void configure(WebSecurity web) throws Exception {
            // ignore paths specified
            web.ignoring().antMatchers("/actuator/**");
        }
    }
}
