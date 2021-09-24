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

import edu.iu.uits.lms.canvas.model.catalog.CatalogEnrollment;
import edu.iu.uits.lms.canvas.model.catalog.EnrollmentGetWrapper;
import edu.iu.uits.lms.canvas.model.catalog.EnrollmentPostWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriTemplate;

import java.net.URI;

@Service
@Slf4j
public class CatalogListingService extends CatalogSpringBaseService {
    private static final String CATALOG_LISTING_URI = "{url}/enrollments/";
    private static final UriTemplate CATALOG_LISTING_TEMPLATE = new UriTemplate(CATALOG_LISTING_URI);

    /**
     * This was used for initial testing. It works but returns the raw json String vs
     * proper Java objects
     * @param listingId
     * @return the JSON string returned from Catalog representing the enrollments
     */
    public String getEnrollments(String listingId) {
        URI uri = CATALOG_LISTING_TEMPLATE.expand(catalogConfiguration.getBaseApiUrl());
        log.debug(uri.toString());

        EnrollmentGetWrapper enrollmentGetWrapper = new EnrollmentGetWrapper(listingId);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<EnrollmentGetWrapper> enrollmentWrapperHttpEntity = new HttpEntity<>(enrollmentGetWrapper, headers);

            HttpEntity<String> getEnrollmentsResponse = this.restTemplate.exchange(uri, HttpMethod.GET, enrollmentWrapperHttpEntity, String.class);
            log.debug(getEnrollmentsResponse.toString());

            return getEnrollmentsResponse.getBody();

        } catch (HttpClientErrorException hcee) {
            log.error("Error getting catalog enrollments", hcee);
        }

        return null;
    }

    public boolean addUserToListing(String canvasUserId, String listingId) {
        URI uri = CATALOG_LISTING_TEMPLATE.expand(catalogConfiguration.getBaseApiUrl());
        log.debug(uri.toString());

        EnrollmentPostWrapper enrollmentPostWrapper = new EnrollmentPostWrapper(new CatalogEnrollment(canvasUserId, listingId));

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

            HttpEntity<EnrollmentPostWrapper> enrollmentWrapperHttpEntity = new HttpEntity<>(enrollmentPostWrapper, headers);

            HttpEntity<String> createEnrollmentResponse = this.restTemplate.exchange(uri, HttpMethod.POST, enrollmentWrapperHttpEntity, String.class);
            log.debug(createEnrollmentResponse.toString());

            HttpStatus responseStatus = ((ResponseEntity<String>) createEnrollmentResponse).getStatusCode();

            if (HttpStatus.CREATED.equals(responseStatus)) {
                return true;
            } else {
                log.error("Error creating enrollment term. Request to Canvas was not successful. Response code: "
                        + responseStatus + ", reason: " + responseStatus.getReasonPhrase()
                        + ", entity: " + createEnrollmentResponse);
            }

        } catch (HttpClientErrorException hcee) {
            log.error("Error adding user to listing", hcee);
        }

        return false;
    }
}
