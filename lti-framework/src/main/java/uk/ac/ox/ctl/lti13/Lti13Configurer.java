package uk.ac.ox.ctl.lti13;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.context.SecurityContextRepository;
import uk.ac.ox.ctl.lti13.security.oauth2.OAuthAuthenticationFailureHandler;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcLaunchFlowAuthenticationProvider;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.TargetLinkUriAuthenticationSuccessHandler;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OAuth2AuthorizationRequestRedirectFilter;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OAuth2LoginAuthenticationFilter;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OIDCInitiatingLoginRequestResolver;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.OptimisticAuthorizationRequestRepository;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.web.StateAuthorizationRequestRepository;

import java.time.Duration;
import java.util.Collections;


/**
 *
 * <h2>Shared Objects Used</h2>
 *
 * The following shared objects are used:
 *
 * <ul>
 * <li>{@link ClientRegistrationRepository}</li>
 * </ul>
 */
public class Lti13Configurer extends AbstractHttpConfigurer<Lti13Configurer, HttpSecurity> {

    protected String ltiPath = "/lti";
    protected String loginPath = "/login";
    protected String loginInitiationPath = "/login_initiation";
    protected ApplicationEventPublisher applicationEventPublisher;
    protected GrantedAuthoritiesMapper grantedAuthoritiesMapper;
    protected boolean limitIpAddresses;
    protected SecurityContextRepository securityContextRepository;


    public Lti13Configurer ltiPath(String ltiPath) {
        this.ltiPath = ltiPath;
        return this;
    }

    public Lti13Configurer loginPath(String loginPath) {
        this.loginPath = loginPath;
        return this;
    }

    public Lti13Configurer loginInitiationPath(String loginInitiationPath) {
        this.loginInitiationPath = loginInitiationPath;
        return this;
    }

    public Lti13Configurer applicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        return this;
    }

    public Lti13Configurer grantedAuthoritiesMapper(GrantedAuthoritiesMapper grantedAuthoritiesMapper) {
        this.grantedAuthoritiesMapper = grantedAuthoritiesMapper;
        return this;
    }

    /**
     * This security context repository to persist the authentication in. This is useful if you want to use 
     * HTTP sessions for authentication.
     */
    public Lti13Configurer setSecurityContextRepository(SecurityContextRepository securityContextRepository) {
        this.securityContextRepository = securityContextRepository;
        return this;
    }

    /**
     * Using this may cause problems for users who are behind a proxy or NAT setup that uses different IP addresses
     * for different requests, even if they are close together in time.
     * 
     * @param limitIpAddresses if true then ensure that all the OAuth requests for a LTI launch come from the same IP
     */
    public Lti13Configurer limitIpAddresses(boolean limitIpAddresses) {
        this.limitIpAddresses = limitIpAddresses;
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(HttpSecurity http) {
        // Allow LTI launches to bypass CSRF protection
        CsrfConfigurer<HttpSecurity> configurer = http.getConfigurer(CsrfConfigurer.class);
        if (configurer != null) {
            // I'm not sure about this.
            configurer.ignoringRequestMatchers(ltiPath + "/**");
        }
        // In the future we should use CSP to limit the domains that can embed this tool
        HeadersConfigurer<HttpSecurity> headersConfigurer = http.getConfigurer(HeadersConfigurer.class);
        if (headersConfigurer != null) {
            headersConfigurer.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable);
        }
    }

    @Override
    public void configure(HttpSecurity http) {
        ClientRegistrationRepository clientRegistrationRepository = Lti13ConfigurerUtils.getClientRegistrationRepository(http);

        OidcLaunchFlowAuthenticationProvider oidcLaunchFlowAuthenticationProvider = configureAuthenticationProvider(http);
        OptimisticAuthorizationRequestRepository authorizationRequestRepository = configureRequestRepository();
        // This handles step 1 of the IMS SEC
        // https://www.imsglobal.org/spec/security/v1p0/#step-1-third-party-initiated-login
        http.addFilterAfter(configureInitiationFilter(clientRegistrationRepository, authorizationRequestRepository), LogoutFilter.class);
        // This handles step 3 of the IMS SEC
        // https://www.imsglobal.org/spec/security/v1p0/#step-3-authentication-response
        http.addFilterAfter(configureLoginFilter(clientRegistrationRepository, oidcLaunchFlowAuthenticationProvider, authorizationRequestRepository), AbstractPreAuthenticatedProcessingFilter.class);
    }

    protected OptimisticAuthorizationRequestRepository configureRequestRepository() {
        HttpSessionOAuth2AuthorizationRequestRepository sessionRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
        StateAuthorizationRequestRepository stateRepository = new StateAuthorizationRequestRepository(Duration.ofMinutes(1));
        stateRepository.setLimitIpAddress(limitIpAddresses);
        return new OptimisticAuthorizationRequestRepository( sessionRepository, stateRepository );
    }

    protected OidcLaunchFlowAuthenticationProvider configureAuthenticationProvider(HttpSecurity http) {
        OidcLaunchFlowAuthenticationProvider oidcLaunchFlowAuthenticationProvider = new OidcLaunchFlowAuthenticationProvider();

        http.authenticationProvider(oidcLaunchFlowAuthenticationProvider);
        if (grantedAuthoritiesMapper != null) {
            oidcLaunchFlowAuthenticationProvider.setAuthoritiesMapper(grantedAuthoritiesMapper);
        }
        return oidcLaunchFlowAuthenticationProvider;
    }

    protected OAuth2AuthorizationRequestRedirectFilter configureInitiationFilter(ClientRegistrationRepository clientRegistrationRepository,  OptimisticAuthorizationRequestRepository authorizationRequestRepository) {
        OIDCInitiatingLoginRequestResolver resolver = new OIDCInitiatingLoginRequestResolver(clientRegistrationRepository, ltiPath+ loginInitiationPath);
        OAuth2AuthorizationRequestRedirectFilter filter = new OAuth2AuthorizationRequestRedirectFilter(resolver);
        filter.setAuthorizationRequestRepository(authorizationRequestRepository);
        return filter;
    }

    protected OAuth2LoginAuthenticationFilter configureLoginFilter(ClientRegistrationRepository clientRegistrationRepository, OidcLaunchFlowAuthenticationProvider oidcLaunchFlowAuthenticationProvider, OptimisticAuthorizationRequestRepository authorizationRequestRepository) {
        // This filter handles the actual authentication and behaviour of errors
        OAuth2LoginAuthenticationFilter loginFilter = new OAuth2LoginAuthenticationFilter(clientRegistrationRepository, ltiPath+ loginPath);
        // This is to find the URL that we should redirect the user to.
        TargetLinkUriAuthenticationSuccessHandler successHandler = new TargetLinkUriAuthenticationSuccessHandler(authorizationRequestRepository);
        loginFilter.setAuthenticationSuccessHandler(successHandler);
        // This is just so that you can get better error messages when something goes wrong.
        OAuthAuthenticationFailureHandler failureHandler = new OAuthAuthenticationFailureHandler();
        loginFilter.setAuthenticationFailureHandler(failureHandler);
        loginFilter.setAuthorizationRequestRepository(authorizationRequestRepository);
        ProviderManager authenticationManager = new ProviderManager(Collections.singletonList(oidcLaunchFlowAuthenticationProvider));
        if (applicationEventPublisher != null) {
            authenticationManager.setAuthenticationEventPublisher(new DefaultAuthenticationEventPublisher(applicationEventPublisher));
        }
        if (securityContextRepository != null) {
            loginFilter.setSecurityContextRepository(securityContextRepository);
        }
        loginFilter.setAuthenticationManager(authenticationManager);
        return loginFilter;
    }

}
