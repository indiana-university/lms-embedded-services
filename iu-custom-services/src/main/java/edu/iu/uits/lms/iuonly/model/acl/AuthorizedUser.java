package edu.iu.uits.lms.iuonly.model.acl;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Entity
@Table(name = "AUTHORIZED_USERS", uniqueConstraints = @UniqueConstraint(name = "UK_AUTHORIZED_USERS", columnNames = {"username"}))
@Data
public class AuthorizedUser implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AUTHORIZED_USER_ID")
    private Long id;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "USERNAME")
    private String username;

    @Column(name = "CANVAS_USER_ID")
    private String canvasUserId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ACTIVE")
    private boolean active;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTHORIZED_USER_ID", foreignKey = @ForeignKey(name = "FK_ATP_AU"))
    @MapKeyColumn(name="TOOL_PERMISSION")
    private Map<String, ToolPermission> toolPermissions;

    public Map<String, String> getToolPermissionProperties(String toolPermission) {
        return toolPermissions.get(toolPermission).getToolPermissionProperties();
    }

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "CREATEDON")
    private Date createdOn;

    @JsonFormat(pattern = DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name = "MODIFIEDON")
    private Date modifiedOn;


    @PreUpdate
    @PrePersist
    public void updateTimeStamps() {
        modifiedOn = new Date();
        if (createdOn==null) {
            createdOn = new Date();
        }
    }
}
