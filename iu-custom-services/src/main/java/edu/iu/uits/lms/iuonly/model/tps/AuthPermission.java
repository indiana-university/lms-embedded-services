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

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Table(
        name = "auth_permission",
        uniqueConstraints = @UniqueConstraint(columnNames = {"auth_tool_id", "permission_key"})
)
public class AuthPermission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_permission_id")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auth_tool_id")
    private AuthTool authTool;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "permission_key", nullable = false)
    private String key;

    private String description;

    @OneToMany(mappedBy = "authPermission", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<AuthPermissionProperty> properties = new ArrayList<>();

    private OffsetDateTime created;
    private OffsetDateTime modified;

    public boolean hasProperties() {
        return properties != null && !properties.isEmpty();
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
