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

import com.fasterxml.jackson.annotation.JsonFormat;
import edu.iu.uits.lms.common.date.DateFormatUtil;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "DEPT_PROV_USERS")
@NamedQueries({
        @NamedQuery(name = "DeptProvisioningUser.findByCanvasUserId", query = "from DeptProvisioningUser where canvasUserId = :canvasUserId"),
})
@SequenceGenerator(name = "DEPT_PROV_USERS_ID_SEQ", sequenceName = "DEPT_PROV_USERS_ID_SEQ", allocationSize = 1)
@Data
public class DeptProvisioningUser implements Serializable {

   @Id
   @GeneratedValue(generator = "DEPT_PROV_USERS_ID_SEQ")
   @Column(name = "DEPT_PROV_USERS_ID")
   private Long id;

   @Column(name = "DISPLAY_NAME")
   private String displayName;

   @Column(name = "USERNAME")
   private String username;

   @Column(name = "CANVAS_USER_ID")
   private String canvasUserId;

   @Column(name = "GROUP_CODE")
   @ElementCollection(fetch = FetchType.EAGER)
   @CollectionTable(name = "DEPT_PROV_USER_GROUP", joinColumns = @JoinColumn(name = "DEPT_PROV_USERS_ID"))
   private List<String> groupCode;

   @Column(name = "ALLOW_SIS_ENROLLMENTS")
   private boolean allowSisEnrollments;

   @Column(name = "AUTHORIZED_ACCOUNTS")
   private String authorizedAccounts;

   @Column(name = "OVERRIDE_RESTRICTIONS")
   private boolean overrideRestrictions;

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
