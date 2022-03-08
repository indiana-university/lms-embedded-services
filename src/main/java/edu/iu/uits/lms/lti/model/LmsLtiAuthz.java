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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * Created by chmaurer on 2/13/15.
 */
@Entity
@Table(name = "LTI_13_AUTHZ", uniqueConstraints = @UniqueConstraint(name = "UK_LTI_AUTH_REG", columnNames = {"REGISTRATION_ID"}))
@NamedQueries({
        @NamedQuery(name = "LmsLtiAuthz.findByRegistrationActive", query = "from LmsLtiAuthz where registrationId = :registrationId and active = true"),
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
    private String secret;
    private boolean active;
    private Date created;
    private Date modified;

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modified = new Date();
        if (created==null) {
            created = new Date();
        }
    }
}
