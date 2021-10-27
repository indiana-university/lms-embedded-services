package edu.iu.uits.lms.canvas.helpers;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.canvas.model.Course;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.Date;

public class CourseHelper {

   @AllArgsConstructor
   @Getter
   public enum WORKFLOW_STATE {
      UNPUBLISHED("unpublished"),
      AVAILABLE("available"),
      COMPLETED("completed"),
      DELETED("deleted");

      private String text;
   }

   public enum FEATURE_FLAG_STATE {
      off,
      allowed,
      on
   }

   /**
    * Is the course locked?
    *
    * @param course Course to check for being locked
    * @param includeStartDate set to true if you want to include course start date in the logic to determine if this course
    *                         is locked. If false, will not check to see if the course has started; it will just return
    *                         whether the course has ended.
    * @return true if this course is locked (hasn't started (if includeStartDate) or has ended)
    */
   public static boolean isLocked(Course course, boolean includeStartDate) {
      boolean locked = false;

      Date courseEndDate = getEndDate(course);
      CanvasTerm term = course.getTerm();
      Date now = new Date();
      Date termEndDate = TermHelper.getEndDate(term);

      if (includeStartDate && getStartDate(course) != null && now.before(getStartDate(course))) {
         // course hasn't started yet
         locked = true;
      } else if (termEndDate != null && now.after(termEndDate)) {
         // the term has ended
         if (courseEndDate == null || now.after(courseEndDate) || (now.before(courseEndDate) && !course.isRestrictEnrollmentsToCourseDates()))  {
            locked = true;
         }
      } else {
         // the term is still open, but the course itself may have closed
         if (course.isRestrictEnrollmentsToCourseDates() && courseEndDate != null && now.after(courseEndDate)) {
            locked = true;
         }
      }

      return locked;
   }

   public static boolean isPublished(Course course) {
      return course.getWorkflowState() != null && WORKFLOW_STATE.AVAILABLE.getText().equals(course.getWorkflowState());
   }

   public static Date getStartDate(Course course) {
      return CanvasDateFormatUtil.string2Date(course.getStartAt());
   }

   public static Date getEndDate(Course course) {
      return CanvasDateFormatUtil.string2Date(course.getEndAt());
   }

   public static Date getCreatedDate(Course course) {
      return CanvasDateFormatUtil.string2Date(course.getCreatedAt());
   }

   public static OffsetDateTime getStartOffsetDateTime(Course course) { 
     return CanvasDateFormatUtil.string2OffsetDateTime(course.getStartAt()); 
   }

   public static OffsetDateTime getEndOffsetDateTime(Course course) { 
     return CanvasDateFormatUtil.string2OffsetDateTime(course.getEndAt()); 
   }

   public static OffsetDateTime getCreatedOffsetDateTime(Course course) { 
     return CanvasDateFormatUtil.string2OffsetDateTime(course.getCreatedAt()); 
   }

   public static boolean isCourseActive(Course course) {
      return !isLocked(course, true);
   }

}
