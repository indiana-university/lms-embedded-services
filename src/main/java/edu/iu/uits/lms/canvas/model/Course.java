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
import java.util.Map;

@JsonIgnoreProperties (ignoreUnknown=true)
@ToString
@Data
public class Course implements Serializable {

	@JsonProperty("account_id")
	private String accountId;
	
	@JsonProperty("course_code")
	private String courseCode;
	
	@JsonProperty("default_view")
	private String defaultView;
	
	private String id;
	
	private String name;

	@JsonProperty("start_at")
	private String startAt;

	@JsonProperty("end_at")
	private String endAt;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("public_syllabus")
	private boolean publicSyllabus;

	@JsonProperty("storage_quota_mb")
	private int storageQuotaMb;

	@JsonProperty("hide_final_grades")
	private boolean hideFinalGrades;

	@JsonProperty("apply_assignment_group_weights")
	private boolean applyAssignmentGroupWeights;

	private Calendar calendar;
	
	@JsonProperty("sis_course_id")
	private String sisCourseId;

	@JsonProperty("integration_id")
	private String integrationId;

	private List<Enrollment> enrollments;

	@JsonProperty("workflow_state")
	private String workflowState;

	@JsonProperty("enrollment_term_id")
	private String enrollmentTermId;

	private String license;

	@JsonProperty("restrict_enrollments_to_course_dates")
	private boolean restrictEnrollmentsToCourseDates;
	
	/**
	 * the grading standard associated with the course
	 */
	@JsonProperty("grading_standard_id")
	private String gradingStandardId;

	private CanvasTerm term;

	private List<Section> sections;

	private boolean blueprint;

	@JsonProperty("blueprint_restrictions")
	private BlueprintRestriction blueprintRestrictions;

	@JsonProperty("blueprint_restrictions_by_object_type")
	private Map<String, BlueprintRestriction> blueprintRestrictionsByObjectType;

	@JsonProperty("is_favorite")
	private boolean favorite;

	@JsonProperty("original_name")
	private String originalName;

	@JsonProperty("access_restricted_by_date")
	private boolean accessRestrictedByDate;

}
