package edu.iu.uits.lms.canvasoauth2.model;

/*-
 * #%L
 * LMS Canvas OAuth2 Client
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Entity
@Table(name = "LMS_CANVAS_OAUTH2_AUTHZ", uniqueConstraints = @UniqueConstraint(name = "UK_CANVAS_OAUTH2_AUTHZ_REG_ENV_USER", columnNames = {"REGISTRATION_ID", "ENV", "CANVAS_USER_ID"}))
@NamedQueries({
        @NamedQuery(name = "CanvasOAuth2Authz.findByRegistrationEnvUser", query = "from CanvasOAuth2Authz where registrationId = :registrationId and env = :env and canvasUserId = :canvasUserId"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanvasOAuth2Authz {

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "REGISTRATION_ID")
    private String registrationId;

    @Column(name = "ENV", length = 5)
    private String env;

    @Column(name = "CANVAS_USER_ID")
    private String canvasUserId;

    @ToString.Exclude
    @Column(name = "ACCESS_TOKEN", length = 4000)
    private String accessToken;

    @ToString.Exclude
    @Column(name = "REFRESH_TOKEN", length = 4000)
    private String refreshToken;

    @Column(name = "TOKEN_TYPE")
    private String tokenType;

    @Column(name = "SCOPES", length = 1000)
    private String scopes;

    @Column(name = "EXPIRES_AT")
    private Date expiresAt;

    private Date created;
    private Date modified;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modified = new Date();
        if (created == null) {
            created = new Date();
        }
    }
}
