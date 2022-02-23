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

import com.nimbusds.jose.Algorithm;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.model.JwtKey;
import edu.iu.uits.lms.lti.model.KeyPair;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Slf4j
public class KeyServiceUtil {

   private static final String BEGIN_PRIV = "-----BEGIN RSA PRIVATE KEY-----";
   private static final String END_PRIV = "-----END RSA PRIVATE KEY-----";
   private static final String BEGIN_PUB = "-----BEGIN PUBLIC KEY-----";
   private static final String END_PUB = "-----END PUBLIC KEY-----";
   private static final String NEW_LINE = "\\n";
   protected static final String RSA_ALGORITHM = "RSA";
   private static final String NOTHING = "";

   public static RSAKey convert(KeyPair keyPair) {
      Security.addProvider(new BouncyCastleProvider());

      RSAKey jwtKey = null;
      try {
         Keys keys = keyPair2Keys(keyPair);

         String keyId = DigestUtils.sha256Hex(keys.getPublicKey().getEncoded());

         jwtKey = new RSAKey.Builder(keys.getPublicKey())
               .privateKey(keys.getPrivateKey())
               .keyID(keyId)
               .algorithm(new Algorithm("RS256"))
               .keyUse(KeyUse.SIGNATURE)
               .build();

      } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
         log.error("Unable to parse keys", e);
      }

      return jwtKey;
   }

   /**
    * Convert the string value of private key into an RSAPrivateKey.
    * It strips out the header/footer and new lines.
    * @param keyString String value if the key
    * @return RSAPrivateKey
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeySpecException
    */
   public static RSAPrivateKey string2PrivateKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
      KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
      String privateKeyContent = keyString.replaceAll(NEW_LINE, NOTHING).replace(BEGIN_PRIV, NOTHING).replace(END_PRIV, NOTHING);
      PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent.getBytes()));
      return (RSAPrivateKey) kf.generatePrivate(keySpecPKCS8);
   }

   /**
    * Convert the string value of a public key into an RSAPublicKey.
    * It strips out the header/footer and new lines.
    * @param keyString String value of the key
    * @return RSAPublicKey
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeySpecException
    */
   public static RSAPublicKey string2PublicKey(String keyString) throws NoSuchAlgorithmException, InvalidKeySpecException {
      KeyFactory kf = KeyFactory.getInstance(RSA_ALGORITHM);
      String publicKeyContent = keyString.replaceAll(NEW_LINE, NOTHING).replace(BEGIN_PUB, NOTHING).replace(END_PUB, NOTHING);
      X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent));
      return (RSAPublicKey) kf.generatePublic(keySpecX509);
   }


   private static Keys keyPair2Keys(KeyPair keyPair) throws NoSuchAlgorithmException, InvalidKeySpecException {
      RSAPrivateKey privateKey = string2PrivateKey(keyPair.getPrivateKey());
      RSAPublicKey pubKey = string2PublicKey(keyPair.getPublicKey());

      return new Keys(privateKey, pubKey);
   }

   /**
    * Extract the public key out of the JWT
    * @param key
    * @return PublicKey
    * @throws NoSuchAlgorithmException
    * @throws InvalidKeySpecException
    */
   public static PublicKey buildRSAPublicKey(final JwtKey key) throws NoSuchAlgorithmException, InvalidKeySpecException {
      BigInteger modulus = new BigInteger(1, Base64.getUrlDecoder().decode(key.getN()));
      BigInteger publicExponent = new BigInteger(1, Base64.getUrlDecoder().decode(key.getE()));
      return KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(
            new RSAPublicKeySpec(modulus, publicExponent));
   }

   @Data
   @AllArgsConstructor
   private static class Keys {
      private RSAPrivateKey privateKey;
      private RSAPublicKey publicKey;
   }
}
