package edu.iu.uits.lms.lti.controller;

import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.service.Lti13Service;
import org.springframework.beans.factory.annotation.Autowired;

public class ConfigurationController {

   @Autowired
   private Lti13Service lti13Service;

   protected RSAKey getJKS() {
      RSAKey jks = lti13Service.getJKS();
      return jks;
   }

}
