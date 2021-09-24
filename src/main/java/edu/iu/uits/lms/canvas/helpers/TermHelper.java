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
import edu.iu.uits.lms.canvas.model.CanvasTerm.TermOverride;

import java.time.OffsetDateTime;
import java.util.Date;

public class TermHelper {

   public static String TERM_NO_EXPIRATION = "noexp";

   public static Date getStartDate(CanvasTerm term) {
      return CanvasDateFormatUtil.string2Date(term.getStartAt());
   }

   public static Date getEndDate(CanvasTerm term) {
      return CanvasDateFormatUtil.string2Date(term.getEndAt());
   }

   public static Date getStartDate(TermOverride termOverride) {
      return CanvasDateFormatUtil.string2Date(termOverride.getStartAt());
   }

   public static Date getEndDate(TermOverride termOverride) {
      return CanvasDateFormatUtil.string2Date(termOverride.getEndAt());
   }

   public static OffsetDateTime getStartOffsetDateTime(CanvasTerm term) {
      return CanvasDateFormatUtil.string2OffsetDateTime(term.getStartAt());
   }

   public static OffsetDateTime getEndOffsetDateTime(CanvasTerm term) {
      return CanvasDateFormatUtil.string2OffsetDateTime(term.getEndAt());
   }

   public static OffsetDateTime getStartOffsetDateTime(TermOverride termOverride) {
      return CanvasDateFormatUtil.string2OffsetDateTime(termOverride.getStartAt());
   }

   public static OffsetDateTime getEndOffsetDateTime(TermOverride termOverride) {
      return CanvasDateFormatUtil.string2OffsetDateTime(termOverride.getEndAt());
   }

   public static boolean isActive(CanvasTerm term) {
      Date now = new Date();
      return CanvasConstants.ACTIVE_STATUS.equals(term.getWorkflowState()) &&
            (term.getEndAt() == null || getEndDate(term).after(now));
   }
}
