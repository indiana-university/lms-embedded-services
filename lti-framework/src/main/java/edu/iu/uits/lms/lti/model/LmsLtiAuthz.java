package edu.iu.uits.lms.lti.model;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by chmaurer on 2/13/15.
 */
@Entity
@Table(name = "LTI_13_AUTHZ", uniqueConstraints = @UniqueConstraint(name = "UK_LTI_AUTH_REG_ENV", columnNames = {"REGISTRATION_ID", "ENV"}))
@NamedQueries({
        @NamedQuery(name = "LmsLtiAuthz.findByRegistrationEnvActive", query = "from LmsLtiAuthz where registrationId = :registrationId and env = :env and active = true"),
        @NamedQuery(name = "LmsLtiAuthz.findById", query = "from LmsLtiAuthz where ltiAuthzId = :id and active = true"),
})
@SequenceGenerator(name = "LTI_13_AUTHZ_ID_SEQ", sequenceName = "LTI_13_AUTHZ_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LmsLtiAuthz {

    @Id
    @Column(name = "LTI_13_AUTHZ_ID")
    @GeneratedValue(generator = "LTI_13_AUTHZ_ID_SEQ")
    private Long ltiAuthzId;

    @Column(name = "REGISTRATION_ID")
    private String registrationId;

    @Column(name = "CLIENT_ID")
    private String clientId;
    private boolean active;
    private Date created;
    private Date modified;

    @Column(name = "ENV", length = 5)
    private String env;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modified = new Date();
        if (created==null) {
            created = new Date();
        }
    }
}
