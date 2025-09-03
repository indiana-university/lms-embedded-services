package edu.iu.uits.lms.iuonly.services;

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
