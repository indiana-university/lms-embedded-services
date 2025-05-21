package edu.iu.uits.lms.iuonly.model.coursetemplating;

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

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


/**
 * Representation of the SIS data used to populate the TEMPLATED_COURSES table. These courses have been
 * processed by the CourseTemplating job
 */
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "TEMPLATED_COURSES",
      uniqueConstraints = @UniqueConstraint(name = "course_id_u", columnNames = {"course_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemplatedCourse extends BaseObject {

   @Id
   @Column(name = "course_id")
   private String courseId;

   @Column(name = "sis_course_id")
   private String sisCourseId;

   @Column(name = "term_id")
   private String termId;

   private String status;

   @Column(name = "iu_crseld_status_added")
   private boolean iuCrseldStatusAdded;

   public TemplatedCourse(String courseId, String sisCourseId, String termId, String status) {
      this.courseId = courseId;
      this.sisCourseId = sisCourseId;
      this.termId = termId;
      this.status = status;
   }

   @OneToMany(cascade = CascadeType.ALL, targetEntity = ContentMigrationStatus.class, mappedBy = "templatedCourse", fetch = FetchType.EAGER, orphanRemoval = true)
   @OrderColumn(name = "sequence")
   @JsonManagedReference
   private List<ContentMigrationStatus> contentMigrations = new ArrayList<>();


   public void addContentMigrations(ContentMigrationStatus contentMigrationStatus) {
      contentMigrationStatus.setTemplatedCourse(this);
      contentMigrations.add(contentMigrationStatus);
   }

}
