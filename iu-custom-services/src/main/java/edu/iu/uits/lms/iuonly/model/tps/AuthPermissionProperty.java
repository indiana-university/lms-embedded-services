// AuthPermissionProperty.java
package edu.iu.uits.lms.iuonly.model.tps;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Entity
@Getter
@Setter
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
    private ValueType valueType;

    private Boolean required = false;
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