package edu.iu.uits.lms.lti.service;

import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.model.JwtKey;
import edu.iu.uits.lms.lti.model.KeyPair;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.security.PublicKey;

public class KeyServiceUtilTest {

   @Test
   public void testConversion2wow() throws Exception {
      KeyPair keyPair = new KeyPair();

      InputStream fileStream = this.getClass().getResourceAsStream("/private_key.txt");
      String privateKey = IOUtils.toString(fileStream, "UTF-8");

      InputStream fileStream2 = this.getClass().getResourceAsStream("/pub_key.txt");
      String publicKey = IOUtils.toString(fileStream2, "UTF-8");

      InputStream fileStream3 = this.getClass().getResourceAsStream("/kid.txt");
      String kid = IOUtils.toString(fileStream3, "UTF-8");

      keyPair.setPrivateKey(privateKey);
      keyPair.setPublicKey(publicKey);

      RSAKey key = KeyServiceUtil.convert(keyPair);
      Assertions.assertEquals(kid, key.getKeyID(), "kid doesn't match");

   }

   @Test
   public void testKeyDecode() throws Exception {
      //{"kty":"RSA","e":"AQAB",
      // "n":"nffxS0G6GfI7nhlTfp7j_Vj6bRKcgi7lgoKMsmxUlkZ4HuGC7M7LYS2QGecHya6-k_W4E6ru0z3sW3PGLWXlHJbw4DC1DwsLxnkGb5bolHKmNNzoqBifxwT6dG-5gLKdl-1O867Z9jUmbPE0UnbH5CtrZGAIqHt-wOtRM54MrfYLVLbdPaGI1e146vjU2xUNRY6JRAQvs3N6KfQ0uSyWHq11QO9B-BhWAA_zNIRaymXOVYBzcHuob4A_4RVYtoH5tNoqohKhNS9mtl5vBFdLjNbScCH31LepL_ov6y88IotLlz1tLFajhNZz2JVdraO3t2A6y7ZB2He4dZFttdq1hQ",
      // "kid":"fSzb64DVYhQQ73dnQ2GrKxISeMuQ_T5FFtuhhKcI8Is",
      // "alg":"RS256","use":"sig"
      JwtKey jwtKey = new JwtKey();
      jwtKey.setKty(KeyServiceUtil.RSA_ALGORITHM);
      jwtKey.setE("AQAB");
      jwtKey.setN("nffxS0G6GfI7nhlTfp7j_Vj6bRKcgi7lgoKMsmxUlkZ4HuGC7M7LYS2QGecHya6-k_W4E6ru0z3sW3PGLWXlHJbw4DC1DwsLxnkGb5bolHKmNNzoqBifxwT6dG-5gLKdl-1O867Z9jUmbPE0UnbH5CtrZGAIqHt-wOtRM54MrfYLVLbdPaGI1e146vjU2xUNRY6JRAQvs3N6KfQ0uSyWHq11QO9B-BhWAA_zNIRaymXOVYBzcHuob4A_4RVYtoH5tNoqohKhNS9mtl5vBFdLjNbScCH31LepL_ov6y88IotLlz1tLFajhNZz2JVdraO3t2A6y7ZB2He4dZFttdq1hQ");
      jwtKey.setAlg("RS256");
      jwtKey.setUse("sig");
      jwtKey.setKid("fSzb64DVYhQQ73dnQ2GrKxISeMuQ_T5FFtuhhKcI8Is");
      PublicKey pk = KeyServiceUtil.buildRSAPublicKey(jwtKey);
      Assertions.assertNotNull(pk);
   }

}
