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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
@ToString
@Data
/**
 * A DiscussionTopic in Canvas represents both DiscussionTopics and Announcements
 */
public class DiscussionTopic implements Serializable {
    public enum TYPE {
        SIDE_COMMENT,
        THREADED,
        NOT_THREADED;

        @JsonCreator
        public static TYPE fromString(String value) {
            return TYPE.valueOf(value.toUpperCase());
        }

        @JsonValue
        public String toValue() {
            return this.name().toLowerCase();
        }
    }

    /**
     * The ID of this topic
     */
    private String id;

    /**
     *  title
     */
    private String title;

    /**
     * HTML content of the message body
     */
    private String message;

    /**
     * Whether this discussion is published (true) or draft state (false)
     */
    private boolean published;

    /**
     * URL to the topic in Canvas
     */
    @JsonProperty("html_url")
    private String htmlUrl;

    /**
     * The datetime the topic was posted. If it is null it hasn't been posted yet.
     */
    @JsonProperty("posted_at")
    private String postedAt;

    /**
     * The datetime to publish the topic (if not right away).
     */
    @JsonProperty("delayed_post_at")
    private String delayedPostAt;

    /**
     * The datetime for when the last reply was in the topic.
     */
    @JsonProperty("last_reply_at")
    private String lastReplyAt;

    /**
     * The unique identifier of the assignment if the topic is for grading, otherwise null
     */
    @JsonProperty("assignment_id")
    private String assignmentId;

    /**
     * If the topic is for grading and a group assignment this will point to the
     * original topic in the course.
     */
    @JsonProperty("root_topic_id")
    private String rootTopicId;

    private String position;

    /**
     * The type of discussion. Values are 'side_comment', for discussions that only
     * allow one level of nested comments, and 'threaded' for fully threaded
     * discussions.
     */
    @JsonProperty("discussion_type")
    private TYPE discussionType;

    /**
     * The datetime to lock the topic (if ever)
     */
    @JsonProperty("lock_at")
    private String lockAt;

    /**
     * If true then a user may not respond to other replies until that user has made an
     * initial reply. Defaults to false.
     */
    @JsonProperty("require_initial_post")
    private String requireInitialPost;

    /**
     * Whether or not posts in this topic are visible to the user.
     */
    @JsonProperty("user_can_see_posts")
    private String userCanSeePosts;

    /**
     * The read_state of the topic for the current user, 'read' or 'unread'.
     */
    @JsonProperty("read_state")
    private String readState;

    /**
     * true if users can rate/like entries related to this topic
     */
    @JsonProperty("allow_rating")
    private boolean allowRating;

    /**
     * True if grader permissions are required to rate/like entries
     */
    @JsonProperty("only_graders_can_rate")
    private boolean onlyGradersCanRate;

    /**
     * True if entries should be sorted by rating
     */
    @JsonProperty("sort_by_rating")
    private boolean sortByRating;

    /**
     * If the topic is a podcast topic this is the feed url for the current user.
     */
    @JsonProperty("postcast_url")
    private String podcastUrl;

    /**
     * True if podcast will include posts from students, as well
     */
    @JsonProperty("podcast_has_student_posts")
    private boolean podcastHasStudentPosts;

    /**
     * The author's name. Not to be confused with actual username.
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * Author information for this post
     */
    private PostAuthor author;

    /**
     * Attachments for this post
     */
    private List<CanvasFile> attachments;

    @JsonProperty("is_announcement")
    private boolean isAnnouncement;
}