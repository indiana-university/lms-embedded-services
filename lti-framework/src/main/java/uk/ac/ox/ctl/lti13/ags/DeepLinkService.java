package uk.ac.ox.ctl.lti13.ags;

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
