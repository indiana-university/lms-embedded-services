package edu.iu.uits.lms.lti.service;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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
