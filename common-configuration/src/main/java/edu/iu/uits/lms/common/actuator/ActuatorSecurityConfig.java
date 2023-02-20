package edu.iu.uits.lms.common.actuator;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class ActuatorSecurityConfig {

    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 10)
    public static class ActuatorSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/actuator/**")
                  .and().authorizeRequests()
                  .antMatchers("/actuator/health").permitAll()
                  .antMatchers("/**").hasRole("LMS_REST_ADMINS")
                  .and()
                  .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                  .and()
                  .oauth2ResourceServer()
                  .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }

    }
}
