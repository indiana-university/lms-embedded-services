package edu.iu.uits.lms.lti.service;

import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.model.KeyPair;
import edu.iu.uits.lms.lti.repository.KeyPairRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class Lti13Service {

   @Autowired
   private KeyPairRepository keyPairRepository = null;

   public RSAKey getJKS() {
      KeyPair pair = keyPairRepository.findFirstByOrderByIdAsc();

      if (pair == null) {
         throw new RuntimeException("No keys to be able to sign for the access token");
      }

      RSAKey key = KeyServiceUtil.convert(pair);

      return key;
   }


}