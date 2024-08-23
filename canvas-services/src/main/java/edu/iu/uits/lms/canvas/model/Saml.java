package edu.iu.uits.lms.canvas.model;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class Saml implements Serializable {
    // Technically this class is an "AuthenticationProvider" in Canvas terminology
    private String id;

    @JsonProperty(value = "auth_type")
    private String authType;

    private String position;

    @JsonProperty(value = "log_in_url")
    private String logInUrl;

    @JsonProperty(value = "log_out_url")
    private String logOutUrl;

    @JsonProperty(value = "requested_authn_context")
    private String requestedAuthnContext;

    @JsonProperty(value = "certificate_fingerprint")
    private String certificateFingerprint;

    @JsonProperty(value = "identifier_format")
    private String identifierFormat;

    @JsonProperty(value = "login_attribute")
    private String loginAttribute;

    @JsonProperty(value = "idp_entity_id")
    private String idpEntityId;

    @JsonProperty(value = "parent_registration")
    private boolean parentRegistration;

    @JsonProperty(value = "jit_provisioning")
    private boolean jitProvisioning;

    @JsonProperty(value = "metadata_uri")
    private String metadataUri;

    @JsonProperty(value = "sig_alg")
    private String sigAlg;

    @JsonProperty(value = "strip_domain_from_login_attribute")
    private boolean stripDomainFromLoginAttribute;

    @JsonProperty(value = "federated_attributes")
    private Map<String, FederatedAttributeConfig> federatedAttributes;

    @JsonProperty(value = "change_password_url")
    private String changePasswordUrl;

    @JsonProperty(value = "login_handle_name")
    private String loginHandleName;

    @JsonProperty(value = "unknown_user_url")
    private String unknownUserUrl;

    @Data
    private static class FederatedAttributeConfig {
        public String attribute;
        @JsonProperty(value = "provisioning_only")
        public boolean provisioningOnly;

        /**
         * (only for 'email' attribute) If the email address is trusted and should be automatically confirmed
         * If this property is sent with any other attribute, Canvas will get mad.  So, set as Boolean so that a null can be
         * supplied, and then excluded from the json payload when sending to Canvas.
         */
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public Boolean autoconfirm;
    }
}
