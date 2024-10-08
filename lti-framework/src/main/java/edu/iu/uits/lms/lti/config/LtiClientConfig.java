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
import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import edu.iu.uits.lms.lti.service.LmsDefaultGrantedAuthoritiesMapper;
import edu.iu.uits.lms.lti.service.Lti13Service;
import edu.iu.uits.lms.lti.service.LtiAuthorizationService;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import uk.ac.ox.ctl.lti13.KeyPairService;
import uk.ac.ox.ctl.lti13.SingleKeyPairService;
import uk.ac.ox.ctl.lti13.TokenRetriever;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import javax.sql.DataSource;
import java.security.KeyPair;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ComponentScan(basePackages = "edu.iu.uits.lms.lti",
      excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE,
            classes = {GlobalErrorHandlerConfig.class, ApplicationErrorController.class}))
@EnableTransactionManagement
@EnableJpaRepositories(entityManagerFactoryRef = "ltiEntityMgrFactory",
      transactionManagerRef = "ltiTransactionMgr",
      basePackageClasses = LtiAuthorizationRepository.class)
@EnableConfigurationProperties(OAuth2ClientProperties.class)
@Slf4j
public class LtiClientConfig implements ImportAware {

   @Lazy
   @Autowired
   private LtiAuthorizationService ltiAuthorizationService = null;

   @Lazy
   @Autowired
   private Lti13Service lti13Service = null;

   private List<String> toolKeys;

   private String toolKeyPrefix;

   @Value("${app.env}")
   private String env;

   @Autowired
   private LtiClientRegistrationProperties ltiClientRegistrationProperties;

   @Lazy
   @Autowired
   private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   @Override
   public void setImportMetadata(AnnotationMetadata annotationMetadata) {
      Map<String, Object> attributeMap = annotationMetadata
            .getAnnotationAttributes(EnableLtiClient.class.getName());
      AnnotationAttributes attributes = AnnotationAttributes.fromMap(attributeMap);

      String[] keys = attributes.getStringArray("toolKeys");
      toolKeys = Arrays.asList(keys);

      toolKeyPrefix = attributes.getString("toolKeyPrefix");
   }

   @Bean
   public NamesRoleService namesRoleService(OAuth2ClientProperties properties) throws JOSEException {
      RSAKey jks = lti13Service.getJKS();
      KeyPair keyPair = jks.toKeyPair();
      KeyPairService keyPairService = new SingleKeyPairService(keyPair, jks.getKeyID());
      TokenRetriever tokenRetriever = new TokenRetriever(keyPairService);
      return new NamesRoleService(clientRegistrationRepository(properties), tokenRetriever);
   }

   @Bean
   public LmsDefaultGrantedAuthoritiesMapper lmsDefaultGrantedAuthoritiesMapper() {
      return new LmsDefaultGrantedAuthoritiesMapper(defaultInstructorRoleRepository);
   }

   @Bean
   public ClientRegistrationRepository clientRegistrationRepository(OAuth2ClientProperties properties) {
      return new LmsClientRegistrationRepository(ltiClientRegistrationProperties, properties, ltiAuthorizationService, env, toolKeys, toolKeyPrefix);
   }

   @ConditionalOnMissingBean
   @Bean(name = "ltiDataSource")
   @ConfigurationProperties(prefix = "spring.datasource")
   public DataSource dataSource(DataSourceProperties properties) {
      log.info("dataSource()");
      return properties.initializeDataSourceBuilder().build();
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
