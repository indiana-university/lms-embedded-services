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

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties (ignoreUnknown=true)
@Data
public class User implements Serializable {

	private String id;

	//The name of the user.
	private String name;

	//The name of the user that is should be used for sorting groups of users, such as
	//in the gradebook.
	@JsonProperty("sortable_name")
	private String sortableName;

	//A short name the user has selected, for use in conversations or other less
	//formal places through the site.
	@JsonProperty("short_name")
	private String shortName;

	//The SIS ID associated with the user.  This field is only included if the user
	//came from a SIS import and has permissions to view SIS information.
	@JsonProperty("sis_user_id")
	private String sisUserId;

	//The id of the SIS import.  This field is only included if the user came from a
	//SIS import and has permissions to manage SIS information.
	@JsonProperty("sis_import_id")
	private String sisImportId;

	//The unique login id for the user.  This is what the user uses to log in to
	//Canvas.
	@JsonProperty("login_id")
	private String loginId;

	//If avatars are enabled, this field will be included and contain a url to
	//retrieve the user's avatar.
	@JsonProperty("avatar_url")
	private String avatarUrl;

	//Optional: This field can be requested with certain API calls, and will return a
	//list of the users active enrollments. See the List enrollments API for more
	//details about the format of these records.
	private List<Enrollment> enrollments;

	//Optional: This field can be requested with certain API calls, and will return
	//the users primary email address.
	private String email;

	//Optional: This field can be requested with certain API calls, and will return
	//the users locale.
	private String locale;

	//Optional: This field is only returned in certain API calls, and will return a
	//timestamp representing the last time the user logged in to canvas.
	@JsonProperty("last_login")
	private String lastLogin;

	//Optional: This field is only returned in ceratin API calls, and will return the
	//IANA time zone name of the user's preferred timezone.
	@JsonProperty("time_zone")
	private String timeZone;
}
