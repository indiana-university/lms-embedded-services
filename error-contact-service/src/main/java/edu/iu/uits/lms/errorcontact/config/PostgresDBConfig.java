package edu.iu.uits.lms.errorcontact.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableJpaRepositories(
        entityManagerFactoryRef = "errorContactPostgresdbEntityMgrFactory",
        transactionManagerRef = "errorContactPostgresdbTransactionMgr",
        basePackages = {
                "edu.iu.uits.lms.errorcontact.repository"
        })

@EnableTransactionManagement
public class PostgresDBConfig {


    @Bean(name = "errorContactPostgresdbEntityMgrFactory")
    public LocalContainerEntityManagerFactoryBean errorContactPostgresdbEntityMgrFactory(
            final EntityManagerFactoryBuilder builder,
            final DataSource dataSource) {
        // dynamically setting up the hibernate properties for each of the datasource.
        final Map<String, String> properties = new HashMap<>();
        return builder
                .dataSource(dataSource)
                .properties(properties)
                .packages("edu.iu.uits.lms.errorcontact.model")
                .build();
    }

    @Bean(name = "errorContactPostgresdbTransactionMgr")
    public PlatformTransactionManager errorContactPostgresdbTransactionMgr(
            @Qualifier("errorContactPostgresdbEntityMgrFactory") final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
