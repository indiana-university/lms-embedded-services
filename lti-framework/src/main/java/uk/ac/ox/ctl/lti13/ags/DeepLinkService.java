package uk.ac.ox.ctl.lti13.ags;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.service.LtiAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import uk.ac.ox.ctl.lti13.KeyPairService;
import uk.ac.ox.ctl.lti13.lti.Claims;

import java.security.KeyPair;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class DeepLinkService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final KeyPairService keyPairService;
    private final String env;
    private final LtiAuthorizationService ltiAuthorizationService;

    public DeepLinkService(ClientRegistrationRepository clientRegistrationRepository, KeyPairService keyPairService,
                           String env, LtiAuthorizationService ltiAuthorizationService) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.keyPairService = keyPairService;
        this.env = env;
        this.ltiAuthorizationService = ltiAuthorizationService;
    }

    public String buildResponseJwt(String clientId, String platformIssuer, String deploymentId,
                                   String deepLinkData, String objectId, String objectTitle, Map<String, String> customClaimMap) {
        log.debug("Building Deep Linking Response JWT for objectId={}", objectId);

        LmsLtiAuthz ltiAuthz = ltiAuthorizationService.findByClientId(clientId, env);
        String clientRegistrationId = ltiAuthz.getRegistrationId();

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);

        try {
            String keyId = keyPairService.getKeyId(clientRegistration.getRegistrationId());
            KeyPair keyPair = keyPairService.getKeyPair(clientRegistrationId);
            RSASSASigner signer = new RSASSASigner(keyPair.getPrivate());

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .keyID(keyId)
                    .build();

            Instant now = Instant.now();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(clientId)
                    .audience(platformIssuer)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(300)))
                    .jwtID(UUID.randomUUID().toString())
                    .claim("nonce", UUID.randomUUID().toString())
                    .claim(Claims.LTI_DEPLOYMENT_ID, deploymentId)
                    .claim(Claims.MESSAGE_TYPE, "LtiDeepLinkingResponse")
                    .claim(Claims.LTI_VERSION, "1.3.0")
                    .claim("https://purl.imsglobal.org/spec/lti-dl/claim/data", deepLinkData)
                    .claim(Claims.CONTENT_ITEMS, List.of(
                            Map.of(
                                    "type", "ltiResourceLink",
                                    "title", objectTitle,
                                    "custom", customClaimMap
                            )
                    ))
                    .build();

            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);
            return jwt.serialize();

        } catch (Exception e) {
            log.error("Failed to build Deep Linking Response JWT for objectId={}", objectId, e);
            throw new RuntimeException("Failed to build Deep Linking Response JWT", e);
        }
    }
}
