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

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.AccountNotification;
import edu.iu.uits.lms.canvas.model.BlueprintTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;

/**
 * Service to get various account related things
 */
@Service
@Slf4j
public class AccountNotificationService extends SpringBaseService {
    private static final String CANVAS_BASE_URI = "{url}";
    private static final String ACCOUNTS_BASE_URI = CANVAS_BASE_URI + "/accounts";
    private static final String ACCOUNT_URI = ACCOUNTS_BASE_URI + "/{id}";
    private static final String ACCOUNT_NOTIF_BASE_URI = ACCOUNT_URI + "/account_notifications";
    private static final String ACCOUNT_NOTIFICATION_URI = ACCOUNT_NOTIF_BASE_URI + "/{id}";

    private static final UriTemplate ALL_NOTIF_TEMPLATE = new UriTemplate(ACCOUNT_NOTIF_BASE_URI);
    private static final UriTemplate GET_NOTIF_TEMPLATE = new UriTemplate(ACCOUNT_NOTIFICATION_URI);

    /**
     *
     * @param accountId notifications will be for the given accountId
     * @param includeAll Include all global announcements, regardless of user’s role or availability date.
     * @return Returns a list of all global notifications in the account for the current user
     */
    public List<AccountNotification> getNotificationsForAccount(String accountId, boolean includeAll) {
        URI uri = ALL_NOTIF_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId);
        log.debug("{}", uri);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("include_all", includeAll);

        return doGet(builder.build().toUri(), AccountNotification[].class);
    }

    public AccountNotification getAccountNotification(String accountId, String accountNotificationId) {
        URI uri = GET_NOTIF_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), accountId, accountNotificationId);
        log.debug("{}", uri);

        HttpEntity<AccountNotification> notification = this.restTemplate.getForEntity(uri, AccountNotification.class);
        log.debug("Account Notification: {}", notification);
        return notification.getBody();
    }

}
