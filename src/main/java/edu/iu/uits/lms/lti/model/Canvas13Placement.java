package edu.iu.uits.lms.lti.model;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;

import java.util.Map;

@Data
@Builder
@Setter(AccessLevel.NONE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Canvas13Placement {

    // Constant for the window target
    public static final String WINDOW_TARGET_BLANK = "_blank";

    public enum MessageType {LtiResourceLinkRequest, LtiDeepLinkingRequest};

    public enum Placement {
        @JsonProperty("link_selection") LINK_SELECTION,
        @JsonProperty("assignment_selection") ASSIGNMENT_SELECTION,
        @JsonProperty("course_navigation") COURSE_NAVIGATION,
        @JsonProperty("account_navigation") ACCOUNT_NAVIGATION,
        @JsonProperty("user_navigation") USER_NAVIGATION,
        @JsonProperty("editor_button") EDITOR_BUTTON,
        @JsonProperty("migration_selection") MIGRATION_SELECTION,

        // These aren't in the API documentation, but are in the list in the UI
        // similarity_detection
        // assignment_edit
        // assignment_menu
        // assignment_view
        // collaboration
        // course_assignments_menu
        // course_home_sub_navigation
        // course_settings_sub_navigation
        // discussion_topic_menu
        // file_menu
        // global_navigation
        // homework_submission
        // module_menu
        // module_index_menu
        // post_grades
        // quiz_menu
        // resource_selection
        // student_context_card
        // tool_configuration
        // wiki_index_menu
        // wiki_page_menu

    }

    private String text;
    private boolean enabled;
    private String iconUrl;
    private Placement placement;
    private MessageType messageType;
    private String targetLinkUri;
    private String canvasIconClass;
    private Map<String, String> customFields;

    @JsonProperty("windowTarget")
    private String windowTarget;

}
