package edu.iu.uits.lms.iuonly.config;

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;

@Profile(IUCUSTOMREST_PROFILE)
@Configuration
@Slf4j
public class IuOnlyJpaRestConfig implements RepositoryRestConfigurer {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config, CorsRegistry cors) {
        log.debug("configureRepositoryRestConfiguration()");
        //  This is needed to allow the "ids" to be served up via the
        //  @RepositoryRestResource annotation (by default, it is suppressed)
        config.exposeIdsFor(AuthorizedUser.class);
        config.setBasePath("/rest/iu");

        RepositoryRestConfigurer.super.configureRepositoryRestConfiguration(config, cors);
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategy.RepositoryDetectionStrategies.ANNOTATED);
    }
}