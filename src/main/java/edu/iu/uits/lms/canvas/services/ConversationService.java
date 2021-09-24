package edu.iu.uits.lms.canvas.services;

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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.Conversation;
import edu.iu.uits.lms.canvas.model.ConversationCreateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

/**
 * API for accessing conversations
 * @see <a href="https://canvas.instructure.com/doc/api/conversations.html">Conversations API</a>
 */
@Service
@Slf4j
public class ConversationService extends SpringBaseService {
	private static final String BASE_URI = "{url}/conversations";
	private static final String DELETE_URI = BASE_URI + "/{id}";
	private static final String GET_URI = BASE_URI + "/{id}";

	private static final UriTemplate BASE_TEMPLATE = new UriTemplate(BASE_URI);
	private static final UriTemplate DELETE_TEMPLATE = new UriTemplate(DELETE_URI);
	private static final UriTemplate GET_TEMPLATE = new UriTemplate(GET_URI);

//	@PostMapping
//	@PreAuthorize("#oauth2.hasScope('" + WRITE_SCOPE + "')")
//	public Conversation postConversation(ConversationCreateWrapper conversationCreateWrapper, @RequestParam String asUser) {
//		return postConversation(conversationCreateWrapper, asUser, false);
//	}

	/**
	 *
	 * @param conversationCreateWrapper ConversationCreateWrapper to create
	 * @param asUser optional - masquerade as this user when creating the conversation. If you wish to use an sis_login_id,
	 * 	      prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
	 * @param isBulk Whether to send as a canvas bulk asynchronous message
	 * @return The conversation that was created
	 */
	public Conversation postConversation(ConversationCreateWrapper conversationCreateWrapper, String asUser, boolean isBulk) {
		URI uri = BASE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl());

		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		// If we include these as query params, we will hit issues with special and reserved characters (like ;) in the subject and body.
		// This may be reworked when we upgrade to Spring 5, but using the request entity avoids these issues now
		MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
		map.add("subject", conversationCreateWrapper.getSubject());
		map.add("body", conversationCreateWrapper.getBody());

		if (isBulk) {
			conversationCreateWrapper.setGroupConversation(true);
			conversationCreateWrapper.setMode("async");
			map.add("bulk_message", String.valueOf(true));
		} else {
			map.add("force_new", String.valueOf(true));
		}

		map.add("group_conversation", String.valueOf(conversationCreateWrapper.isGroupConversation()));

		// Canvas will return a 500 error if you set group_conversation to false and populate the context_code for some reason
		if (conversationCreateWrapper.isGroupConversation()) {
			map.add("context_code", conversationCreateWrapper.getContextCode());
		}

		if (conversationCreateWrapper.getMode() != null) {
			map.add("mode", conversationCreateWrapper.getMode());
		}

		if (asUser != null) {
			map.add("as_user_id", asUser);
		}

		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

		if (conversationCreateWrapper.getAttachmentIds() != null) {
			for (String attachmentId : conversationCreateWrapper.getAttachmentIds()) {
				builder.queryParam("attachment_ids[]", attachmentId);
			}
		}

		for (String recipient: conversationCreateWrapper.getRecipients()) {
			builder.queryParam("recipients[]", recipient);
		}

		try {
			HttpEntity<Conversation[]> conversationsEntity = restTemplate.postForEntity(builder.build().toUri(), requestEntity, Conversation[].class);

			if (conversationsEntity != null &&
					conversationsEntity.getBody() != null &&
					conversationsEntity.getBody().length > 0) {

				// For some reason Canvas returns a collection of conversations on conversation posting. In this case a collection
				// of 1. So we grab the first/only one to return.
				return conversationsEntity.getBody()[0];
			}
		} catch (HttpClientErrorException hcee) {
			log.error("Error posting conversation", hcee);
		}

		return null;
	}

	/**
	 * Delete a conversation
	 * @param id Id of the conversation to delete
	 * @param asUser The author of the conversation. If you wish to use an sis_login_id, prefix asUser with
	 * 	 *         {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
	 * @return The Conversation that was deleted
	 */
	public Conversation deleteConversation(String id, String asUser) {
		URI uri = DELETE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);
		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

		if (asUser != null) {
			builder.queryParam("as_user_id", asUser);
		}

		HttpEntity<Conversation> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.DELETE, null, Conversation.class);

		return response.getBody();
	}

	/**
	 *
	 * @param id Id of the conversation
	 * @param asUser you must masquerade as the author of the conversation to retrieve it. If you wish to use an sis_login_id,
	 *         prefix asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
	 * @return The conversation with the given id
	 */
	public Conversation getConversation(String id, String asUser) {
		URI uri = GET_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), id);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

		if (asUser != null) {
			builder.queryParam("as_user_id", asUser);
		}

		HttpEntity<Conversation> response = restTemplate.exchange(builder.build().toUri(), HttpMethod.GET, null, Conversation.class);

		return response.getBody();
	}

}
