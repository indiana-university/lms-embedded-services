package edu.iu.uits.lms.iuonly.model.acl;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "AUTHORIZED_TOOL_PERMISSION",
        uniqueConstraints = @UniqueConstraint(name = "U_AUTHORIZED_TOOL_PERMISSION_USER",
                columnNames = {"TOOL_PERMISSION", "AUTHORIZED_USER_ID"}) )
@Data
@NoArgsConstructor
public class ToolPermission implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTHORIZED_TOOL_PERMISSION_ID")
    private Long id;

    @Column(name="TOOL_PERMISSION")
    private String toolPermission;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "AUTHORIZED_TOOL_PERMISSION_PROPERTIES",
            joinColumns = @JoinColumn(name = "AUTHORIZED_TOOL_PERMISSION_ID",
                    foreignKey = @ForeignKey(name = "FK_ATPP_ATP") ))
    @MapKeyColumn(name = "PROPERTY_KEY")
    @Column(name = "PROPERTY_VALUE")
    private Map<String, String> toolPermissionProperties = new HashMap<>();

}
