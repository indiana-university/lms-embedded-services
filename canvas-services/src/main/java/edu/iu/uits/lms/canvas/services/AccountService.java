package edu.iu.uits.lms.canvas.services;

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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.AccountAdmin;
import edu.iu.uits.lms.canvas.model.AccountAdminCreate;
import edu.iu.uits.lms.canvas.model.CanvasRole;
import edu.iu.uits.lms.canvas.model.Saml;
import edu.iu.uits.lms.canvas.model.SsoSettings;
import edu.iu.uits.lms.canvas.model.SsoSettingsWrapper;
import edu.iu.uits.lms.canvas.utils.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Service to get various account related things
 */
@Service
@Slf4j
public class AccountService extends SpringBaseService {
    private static final String CANVAS_BASE_URI = "{url}";
    private static final String ACCOUNTS_BASE_URI = CANVAS_BASE_URI + "/accounts";
    private static final String ACCOUNT_URI = ACCOUNTS_BASE_URI + "/{id}";
    private static final String COURSE_ACCOUNTS_URI = CANVAS_BASE_URI + "/course_accounts";
    private static final String ROLES_URI = ACCOUNT_URI + "/roles";
    private static final String ACCOUNT_ADMINS_URI = ACCOUNT_URI + "/admins";
    private static final String ACCOUNT_ADMINS_SPECIFIC_URI = ACCOUNT_ADMINS_URI + "/{userid}";
    private static final String SUBACCOUNTS_URI = ACCOUNT_URI + "/sub_accounts";
    private static final String SSO_SETTINGS_URI = ACCOUNT_URI + "/sso_settings";
    private static final String SAML_URI = ACCOUNT_URI + "/authentication_providers/{samlId}";

    private static final UriTemplate ACCOUNT_TEMPLATE = new UriTemplate(ACCOUNT_URI);
    private static final UriTemplate COURSE_ACCOUNTS_TEMPLATE = new UriTemplate(COURSE_ACCOUNTS_URI);
    private static final UriTemplate ROLES_TEMPLATE = new UriTemplate(ROLES_URI);
    private static final UriTemplate ACCOUNT_ADMINS_TEMPLATE = new UriTemplate(ACCOUNT_ADMINS_URI);
    private static final UriTemplate ACCOUNT_ADMINS_SPECIFIC_TEMPLATE = new UriTemplate(ACCOUNT_ADMINS_SPECIFIC_URI);
    private static final UriTemplate SUBACCOUNTS_TEMPLATE = new UriTemplate(SUBACCOUNTS_URI);
    private static final UriTemplate SSO_SETTINGS_TEMPLATE = new UriTemplate(SSO_SETTINGS_URI);
    private static final UriTemplate SAML_TEMPLATE = new UriTemplate(SAML_URI);

    public static final String ORDER_BY_ID = "id";
    public static final String ORDER_BY_NAME = "name";

    /**
     * Get all roles that are defined in the system
     * @return List of CanvasRole objects
     */
    public List<CanvasRole> getAllRoles() {
        URI uri = ROLES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasConfiguration.getAccountId());
        log.debug("{}", uri);

        return doGet(uri, CanvasRole[].class);
    }

    /**
     * Get all the roles for the given accountId
     * @param accountId Account id
     * @param showInherited Flag indicating if all inherited roles should be returned as well
     * @return List of canvas roles available to this account
     */
    public List<CanvasRole> getRolesForAccount(String accountId, boolean showInherited) {
        URI uri = ROLES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("show_inherited", showInherited);

        return doGet(builder.build().toUri(), CanvasRole[].class);
    }

    /**
     *
     * @param accountId Account id
     * @return a list of the parent {@link Account}s for the given accountId
     */
    @Cacheable(value = CacheConstants.PARENT_ACCOUNTS_CACHE_NAME, cacheManager = "CanvasServicesCacheManager")
    public List<Account> getParentAccounts(String accountId) {
        List<Account> parentAccounts = new ArrayList<>();
        Account account = getAccount(accountId);

        // if there is no parent account (ie this is the root), parent_account_id should be null
        while (account != null && account.getParentAccountId() != null) {
            account = getAccount(account.getParentAccountId());
            parentAccounts.add(account);
        }

        return parentAccounts;
    }

    /**
     * Get a specific account object from Canvas
     * GET /api/v1/accounts/account_id
     * @param accountId Canvas account id.  Could also have the form of sis_account_id:account_id
     * @return Account
     */
    public Account getAccount(String accountId) {
        URI uri = ACCOUNT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("uri: {}", uri);

        try {
            ResponseEntity<Account> accountResponseEntity = this.restTemplate.getForEntity(uri, Account.class);
            log.debug("accountResponseEntity: {}", accountResponseEntity);

            return accountResponseEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error: ", hcee);
        }

        return null;
    }

    /**
     * Get all accounts a given user can access
     * @param userLoginId User's login id
     * @return List of accessible accounts
     */
    public List<Account> getAccountsForUser(String userLoginId) {
        URI uri = COURSE_ACCOUNTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl());
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("as_user_id", "sis_login_id:" + userLoginId);
        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Account[].class);
    }

    /**
     * Is the given user an admin in the given account
     * @param accountId
     * @param userId
     * @return
     */
    public boolean isAccountAdmin(String accountId, String userId) {
        URI uri = ACCOUNT_ADMINS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("user_id[]", userId);

        List<AccountAdmin> accountAdmins = doGet(builder.build().toUri(), AccountAdmin[].class);

        for (AccountAdmin accountAdmin : accountAdmins) {
            if (CanvasConstants.ACTIVE_STATUS.equals(accountAdmin.getWorkflow_state())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Elevate the given user to an account admin
     * @param accountId
     * @param userId
     * @return
     */
    public boolean elevateToAccountAdmin(String accountId, String userId) {

        if (isAccountAdmin(accountId, userId)) {
            return true;
        }

        URI uri = ACCOUNT_ADMINS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("uri: {}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            AccountAdminCreate accountAdminCreate = new AccountAdminCreate(userId, false);

            HttpEntity<AccountAdminCreate> accountAdminCreateRequestEntity = new HttpEntity<>(accountAdminCreate, headers);
            ResponseEntity<AccountAdmin> accountAdminCreateResponseEntity = this.restTemplate.exchange(uri, HttpMethod.POST, accountAdminCreateRequestEntity, AccountAdmin.class);
            log.debug("accountAdminCreateResponseEntity: {}", accountAdminCreateResponseEntity);

            if (accountAdminCreateResponseEntity.getStatusCode() == HttpStatus.OK) {
                return true;
            }

        } catch (HttpClientErrorException hcee) {
            log.error("uh oh", hcee);
        }

        return false;

    }

    /**
     * Demote the given user back to a "regular" user as they no longer need to ba an account admin
     * @param accountId
     * @param userId
     * @return
     */
    public boolean revokeAsAccountAdmin(String accountId, String userId) {

        if (! isAccountAdmin(accountId, userId)) {
            return true;
        }

        try {
            URI uri = ACCOUNT_ADMINS_SPECIFIC_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId, userId);
            log.debug("uri: {}", uri);

            ResponseEntity<AccountAdmin> accountAdminDeleteResponseEntity = this.restTemplate.exchange(uri, HttpMethod.DELETE, null, AccountAdmin.class);
            log.debug("accountAdminDeleteResponseEntity: {}",  accountAdminDeleteResponseEntity);

            if (accountAdminDeleteResponseEntity.getStatusCode() == HttpStatus.OK) {
                return true;
            }
        } catch (HttpClientErrorException hcee) {
            log.error("uh oh", hcee);
        }

        return false;

    }

    /**
     * Get all subaccounts that are defined in the system
     * @return List of Account objects
     */
    public List<Account> getSubAccounts() {
        URI uri = SUBACCOUNTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasConfiguration.getAccountId());
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("recursive","true");
        builder.queryParam("per_page", "50");

        return doGet(builder.build().toUri(), Account[].class);
    }

    /**
     *
     * @param accountId
     * @param recursive If true, the entire account tree underneath this account will be returned (though still paginated). If false, only direct sub-accounts of this account will be returned.
     * @param order Values: {@link #ORDER_BY_ID}, {@link #ORDER_BY_NAME} Sorts the accounts by id or name. Only applies when recursive is false. Defaults to id.
     * @return
     */
    public List<Account> getSubAccountsForAccount(String accountId, boolean recursive, String order) {
        URI uri = SUBACCOUNTS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

        builder.queryParam("recursive",recursive);
        builder.queryParam("per_page", "50");
        builder.queryParam("order", order);

        return doGet(builder.build().toUri(), Account[].class);
    }

    /**
     * Account with this accountId to use to set the sisAccountId value
     * @param accountId - the accountId to use to set
     * @param sisAccountId - the sisAccountId value to set for account with accountId
     * @return - the account changed
     */
    public Account setSisAccountId(String accountId, String sisAccountId) {
        if (accountId == null || accountId.isEmpty() || sisAccountId == null || sisAccountId.isEmpty()) {
            throw new RuntimeException("Null parameters");
        }

        URI uri = ACCOUNT_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("uri: {}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("account[sis_account_id]", sisAccountId);

        try {
            ResponseEntity<Account> response = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, null, Account.class);
            log.debug("{}", response);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                        + response.getStatusCode() + ", reason: " + ((HttpStatus)response.getStatusCode()).getReasonPhrase()
                        + ", body: " + response.getBody());
            }

            return response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("cannot set sisAccountId", hcee);
            throw new RuntimeException("cannot set sisAccountId", hcee);
        }
    }

    /**
     * GET /api/v1/accounts/:account_id/sso_settings
     * @param canvasServer
     * @return
     */
    public SsoSettings getSsoSettings(String canvasServer) {
        URI uri = SSO_SETTINGS_TEMPLATE.expand(canvasServer, canvasConfiguration.getAccountId());
        log.debug("{}", uri);

        try {
            ResponseEntity<SsoSettingsWrapper> ssoSettingsResponseEntity = this.restTemplate.getForEntity(uri, SsoSettingsWrapper.class);
            log.debug("{}", ssoSettingsResponseEntity);

            SsoSettingsWrapper ssoSettingsWrapperResponse = ssoSettingsResponseEntity.getBody();

            if (ssoSettingsWrapperResponse != null) {
                return ssoSettingsWrapperResponse.getSsoSettings();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting SsoSettings", hcee);
        }

        return null;
    }

    /**
     * PUT /api/v1/accounts/:account_id/sso_settings
     * @param canvasServer
     * @param ssoSettingsWrapper
     * @return
     */
    public SsoSettings setSsoSettings(String canvasServer, SsoSettingsWrapper ssoSettingsWrapper) {
        URI uri = SSO_SETTINGS_TEMPLATE.expand(canvasServer, canvasConfiguration.getAccountId());
        log.debug("{}", uri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<SsoSettingsWrapper> ssoSettingsWrapperRequestEntity = new HttpEntity<>(ssoSettingsWrapper, headers);
            ResponseEntity<SsoSettingsWrapper> ssoSettingsResponseEntity = this.restTemplate.exchange(uri, HttpMethod.PUT, ssoSettingsWrapperRequestEntity, SsoSettingsWrapper.class);
            log.debug("{}", ssoSettingsResponseEntity);

            SsoSettingsWrapper ssoSettingsWrapperResponse = ssoSettingsResponseEntity.getBody();

            if (ssoSettingsWrapperResponse != null) {
                return ssoSettingsWrapperResponse.getSsoSettings();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error setting SsoSettings", hcee);
        }

        return null;
    }

    /**
     * GET /api/v1/accounts/:account_id/authentication_providers/:id
     * @param canvasServer
     * @param samlId
     * @return
     */
    public Saml getSaml(String canvasServer, String samlId) {
        URI uri = SAML_TEMPLATE.expand(canvasServer, canvasConfiguration.getAccountId(), samlId);
        log.debug("{}", uri);

        try {
            ResponseEntity<Saml> samlResponseEntity = this.restTemplate.getForEntity(uri, Saml.class);
            log.debug("{}", samlResponseEntity);

            return samlResponseEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error getting Saml", hcee);
        }

        return null;
    }

    /**
     * PUT /api/v1/accounts/:account_id/authentication_providers/:id
     * @param canvasServer
     * @param samlId
     * @param newSamlObject
     */
    public void setSaml(String canvasServer, String samlId, Saml newSamlObject) {
        URI uri = SAML_TEMPLATE.expand(canvasServer, canvasConfiguration.getAccountId(), samlId);
        log.debug("{}", uri);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        try {
            HttpEntity<Saml> requestEntity = new HttpEntity<>(newSamlObject, headers);
            ResponseEntity<Saml> samlResponseEntity = this.restTemplate.exchange(uri, HttpMethod.PUT, requestEntity, Saml.class);
            log.debug("{}", samlResponseEntity);

//            ResponseEntity<Saml> responseEntity = (ResponseEntity<Saml>) samlResponseEntity;

            if (samlResponseEntity.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Request to Canvas was not successful. Response code: "
                      + samlResponseEntity.getStatusCode() + ", reason: " + ((HttpStatus)samlResponseEntity.getStatusCode()).getReasonPhrase()
                      + ", body: " + samlResponseEntity.getBody());
            }
        } catch (RuntimeException re) {
            log.error("Error setting Saml", re);
        }
    }
}
