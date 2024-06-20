package edu.iu.uits.lms.iuonly.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "LMS_JOB_COMPLETION_DETAILS")
@SequenceGenerator(name = "LMS_JOB_COMPLETION_DETAILS_ID_SEQ", sequenceName = "LMS_JOB_COMPLETION_DETAILS_ID_SEQ", allocationSize = 1)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobCompletionDetail {
    @Id
    @GeneratedValue(generator = "LMS_JOB_COMPLETION_DETAILS_ID_SEQ")
    private Long id;

    @Column(name = "JOB_CODE")
    private String jobCode;

    @Column(name = "JOB_HOST")
    private String jobHost;

    @Lob
    @Type(type="text")
    private String details;
    private boolean success;
    private boolean errors;

    @JsonFormat(pattern= DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name="createdon")
    private Date createdOn;
    @JsonFormat(pattern= DateFormatUtil.JSON_DATE_FORMAT)
    @Column(name="modifiedon")
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
