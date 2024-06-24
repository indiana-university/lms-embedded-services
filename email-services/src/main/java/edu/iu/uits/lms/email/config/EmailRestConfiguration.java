package edu.iu.uits.lms.email.config;

/*-
 * #%L
 * lms-email-service
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
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static edu.iu.uits.lms.email.EmailConstants.EMAILREST_PROFILE;
import static edu.iu.uits.lms.email.EmailConstants.SEND_SCOPE;

@EnableWebSecurity
public class EmailRestConfiguration {

    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4999)
    public SecurityFilterChain emailRestFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/rest/email/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/rest/email/**").hasAuthority(SEND_SCOPE)
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())))
                .with(new RestSecurityLoggingConfig(), log -> {});

        return http.build();
    }

    @Profile(EMAILREST_PROFILE + " & swagger")
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER - 4998)
    public SecurityFilterChain emailApiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/email/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/email/**").permitAll()
                );

        return http.build();
    }
}
