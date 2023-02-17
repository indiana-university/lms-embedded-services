package edu.iu.uits.lms.canvas.model.groups;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown=true)
@Data
public class Group {
    @AllArgsConstructor
    @Getter
    public enum GROUP_TYPE {
        COURSE("Course"),
        ACCOUNT("Account");

        private String text;
    }

    /**
     * The ID of the group.
     */
    String id;
    /**
     * The display name of the group.
     */
    String name;
    /**
     * A description of the group. This is plain text.
     */
    String description;
    /**
     * The course or account that the group belongs to. The pattern here is that
     * whatever the context_type is, there will be an _id field named after that
     * type. So if instead context_type was 'account', the course_id field would be
     * replaced by an account_id field. If course type, use CourseGroup object
     */
    @JsonProperty("context_type")
    String contextType;
    /**
     * The ID of the group's category.
     */
    @JsonProperty("group_category_id")
    String groupCategoryId;
    /**
     * The SIS ID of the group. Only included if the user has permission to view SIS
     * information.
     */
    @JsonProperty("sis_group_id")
    String sisGroupId;

}
