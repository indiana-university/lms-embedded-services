package edu.iu.uits.lms.lti.config;

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

public class LtiRestConfiguration {
    @Profile("ltirest")
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 5000)
    public static class LtiRestWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/rest/lti/**")
                    .and()
                    .authorizeRequests()
                    .antMatchers("/rest/lti/**")
                    .access("hasAuthority('SCOPE_lti:read') or hasAuthority('SCOPE_lti:write')")
                    .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .oauth2ResourceServer()
                    .jwt().jwtAuthenticationConverter(new CustomJwtAuthenticationConverter());

            // Need to disable csrf so that we can use POST via REST
            http.csrf().disable();
        }
    }

    @Profile("ltirest & swagger")
    @Configuration
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 5001)
    public static class LtiApiWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.requestMatchers().antMatchers("/api/lti/**")
                  .and()
                  .authorizeRequests()
                  .antMatchers("/api/lti/**").permitAll();
        }
    }
}
