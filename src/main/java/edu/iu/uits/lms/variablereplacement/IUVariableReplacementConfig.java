package edu.iu.uits.lms.variablereplacement;

import edu.iu.uits.lms.canvas.config.EnableCanvasClient;
import edu.iu.uits.lms.common.variablereplacement.VariableReplacementService;
import edu.iu.uits.lms.iuonly.config.EnableIuOnlyClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@EnableIuOnlyClient
@EnableCanvasClient
@Slf4j
@Configuration
public class IUVariableReplacementConfig {

   public IUVariableReplacementConfig() {
      log.debug("IUVariableReplacementConfig()");
   }

   @Bean
   @Primary
   public VariableReplacementService variableReplacementService(){
      log.debug("Registering custom VariableReplacementServiceImpl");
      return new IUVariableReplacementServiceImpl();
   }
}
