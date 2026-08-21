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

import edu.iu.uits.lms.canvas.config.CanvasEnvironmentConfiguration;
import edu.iu.uits.lms.canvas.security.CanvasOAuth2TokenInterceptor;
import edu.iu.uits.lms.canvasoauth2.CanvasOAuth2Registration;
import edu.iu.uits.lms.canvasoauth2.repository.CanvasOAuth2AuthzRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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
import org.springframework.web.client.RestTemplate;
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
 * Implements {@link ImportAware} to read {@code @EnableCanvasOAuth2Client}'s required
 * {@code registrationIdSuffix} attribute, mirroring {@code LtiClientConfig}'s
 * {@code toolKeys}/{@code toolKeyPrefix} wiring exactly - see {@link #setImportMetadata}.
 * <p>
 * The {@code excludeFilters} below matter: {@code @EnableCanvasOAuth2Client} is itself
 * meta-annotated {@code @Configuration}, so ANY class anywhere under {@code edu.iu.uits.lms.canvasoauth2}
 * that carries it - including nested test-only fixtures used purely for reflection-based unit tests,
 * such as {@code CanvasOAuth2ClientConfigTest.DummyEnableCanvasOAuth2ClientHost} - is itself a
 * component-scan-discoverable "lite" configuration class. Since classpath component scanning sees
 * both {@code target/classes} and {@code target/test-classes}, an unfiltered scan here would let this
 * config's own {@code @ComponentScan} re-discover such a fixture during any OTHER test that boots a
 * real Spring context importing this class (e.g. {@code CanvasOAuth2AuthzRepositoryTest}), causing
 * Spring to process the fixture's {@code @EnableCanvasOAuth2Client} metadata too and potentially
 * deliver the WRONG importing class's {@code registrationIdSuffix} to {@link #setImportMetadata}.
 * A test class's compiled nested class always has "$" in its binary/fully-qualified name (e.g.
 * {@code CanvasOAuth2ClientConfigTest$DummyEnableCanvasOAuth2ClientHost}), and no class under this
 * scanned package ({@code edu.iu.uits.lms.canvasoauth2}) is ever nested, so excluding any class
 * whose name contains "$" reliably filters out test fixtures (present and future) without a
 * main-depends-on-test-class compile-time reference, which Maven's classpath separation wouldn't
 * allow anyway (main is compiled before, and without visibility into, test sources).
 * <p>
 * WARNING: this exclusion relies entirely on that invariant holding. If a future main-source class
 * under this package ever genuinely needs to be a nested {@code @Configuration}/{@code @Component}
 * (rather than top-level), this filter will SILENTLY exclude it too - no compile error, no obvious
 * test failure, just a bean that quietly never gets registered. Should that happen, the fix is to
 * either keep the class top-level, or narrow this regex (e.g. restrict it to a pattern that only
 * matches known test-fixture naming, or otherwise exclude only test-sourced classes) - not to remove
 * the exclusion outright, since that would reintroduce the cross-test contamination described above.
 */
@ComponentScan(basePackages = "edu.iu.uits.lms.canvasoauth2",
      excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = ".*\\$.*"))
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "canvasOAuth2EntityMgrFactory",
      transactionManagerRef = "canvasOAuth2TransactionMgr",
      basePackageClasses = CanvasOAuth2AuthzRepository.class)
@Slf4j
public class CanvasOAuth2ClientConfig implements ImportAware {

   private String registrationIdSuffix;

   @Override
   public void setImportMetadata(AnnotationMetadata annotationMetadata) {
      AnnotationAttributes attributes = AnnotationAttributes.fromMap(
            annotationMetadata.getAnnotationAttributes(EnableCanvasOAuth2Client.class.getName()));
      registrationIdSuffix = attributes.getString("registrationIdSuffix");
   }

   @Bean
   public CanvasOAuth2Registration canvasOAuth2Registration() {
      return new CanvasOAuth2Registration(registrationIdSuffix);
   }

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
    * (via {@link #canvasRestTemplateAsUser}, below) to resolve/refresh the current LTI user's
    * Canvas OAuth2 access token.
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

   /**
    * Creates a RestTemplate bean that authorizes Canvas API calls as the currently logged-in LTI
    * user (via Canvas OAuth2 delegated access) instead of the shared admin token. Only created
    * when the host tool has opted in with {@code canvas.oauth2.enabled=true}.
    * <p>
    * Lives here (not in canvas-services, which can't depend back on this module) so it can use the
    * {@link #canvasOAuth2Registration()} bean's per-tool registration id - the same id the host
    * tool's {@code application.yml} declares under {@code spring.security.oauth2.client.registration.*}
    * - rather than a hardcoded/stale default.
    *
    * @return a RestTemplate instance carrying a CanvasOAuth2TokenInterceptor
    */
   @Bean(name = "CanvasRestTemplateAsUser")
   @ConditionalOnProperty(prefix = "canvas.oauth2", name = "enabled", havingValue = "true")
   public RestTemplate canvasRestTemplateAsUser(OAuth2AuthorizedClientManager authorizedClientManager,
                                                 CanvasOAuth2Registration canvasOAuth2Registration) {
      RestTemplate restTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
      CanvasEnvironmentConfiguration.configureJackson(restTemplate);
      restTemplate.getInterceptors().add(
            new CanvasOAuth2TokenInterceptor(authorizedClientManager, canvasOAuth2Registration.getRegistrationId()));
      return restTemplate;
   }

}
