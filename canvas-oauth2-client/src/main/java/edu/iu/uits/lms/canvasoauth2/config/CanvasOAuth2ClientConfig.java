package edu.iu.uits.lms.canvasoauth2.config;

/*-
 * #%L
 * LMS Canvas OAuth2 Client
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import edu.iu.uits.lms.canvasoauth2.repository.CanvasOAuth2AuthzRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.ac.ox.ctl.oauth2.client.web.method.annotation.OAuth2AuthorizedClientArgumentResolver;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Wires the shared JPA plumbing (DataSource / EntityManagerFactory / TransactionManager) for this
 * module's own entity/repository packages, mirroring {@code LtiClientConfig}'s
 * shared-datasource-reuse trick - plus the cross-cutting beans needed to make Canvas OAuth2
 * delegated access usable by a host tool: the {@link OAuth2AuthorizedClientArgumentResolver}
 * wiring (via {@link WebMvcConfigurer}) for controllers that want the current user's
 * {@code OAuth2AuthorizedClient} injected directly, and the {@link OAuth2AuthorizedClientManager}
 * that {@code CanvasOAuth2TokenInterceptor} (in canvas-services) relies on to resolve/refresh that
 * same user's Canvas OAuth2 access token for outbound Canvas API calls.
 * <p>
 * LTI-style client-registration/OAuth2 wiring (ImportAware, toolKeys, etc.) is added by a future
 * {@code @EnableCanvasOAuth2Client} annotation (a later task), not here.
 */
@ComponentScan(basePackages = "edu.iu.uits.lms.canvasoauth2")
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "canvasOAuth2EntityMgrFactory",
      transactionManagerRef = "canvasOAuth2TransactionMgr",
      basePackageClasses = CanvasOAuth2AuthzRepository.class)
@Slf4j
public class CanvasOAuth2ClientConfig {

   @ConditionalOnMissingBean
   @Bean(name = "canvasOAuth2DataSource")
   @ConfigurationProperties(prefix = "spring.datasource")
   public DataSource dataSource(DataSourceProperties properties) {
      log.info("dataSource()");
      return properties.initializeDataSourceBuilder().build();
   }

   @Bean(name = "canvasOAuth2EntityMgrFactory")
   public LocalContainerEntityManagerFactoryBean canvasOAuth2EntityMgrFactory(
         final EntityManagerFactoryBuilder builder,
         final DataSource dataSource) {
      // dynamically setting up the hibernate properties for each of the datasource.
      final Map<String, String> properties = new HashMap<>();
      return builder
            .dataSource(dataSource)
            .properties(properties)
            .packages("edu.iu.uits.lms.canvasoauth2.model")
            .build();
   }

   @Bean(name = "canvasOAuth2TransactionMgr")
   public PlatformTransactionManager canvasOAuth2TransactionMgr(
         @Qualifier("canvasOAuth2EntityMgrFactory") final EntityManagerFactory entityManagerFactory) {
      return new JpaTransactionManager(entityManagerFactory);
   }

   @Bean
   public WebMvcConfigurer canvasOAuth2WebMvcConfigurer(ClientRegistrationRepository clientRegistrationRepository,
                                                          OAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository) {
      return new WebMvcConfigurer() {
         @Override
         public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new OAuth2AuthorizedClientArgumentResolver(clientRegistrationRepository, canvasOAuth2AuthorizedClientRepository));
         }
      };
   }

   /**
    * Provides the {@link OAuth2AuthorizedClientManager} used by {@code CanvasOAuth2TokenInterceptor}
    * (via {@code canvasRestTemplateAsUser()} in canvas-services) to resolve/refresh the current LTI
    * user's Canvas OAuth2 access token.
    * <p>
    * {@link DefaultOAuth2AuthorizedClientManager} is used - not
    * {@code AuthorizedClientServiceOAuth2AuthorizedClientManager} - for two reasons: (1) it's the
    * only built-in manager whose constructor accepts an {@link OAuth2AuthorizedClientRepository},
    * which is what {@link CanvasOAuth2AuthorizedClientRepository} implements (the service-based
    * manager instead requires an {@code OAuth2AuthorizedClientService}, a different persistence
    * contract this module doesn't implement); and (2) it resolves the current
    * {@code HttpServletRequest}/{@code HttpServletResponse} from the {@code OAuth2AuthorizeRequest}
    * attributes map (falling back to the thread-bound {@code RequestContextHolder} if absent) - the
    * exact mechanism {@code CanvasOAuth2TokenInterceptor} relies on via its
    * {@code .attribute(HttpServletRequest.class.getName(), ...)} /
    * {@code .attribute(HttpServletResponse.class.getName(), ...)} calls.
    * <p>
    * The provider chain includes {@code .authorizationCode()} in addition to {@code .refreshToken()}.
    * This does NOT cause the manager to attempt a redirect/callback dance itself - that exchange only
    * ever happens in Spring Security's web filter chain (wired separately via {@code .oauth2Client(...)}
    * for tools that need the login/consent flow). All {@code AuthorizationCodeOAuth2AuthorizedClientProvider}
    * does is throw {@code ClientAuthorizationRequiredException} when no authorized client exists yet
    * for the current user, instead of the manager quietly returning {@code null}; either way,
    * {@code CanvasOAuth2TokenInterceptor} ends up seeing that failure (either as the thrown exception
    * propagating out of {@code authorize()}, or via its own null-check) and reacts by throwing
    * {@code ClientAuthorizationRequiredException} for the caller to handle - it never triggers a
    * redirect on its own since no {@code OAuth2AuthorizationRequestRedirectFilter} is in play when
    * this manager is invoked directly from a {@code ClientHttpRequestInterceptor}.
    *
    * @return a DefaultOAuth2AuthorizedClientManager backed by the shared ClientRegistrationRepository
    * and CanvasOAuth2AuthorizedClientRepository
    */
   @Bean
   public OAuth2AuthorizedClientManager canvasOAuth2AuthorizedClientManager(
         ClientRegistrationRepository clientRegistrationRepository,
         OAuth2AuthorizedClientRepository canvasOAuth2AuthorizedClientRepository) {
      OAuth2AuthorizedClientProvider authorizedClientProvider =
            OAuth2AuthorizedClientProviderBuilder.builder()
                  .authorizationCode()
                  .refreshToken()
                  .build();
      DefaultOAuth2AuthorizedClientManager authorizedClientManager =
            new DefaultOAuth2AuthorizedClientManager(clientRegistrationRepository, canvasOAuth2AuthorizedClientRepository);
      authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider);
      return authorizedClientManager;
   }

}
