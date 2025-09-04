package edu.iu.uits.lms.iuonly.services;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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
import edu.iu.uits.lms.canvas.model.CanvasTerm;
import edu.iu.uits.lms.iuonly.model.ProvisioningTerm;
import edu.iu.uits.lms.iuonly.model.Term;
import edu.iu.uits.lms.iuonly.repository.ProvisioningTermRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by chmaurer on 6/14/17.
 */
@Service
@Slf4j
public class LmsTermService {

    @Autowired
    private ProvisioningTermRepository provisioningTermRepository = null;

    public List<Term> getTermsList() {
        Iterable<ProvisioningTerm> provisioningTerms = provisioningTermRepository.findAll();

        List<Term> tempTermsList = new ArrayList<Term>();

        for (ProvisioningTerm provTerm : provisioningTerms) {
            Term tempTerm = new Term(provTerm, CanvasConstants.ACTIVE_STATUS);
            // add Term to termsList
            tempTermsList.add(tempTerm);
        }
        return tempTermsList;
    }

    public Map<String, String> makeTermMap(List<CanvasTerm> enrollmentTerms) {
        Map<String, String> termMap = new HashMap<>();
        for (CanvasTerm term : enrollmentTerms) {
            termMap.put(term.getSisTermId(), term.getId());
        }
        return termMap;
    }

    /**
     * Gets the maximum term being provisioned
     * @return
     */
    public Term getCurrentTerm() {
        List<Term> beingProvisionedTerms = getTermsList();

        if (beingProvisionedTerms == null || beingProvisionedTerms.size() == 0) {
            return null;
        }

        Optional<Term> maxTerm = beingProvisionedTerms.stream().max(Comparator.comparing(Term::getTermId));

        return maxTerm.get();
    }

    public String termIds2DelimitedString() {
        List<Term> terms = getTermsList();
        List<String> termIds = terms.stream().map(Term::getTermId).collect(Collectors.toList());
        return StringUtils.collectionToDelimitedString(termIds, ", ", "'", "'");
    }
}
