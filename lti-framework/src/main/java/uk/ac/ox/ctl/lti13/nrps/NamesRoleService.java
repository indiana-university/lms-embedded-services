package uk.ac.ox.ctl.lti13.nrps;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import com.nimbusds.jose.JOSEException;
import net.minidev.json.JSONObject;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.client.RestTemplate;
import uk.ac.ox.ctl.lti13.OAuth2Interceptor;
import uk.ac.ox.ctl.lti13.TokenRetriever;
import uk.ac.ox.ctl.lti13.lti.Claims;
import uk.ac.ox.ctl.lti13.security.oauth2.client.lti.authentication.OidcLaunchFlowToken;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;

public class NamesRoleService {

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final TokenRetriever tokenRetriever;

    public NamesRoleService(ClientRegistrationRepository clientRegistrationRepository, TokenRetriever tokenRetriever) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.tokenRetriever = tokenRetriever;
    }

    public NRPSResponse getMembers(OidcLaunchFlowToken oAuth2AuthenticationToken, boolean includeResourceLink) {
        OidcUser principal = oAuth2AuthenticationToken.getPrincipal();
        if (principal != null) {
            Object o = principal.getClaims().get(LtiScopes.LTI_NRPS_CLAIM);
            if (o instanceof JSONObject) {
                JSONObject json = (JSONObject)o;
                String contextMembershipsUrl = json.getAsString("context_memberships_url");
                if (contextMembershipsUrl != null && !contextMembershipsUrl.isEmpty()) {
                    // Got a URL to go to.
                    Object r = principal.getClaims().get(Claims.RESOURCE_LINK);
                    String resourceLinkId = null;
                    if (includeResourceLink && r instanceof JSONObject) {
                        JSONObject resourceJson = (JSONObject) r;
                        resourceLinkId = resourceJson.getAsString("id");
                    }
                    return loadMembers(contextMembershipsUrl, resourceLinkId, oAuth2AuthenticationToken.getClientRegistration().getRegistrationId());
                }
            }
        }
        return null;
    }

    private NRPSResponse loadMembers(String contextMembershipsUrl, String resourceLinkId, String clientRegistrationId) {

        ClientRegistration clientRegistration = clientRegistrationRepository.findByRegistrationId(clientRegistrationId);
        if (clientRegistration == null) {
            throw new IllegalStateException("Failed to find client registration for: "+ clientRegistrationId);
        }
        try {
            OAuth2AccessTokenResponse token = tokenRetriever.getToken(clientRegistration, LtiScopes.LTI_NRPS_SCOPE);

            String url = contextMembershipsUrl;
            if (resourceLinkId != null) {
                url = url + "?rlid="+ URLEncoder.encode(resourceLinkId, "UTF-8");
            }
            RestTemplate client = new RestTemplate();
            client.setInterceptors(Collections.singletonList(new OAuth2Interceptor(token.getAccessToken())));
            // TODO Needs to set accept header to: application/vnd.ims.lti-nrps.v2.membershipcontainer+json
            // TODO Needs to handle Link headers
            NRPSResponse response = client.getForObject(url, NRPSResponse.class);
            return response;
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to sign JWT", e);
        } catch (UnsupportedEncodingException e) {
            // This should never happen
            throw new RuntimeException("Unable to find encoding.", e);
        }
    }
}
