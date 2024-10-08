package edu.iu.uits.lms.canvas.model;

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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.iu.uits.lms.canvas.helpers.TermHelper;
import lombok.Data;
import lombok.NonNull;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by chmaurer on 6/24/15.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown=true)
public class CanvasTerm implements Serializable, Comparable<CanvasTerm> {

    private String id;

    @JsonProperty("sis_term_id")
    private String sisTermId;

    private String name;

    @JsonProperty("start_at")
    private String startAt;

    @JsonProperty("end_at")
    private String endAt;

    @JsonProperty("workflow_state")
    private String workflowState;

    private Map<String, TermOverride> overrides;


    @Override
    public int compareTo(@NonNull CanvasTerm otherCanvasTerm) {
        // This comparator is used for sorting terms in a map. We don't want it to ever return equal because
        // that entry would not be added to the map.  In case of equality, return in ABC order.
        if (TermHelper.getStartDate(this) == null && TermHelper.getStartDate(otherCanvasTerm) == null) {
            // if start dates are the same, return in ABC order
            return this.getName().compareTo(otherCanvasTerm.getName());
        } else if (TermHelper.getStartDate(this) == null && TermHelper.getStartDate(otherCanvasTerm) != null) {
            return 1;
        } else if (TermHelper.getStartDate(this) != null && TermHelper.getStartDate(otherCanvasTerm) == null) {
            return -1;
        } else if (TermHelper.getStartDate(this).before(TermHelper.getStartDate(otherCanvasTerm))) {
            return 1;
        } else if (TermHelper.getStartDate(this).after(TermHelper.getStartDate(otherCanvasTerm))) {
            return -1;
        } else {
            // if start dates are the same, return in ABC order
            return this.getName().compareTo(otherCanvasTerm.getName());
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class TermOverride implements Serializable {

        @JsonProperty("start_at")
        private String startAt;

        @JsonProperty("end_at")
        private String endAt;

    }
}
