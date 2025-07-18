package edu.iu.uits.lms.iuonly.model;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "GLOBAL_ANNOUNCEMENT")
@SequenceGenerator(name = "GLOBAL_ANNOUNCEMENT_ID_SEQ", sequenceName = "GLOBAL_ANNOUNCEMENT_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@Slf4j
public class GlobalAnnouncement implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GLOBAL_ANNOUNCEMENT_ID")
    private Long id;

    @NonNull
    @Column(name = "ACCOUNT_NOTIFICATION_ID")
    private String accountNotificationId;

    @NonNull
    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @NonNull
    @Column(name = "ACCOUNT_NAME")
    private String accountName;

    @NonNull
    @Column(name = "AUTHOR_NAME")
    private String authorName;

    @NonNull
    @Column
    private String title;

    @NonNull
    @Column
    private String type;

    @NonNull
    @Column(name = "ROLE_CATEGORY")
    private String roleCategory;

    @NonNull
    @Column(name = "ROLES")
    private List<String> roles;

    @NonNull
    @Column(name = "START_DATE")
    private Date startDate;

    @NonNull
    @Column(name = "END_DATE")
    private Date endDate;

    @NonNull
    @Column(name = "CREATEDBY")
    private String createdBy;

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
