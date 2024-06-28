package edu.iu.uits.lms.canvas.utils;

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

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.net.URLEncodedUtils;
import org.glassfish.jersey.message.internal.JerseyLink;
import org.springframework.http.HttpHeaders;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class LinkHeaderParser {

    private static final String LINK_DELIM = ",";

    private HttpHeaders headers;
    private Map<LINK_KEY, String> headerMap = new HashMap<>();

    private enum LINK_KEY {
        FIRST, PREV, NEXT, LAST, CURRENT;
    }

    public LinkHeaderParser(HttpHeaders headers) {
        this.headers = headers;
        parse();
    }

    private void parse() {
        String linkHeaderValue = headers.getFirst(HttpHeaders.LINK);

        if(linkHeaderValue != null) {
            List<String> links = Arrays.asList(linkHeaderValue.split(LINK_DELIM));
            for (String linkVal : links) {
                String trimmedLink = linkVal.trim();
                if (!trimmedLink.isEmpty()) {
                    JerseyLink.Builder b = new JerseyLink.Builder();
                    JerseyLink link = b.link(trimmedLink).build();
                    headerMap.put(LINK_KEY.valueOf(link.getRel().toUpperCase()), link.getUri().toString());
                }
            }
        }
    }

    private Object extractTypeOfRelation(final String linkRelation) {
        final int positionOfEquals = linkRelation.indexOf('=');
        return linkRelation.substring(positionOfEquals + 2, linkRelation.length() - 1).trim();
    }

    public boolean hasLinkHeader() {
        return headerMap.size() > 0;
    }

    public String getFirst() {
        return headerMap.get(LINK_KEY.FIRST);
    }

    public String getNext() {
        return headerMap.get(LINK_KEY.NEXT);
    }

    public String getLast() {
        return headerMap.get(LINK_KEY.LAST);
    }

    public String getPrevious() {
        return headerMap.get(LINK_KEY.PREV);
    }

    public String getCurrent() {
        return headerMap.get(LINK_KEY.CURRENT);
    }

    public String debug(String currentUrl) {
        String lastLink = getLast();
        String lastPage = getPageForUrl(lastLink);
        String currentPage = getPageForUrl(currentUrl);
        return "Page " + currentPage + " of " + lastPage;
    }

    private String getPageForUrl(String url) {
        List<NameValuePair> queryParams = URLEncodedUtils.parse(url, Charset.defaultCharset());
        Map<String, String> paramMap = queryParams.stream()
                .collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));
        return paramMap.get("page");
    }
}

