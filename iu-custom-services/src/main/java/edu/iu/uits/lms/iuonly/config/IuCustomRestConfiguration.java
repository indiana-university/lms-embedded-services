package edu.iu.uits.lms.iuonly.config;

/*-
 * #%L
 * lms-canvas-iu-custom-services
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

import edu.iu.uits.lms.common.oauth.CustomJwtAuthenticationConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;
import static edu.iu.uits.lms.iuonly.IuCustomConstants.READ_SCOPE;
import static edu.iu.uits.lms.iuonly.IuCustomConstants.WRITE_SCOPE;

@EnableWebSecurity
@Configuration
public class IuCustomRestConfiguration {

    @Order(1)
    @Bean
    public SecurityFilterChain iuCustomRestFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/rest/iu/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/rest/iu/file/**").permitAll()
                        .requestMatchers("/rest/iu/honorlock/**").permitAll()
                        .requestMatchers("/rest/iu/**").hasAnyAuthority(READ_SCOPE, WRITE_SCOPE)
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(new CustomJwtAuthenticationConverter())));

        return http.build();
    }

    @Profile(IUCUSTOMREST_PROFILE + " & swagger")
    @Order(1)
    @Bean
    public SecurityFilterChain iuCustomApiFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/iu/**")
                .authorizeHttpRequests((authz) -> authz
                        .requestMatchers("/api/iu/**").permitAll()
                );

        return http.build();
    }
}
