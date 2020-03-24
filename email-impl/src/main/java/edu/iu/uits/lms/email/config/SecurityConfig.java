package edu.iu.uits.lms.email.config;

import lombok.extern.log4j.Log4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Log4j
public class SecurityConfig {

    @Configuration
    @Order(1)
    public static class EmailSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {
        public static final String PATH_TO_OPEN = "/**";

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.antMatcher(PATH_TO_OPEN)
                  .authorizeRequests().anyRequest().permitAll();

            //Need to disable csrf so that we can use POST via REST
            http.csrf().disable();

            //Need to disable the frame options so we can embed this in another tool
            http.headers().frameOptions().disable();

            http.exceptionHandling().accessDeniedPage("/accessDenied");
        }
    }
}
