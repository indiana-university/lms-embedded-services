package edu.iu.uits.lms.variablereplacement;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

@Component
public class VariableReplacementInfoContributor implements InfoContributor {

   @Override
   public void contribute(Info.Builder builder) {
      Package pkg = this.getClass().getPackage();
      String version =  pkg != null ? pkg.getImplementationVersion() : null;
      builder.withDetail("variable-replacement-service", version);
   }

}
