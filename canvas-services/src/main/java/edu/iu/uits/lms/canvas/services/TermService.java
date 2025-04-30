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

import edu.iu.uits.lms.canvas.model.CanvasEnrollmentTerms;
import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.canvas.model.CanvasTermCreateWrapper;
import edu.iu.uits.lms.canvas.utils.CacheConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by chmaurer on 1/14/15.
 */
@Service
@Slf4j
public class TermService extends SpringBaseService {

	private static final String TERMS_URI = "{url}/accounts/{account_id}/terms";
	private static final UriTemplate TERMS_TEMPLATE = new UriTemplate(TERMS_URI);

	/**
	 * Get the list of enrollment term objects from canvas
	 * @return List of CanvasTerm objects
	 */
   @Cacheable(value = CacheConstants.ENROLLMENT_TERMS_CACHE_NAME, cacheManager = "CanvasServicesCacheManager")
	public List<CanvasTerm> getEnrollmentTerms() {
		URI uri = TERMS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasConfiguration.getAccountId());
		log.debug("{}", uri);

		UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

		builder.queryParam("per_page", "50");
		builder.queryParam("include[]", "overrides");

		List<CanvasEnrollmentTerms> termsList =  doGetSingle(builder.build().toUri(), CanvasEnrollmentTerms.class);

		List<CanvasTerm> terms = new ArrayList<>();

		if (termsList != null && !termsList.isEmpty()) {
			for (CanvasEnrollmentTerms canvasTermList : termsList) {
				terms.addAll(canvasTermList.getEnrollmentTerms());
			}
		} else {
			log.warn("No enrollment terms returned.");
		}

		return terms;
    }

	/**
	 * Get a term by its id
	 * @param termId Canvas term id
	 * @return CanvasTerm
	 */
	public CanvasTerm getTermById(String termId) {
		List<CanvasTerm> allTerms = getEnrollmentTerms();

		return allTerms.stream()
				.filter(term -> termId.equals(term.getId()))
				.findFirst()
				.orElse(null);
	}

	/**
	 * Get a term by its sis id
	 * @param sisTermId Canvas sis term id
	 * @return CanvasTerm
	 */
	public CanvasTerm getTermBySisId(String sisTermId) {
		List<CanvasTerm> allTerms = getEnrollmentTerms();

		return allTerms.stream()
				.filter(term -> sisTermId.equals(term.getSisTermId()))
				.findAny()
				.orElse(null);
	}

	/**
	 *
	 * @return the Canvas term for the current year. Will create term if it does not exist (which is unlikely).
	 * Returns null if creation of the new term failed
	 */
	public CanvasTerm getCurrentYearTerm() {
		CanvasTerm currentYearTerm = null;

		Calendar cal = Calendar.getInstance();
		String year = String.valueOf(cal.get(Calendar.YEAR));

		boolean createNewTerm = true;

		// look up to make sure the current year term exists in Canvas
		List<CanvasTerm> enrollmentTerms = getEnrollmentTerms();
		for (CanvasTerm canvasTerm : enrollmentTerms) {
			if (year.equals(canvasTerm.getSisTermId())) {
				currentYearTerm = canvasTerm;
				createNewTerm = false;
				break;
			}
		}

		// this block of code is for writing a new term and will almost never be called
		if (createNewTerm) {
			// code to write a new term to Canvas
			// year = the new id for the year

			CanvasTerm newTerm = new CanvasTerm();
			newTerm.setSisTermId(year);
			newTerm.setName(year);
			newTerm.setStartAt(year + "-01-01T05:00:00Z");
			newTerm.setEndAt(year + "-12-31T05:00:00Z");

			CanvasTermCreateWrapper termWrapper = new CanvasTermCreateWrapper();
			termWrapper.setEnrollmentTerm(newTerm);

			currentYearTerm = createCanvasTerm(termWrapper);
		}

		return currentYearTerm;
	}

	/**
	 *
	 * @param newTerm
	 * @return the newly created CanvasTerm object. Returns null if the creation failed.
	 */
	public CanvasTerm createCanvasTerm(CanvasTermCreateWrapper newTerm) {

		CanvasTerm savedTerm = null;

		URI uri = TERMS_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), canvasConfiguration.getAccountId());

		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

			HttpEntity<CanvasTermCreateWrapper> termCreateWrapperRequestEntity = new HttpEntity<>(newTerm, headers);

			ResponseEntity<CanvasTerm> createTermResponse = this.restTemplate.exchange(uri, HttpMethod.POST, termCreateWrapperRequestEntity, CanvasTerm.class);
			log.debug("{}", createTermResponse);

			HttpStatusCode responseStatus = createTermResponse.getStatusCode();

			if (HttpStatus.OK.equals(responseStatus)) {
				savedTerm = createTermResponse.getBody();
			} else {
				log.error("Error creating enrollment term. Request to Canvas was not successful. Response code: "
						+ responseStatus + ", reason: " + ((HttpStatus)responseStatus).getReasonPhrase()
						+ ", entity: " + createTermResponse);
			}

		} catch (HttpClientErrorException hcee) {
			log.error("uh oh", hcee);
		}

		return savedTerm;
	}

}
