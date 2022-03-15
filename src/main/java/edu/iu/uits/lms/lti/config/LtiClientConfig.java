package edu.iu.uits.lms.lti.config;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import com.nimbusds.jose.JOSEException;
import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import edu.iu.uits.lms.lti.service.Lti13Service;
import edu.iu.uits.lms.lti.service.LtiAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.ImportAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.context.support.GenericWebApplicationContext;
import uk.ac.ox.ctl.lti13.KeyPairService;
import uk.ac.ox.ctl.lti13.SingleKeyPairService;
import uk.ac.ox.ctl.lti13.TokenRetriever;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@ComponentScan(basePackages = "edu.iu.uits.lms.lti",
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {GlobalErrorHandlerConfig.class, ApplicationErrorController.class}))
@EnableJpaRepositories(entityManagerFactoryRef = "ltiEntityMgrFactory",
      transactionManagerRef = "ltiTransactionMgr",
      basePackageClasses = LtiAuthorizationRepository.class)
@Slf4j
public class LtiClientConfig implements ImportAware {

   @Lazy
   @Autowired
   private LtiAuthorizationService ltiAuthorizationService = null;

   @Lazy
   @Autowired
   private Lti13Service lti13Service = null;

   @Autowired
   private GenericWebApplicationContext context;

   private List<String> toolKeys;

   @Value("https://${canvas.host}")
   private String canvasBaseUrl;

   @Override
   public void setImportMetadata(AnnotationMetadata annotationMetadata) {
      Map<String, Object> attributeMap = annotationMetadata
            .getAnnotationAttributes(EnableLtiClient.class.getName());
      AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributeMap);

      String[] keys = attributes.getStringArray("toolKeys");
      toolKeys = Arrays.asList(keys);

      boolean enableClientRepository = attributes.getBoolean("enableClientRepository");

      if (enableClientRepository) {
         ClientRegistrationRepository clientRegistrationRepository = clientRegistrationRepository();
         context.registerBean(ClientRegistrationRepository.class, clientRegistrationRepository);

         boolean enableNamesRoleService = attributes.getBoolean("enableNamesRoleService");

         if (enableNamesRoleService) {
            try {
               KeyPair keyPair = lti13Service.getJKS().toKeyPair();
               KeyPairService keyPairService = new SingleKeyPairService(keyPair);
               TokenRetriever tokenRetriever = new TokenRetriever(keyPairService);
               context.registerBean(NamesRoleService.class, new NamesRoleService(clientRegistrationRepository, tokenRetriever));
            } catch (JOSEException e) {
               log.error("Unable to configure NamesRoleService", e);
            }
         }
      }


   }

   public ClientRegistrationRepository clientRegistrationRepository() {
      List<ClientRegistration> registrations = toolKeys.stream()
            .map(this::getCanvasBuilder)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

      return new InMemoryClientRegistrationRepository(registrations);
   }

   public ClientRegistration getCanvasBuilder(String toolKey) {
      ClientRegistration.Builder builder = ClientRegistration.withRegistrationId(toolKey)
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
            .authorizationGrantType(AuthorizationGrantType.IMPLICIT)
//      builder.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE);
//      builder.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS);

            .redirectUri("{baseUrl}/lti/login")
            .scope("openid")
            .authorizationUri(canvasBaseUrl + "/api/lti/authorize_redirect")
            .tokenUri(canvasBaseUrl + "/login/oauth2/token")
            .jwkSetUri(canvasBaseUrl + "/api/lti/security/jwks")

            // Issuer should always be https://canvas.instructure.com and not match your institutional/env url
            .issuerUri("https://canvas.instructure.com")
            .userNameAttributeName("sub")
            .clientName(toolKey);

      // Use toolKey to lookup client and secret
      LmsLtiAuthz ltiAuthz = ltiAuthorizationService.findByRegistrationActive(toolKey);
      if (ltiAuthz != null) {
         builder.clientId(ltiAuthz.getClientId())
               .clientSecret(ltiAuthz.getSecret());
      }
      return builder.build();
   }


   @ConditionalOnMissingBean
   @Bean(name = "ltiDataSource")
   @ConfigurationProperties(prefix = "spring.datasource")
   public DataSource dataSource() {
      log.info("dataSource()");
      return DataSourceBuilder.create().build();
   }

   @Bean(name = "ltiEntityMgrFactory")
   public LocalContainerEntityManagerFactoryBean ltiEntityMgrFactory(
         final EntityManagerFactoryBuilder builder,
         final DataSource dataSource) {
      // dynamically setting up the hibernate properties for each of the datasource.
      final Map<String, String> properties = new HashMap<>();
      return builder
            .dataSource(dataSource)
            .properties(properties)
            .packages("edu.iu.uits.lms.lti.model")
            .build();
   }

   @Bean(name = "ltiTransactionMgr")
   public PlatformTransactionManager ltiTransactionMgr(
         @Qualifier("ltiEntityMgrFactory") final EntityManagerFactory entityManagerFactory) {
      return new JpaTransactionManager(entityManagerFactory);
   }

}
