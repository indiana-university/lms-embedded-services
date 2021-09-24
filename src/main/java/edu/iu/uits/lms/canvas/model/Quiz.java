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
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
@Data
public class Quiz implements Serializable {

    private String id;

    private String title;

//    @JsonProperty("html_url")
//    private String htmlUrl;

//    @JsonProperty("mobile_url")
//    private String mobileUrl;

//    @JsonProperty("preview_url")
//    private String previewUrl;

//    private String description;

//    // type of quiz possible values; 'practice_quiz', 'assignment', 'graded_survey', 'survey'
//    @JsonProperty("quiz_type")
//    private String quizType;

//    @JsonProperty("assignment_group_id")
//    private String assignmentGroupId;

    @JsonProperty("time_limit")
    private int timeLimit;

//    @JsonProperty("shuffle_answers")
//    private boolean shuffleAnswers;

    @JsonProperty("hide_results")
    private String hideResults;

    @JsonProperty("show_correct_answers")
    private boolean showCorrectAnswers;

    @JsonProperty("show_correct_answers_last_attempt")
    private boolean showCorrectAnswersLastAttempt;

    @JsonProperty("show_correct_answers_at")
    private String showCorrectAnswersAt;

    @JsonProperty("hide_correct_answers_at")
    private String hideCorrectAnswersAt;

//    @JsonProperty("one_time_results")
//    private boolean oneTimeResults;

    // which quiz score to keep (only if allowed_attempts != 1) possible values; 'keep_highest', 'keep_latest'
    @JsonProperty("scoring_policy")
    private String scoringPolicy;

    // how many times a student can take the quiz -1 = unlimited attempts
    @JsonProperty("allowed_attempts")
    private int allowedAttempts;

    @JsonProperty("one_question_at_a_time")
    private boolean oneQuestionAtATime;

    @JsonProperty("question_count")
    private int questionCount;

    @JsonProperty("points_possible")
    private int pointsPossible;

    @JsonProperty("cant_go_back")
    private boolean cantGoBack;

    @JsonProperty("access_code")
    private String accessCode;

//    @JsonProperty("ip_filter")
//    private String ipFilter;

    @JsonProperty("due_at")
    private String dueAt;

    @JsonProperty("lock_at")
    private String lockAt;

    @JsonProperty("unlock_at")
    private String unlockAt;

    private boolean published;

//    private boolean unpublishable;

//    @JsonProperty("locked_for_user")
//    private boolean lockedForUser;

//    @JsonProperty("lock_info")
//    private String lockInfo;

//    @JsonProperty("lock_explanation")
//    private String lockExplanation;

//    @JsonProperty("speedgrader_url")
//    private String speedgraderUrl;

//    @JsonProperty("quiz_extensions_url")
//    private String quizExtensionsUrl;

//    private String permissions;

//    @JsonProperty("all_dates")
//    private String allDates;

//    @JsonProperty("version_number")
//    private int versionNumber;

    @JsonProperty("question_types")
    private List<String> questionTypes;

//    @JsonProperty("anonymous_submissions")
//    private boolean anonymousSubmissions;

    @JsonProperty("require_lockdown_browser")
    private boolean requireLockdownBrowser;

    @JsonProperty("require_lockdown_browser_monitor")
    private boolean requireLockdownBrowserMonitor;

    @JsonProperty("lockdown_browser_monitor_data")
    private String lockdownBrowserMonitorData;
}
