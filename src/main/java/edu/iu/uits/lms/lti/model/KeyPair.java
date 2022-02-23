package edu.iu.uits.lms.lti.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "LTI_KEY_PAIRS")
@SequenceGenerator(name = "LTI_KEY_PAIR_ID_SEQ", sequenceName = "LTI_KEY_PAIR_ID_SEQ", allocationSize = 1)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyPair {
   @Id
   @Column(name = "LTI_KEY_PAIR_ID")
   @GeneratedValue(generator = "LTI_KEY_PAIR_ID_SEQ")
   private Long id;

   @Column(name = "PRIVATE_KEY")
   private String privateKey;

   @Column(name = "PUBLIC_KEY")
   private String publicKey;

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
