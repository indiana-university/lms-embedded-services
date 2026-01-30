package edu.iu.uits.lms.iuonly.model.acl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "AUTHORIZED_TOOL_PERMISSION_DETAILS")
@Data
@NoArgsConstructor
public class ToolPermissionDetails {

    @Id
    @Column(name = "TOOL_PERMISSION")
    private String toolPermission;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "TOOL_NAME")
    private String toolName;

    @Column(name = "PROPERTY_KEYS")
    private List<String> propertyKeys;

    /**
     *
     * @return true if there are property keys defined, false otherwise
     */
    public boolean hasProperties() {
        return propertyKeys != null && !propertyKeys.isEmpty();
    }
}