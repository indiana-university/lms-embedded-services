package edu.iu.uits.lms.email.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

public class EmailRestConfiguration {
    @Profile("emailrest")
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4999)
    public static class EmailRestWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/email/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/rest/email/**")
                    .access("hasAuthority('SCOPE_email:send')")
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());
        }
    }

    @Profile("emailrest & swagger")
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4998)
    public static class EmailApiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/api/email/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/api/email/**").permitAll();
        }
    }
}
