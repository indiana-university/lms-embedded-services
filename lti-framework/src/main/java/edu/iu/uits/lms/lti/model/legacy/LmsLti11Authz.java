package edu.iu.uits.lms.lti.model.legacy;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2023 Indiana University
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
import java.util.Date;

/**
 * Created by chmaurer on 2/13/15.
 * @deprecated This class was temporarily added and is not intended for long-term use.  Please use the 1.3 launch mechanism instead.
 */
@Entity
@Table(name = "LTI_AUTHZ")
@NamedQueries({
        @NamedQuery(name = "LmsLti11Authz.findByKeyContextActive", query = "from LmsLti11Authz where consumerKey = :consumerKey and " +
                "(context = :context or context = '" + LmsLti11Authz.CONTEXT_WILDCARD + "') and active = true"),
        @NamedQuery(name = "LmsLti11Authz.findById", query = "from LmsLti11Authz where ltiAuthzId = :id and active = true"),
})
@SequenceGenerator(name = "LTI_AUTHZ_ID_SEQ", sequenceName = "LTI_AUTHZ_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Deprecated(since = "5.2.2", forRemoval = true)
public class LmsLti11Authz {

    protected static final String CONTEXT_WILDCARD = "*";

    @Id
    @Column(name = "LTI_AUTHZ_ID")
    @GeneratedValue(generator = "LTI_AUTHZ_ID_SEQ")
    private Long ltiAuthzId;

    @Column(name = "CONSUMER_KEY")
    private String consumerKey;

    private String context;
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
