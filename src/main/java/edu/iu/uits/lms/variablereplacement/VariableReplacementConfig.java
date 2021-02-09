package edu.iu.uits.lms.variablereplacement;

import canvas.config.EnableCanvasClient;
import edu.iu.uits.lms.common.variablereplacement.RoleResolver;
import edu.iu.uits.lms.common.variablereplacement.VariableReplacementService;
import iuonly.config.EnableIuOnlyClient;
import org.springframework.context.annotation.Bean;

@EnableIuOnlyClient
@EnableCanvasClient
public class VariableReplacementConfig {

   @Bean
   public RoleResolver roleResolver(){
      return new RoleResolverImpl();
   }

   @Bean
   public VariableReplacementService variableReplacementService(){
      return new VariableReplacementServiceImpl();
   }
}
