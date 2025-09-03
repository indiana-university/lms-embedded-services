package edu.iu.uits.lms.iuonly.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "LMS_PROVISIONING_TERMS")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProvisioningTerm {

   @Id
   @Column(name = "SIS_TERM_ID")
   private String sisTermId;

   @Column(name = "NAME")
   private String name;

   @Column(name = "STARTDATE")
   private String startDate;

   @Column(name = "ENDDATE")
   private String endDate;

}
