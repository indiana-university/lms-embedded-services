package edu.iu.uits.lms.common.variablereplacement;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@Slf4j
public class VariableReplacementConfig {

   @ConditionalOnMissingBean
   @Bean
   public RoleResolver roleResolver() {
      log.debug("Registering DefaultRoleResolverImpl bean");
      return new DefaultRoleResolverImpl();
   }

   @ConditionalOnMissingBean
   @Bean
   public VariableReplacementService variableReplacementService() {
      log.debug("Registering DefaultVariableReplacementServiceImpl bean");
      return new DefaultVariableReplacementServiceImpl();
   }
}
