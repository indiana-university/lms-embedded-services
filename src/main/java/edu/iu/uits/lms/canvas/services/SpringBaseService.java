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

import edu.iu.uits.lms.canvas.config.CanvasConfiguration;
import edu.iu.uits.lms.canvas.model.CanvasEnrollmentTerms;
import edu.iu.uits.lms.canvas.utils.LinkHeaderParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service("SpringBaseService")
@Slf4j
public class SpringBaseService extends BaseService {

    @Autowired
    @Qualifier("CanvasRestTemplate")
    protected RestTemplate restTemplate;

    @Autowired
    @Qualifier("restTemplateNoBuffer")
    protected RestTemplate restTemplateNoBuffer;

    @Autowired
    protected CanvasConfiguration canvasConfiguration;

    /**
     * Do a GET request, paginating through the entire dataset
     * @param uri
     * @param setType
     * @param <T>
     * @return
     */
    protected <T> List<T> doGet(URI uri, Class<T[]> setType) {
        List<T> resultList = new ArrayList<T>();
        try {
            ResponseEntity<T[]> entity = restTemplate.getForEntity(uri, setType);

            LinkHeaderParser lhp = new LinkHeaderParser(entity.getHeaders());
            resultList.addAll(Arrays.asList(entity.getBody()));
            if (lhp.hasLinkHeader()) {
                String nextLink = lhp.getNext();
                if (nextLink != null) {
                    try {
                        resultList.addAll(doGet(new URI(nextLink), setType));
                    } catch (URISyntaxException e) {
                        log.error("error parsing uri");
                    }
                }
            }
        } catch (HttpStatusCodeException e) {
            log.warn("uh oh", e);
            //Should be safe to ignore and just return the empty list
        }
        return resultList;
    }

    /**
     * In most situations, you will use doGet to paginate through a dataset. Use this method if Canvas is
     * returning a single element at the root level instead of a Collection of objects (see {@link CanvasEnrollmentTerms}
     * for an example).
     * @param uri
     * @param setType
     * @param <T>
     * @return
     */
    protected <T> List<T> doGetSingle(URI uri, Class<T> setType) {
        List<T> resultList = new ArrayList<T>();
        try {
            ResponseEntity<T> entity = restTemplate.getForEntity(uri, setType);

            LinkHeaderParser lhp = new LinkHeaderParser(entity.getHeaders());
            resultList.add(entity.getBody());
            if (lhp.hasLinkHeader()) {
                String nextLink = lhp.getNext();
                if (nextLink != null) {
                    try {
                        resultList.addAll(doGetSingle(new URI(nextLink), setType));
                    } catch (URISyntaxException e) {
                        log.error("error parsing uri");
                    }
                }
            }
        } catch (HttpStatusCodeException e) {
            log.warn("Error occurred attempting to paginate through dataset", e);
            //Should be safe to ignore and just return the empty list
        }
        return resultList;
    }
}
