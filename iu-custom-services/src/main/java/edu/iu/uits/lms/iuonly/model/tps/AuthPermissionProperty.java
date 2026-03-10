package edu.iu.uits.lms.iuonly.model.tps;

/*-
 * #%L
 * lms-canvas-iu-custom-services
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
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.EnumType;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Data
@Table(
        name = "auth_permission_property",
        uniqueConstraints = @UniqueConstraint(columnNames = {"auth_permission_id", "property_key"})
)
public class AuthPermissionProperty {

    public enum ValueType {
        STRING,
        INTEGER,
        BOOLEAN
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_permission_property_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auth_permission_id")
    private AuthPermission authPermission;

    @Column(nullable = false, name = "property_key")
    private String key;

    /**
     * The data type of the value for this property (e.g., string, integer, boolean)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "value_type")
    private ValueType valueType;

    @Column(name = "default_value")
    private String defaultValue;

    private boolean required = false;
    private String description;

    private OffsetDateTime created;
    private OffsetDateTime modified;

    public boolean isBooleanType() {
        return ValueType.BOOLEAN.equals(this.valueType);
    }

    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modified = OffsetDateTime.now();
        if (created == null) {
            created = OffsetDateTime.now();
        }
    }
}