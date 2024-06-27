package edu.iu.uits.lms.lti.config;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import edu.iu.uits.lms.common.it12logging.RestSecurityLoggingConfig;
import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static edu.iu.uits.lms.lti.LTIConstants.LTIREST_PROFILE;
import static edu.iu.uits.lms.lti.LTIConstants.READ_SCOPE;
import static edu.iu.uits.lms.lti.LTIConstants.WRITE_SCOPE;

@EnableWebSecurity
public class LtiRestConfiguration {

    @Order(1)
    @Bean("ltiRestFilterChain")
    public SecurityFilterChain ltiRestFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/rest/lti/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/rest/lti/**").hasAnyAuthority(READ_SCOPE, WRITE_SCOPE)
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())))
                .with(new RestSecurityLoggingConfig(), log -> {
                });

        return http.build();
    }

    @Profile(LTIREST_PROFILE + " & swagger")
    @Order(1)
    @Bean("ltiApiFilterChain")
    public SecurityFilterChain ltiApiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/lti/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/lti/**").permitAll()
                );

        return http.build();
    }
}
