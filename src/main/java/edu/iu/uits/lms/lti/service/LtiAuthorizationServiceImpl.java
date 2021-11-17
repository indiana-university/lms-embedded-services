package edu.iu.uits.lms.lti.service;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
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

import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/rest/lti"})
public class LtiAuthorizationServiceImpl extends BaseService {

    @Autowired
    private LtiAuthorizationRepository ltiAuthorizationRepository = null;

    public LmsLtiAuthz findByKeyContextActive(String consumerKey, String context) {
        return ltiAuthorizationRepository.findByKeyContextActive(consumerKey, context);
    }

    @GetMapping(value = "/authz/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public LmsLtiAuthz findById(@PathVariable("id") Long id) {
        return ltiAuthorizationRepository.findById(id).orElse(null);
    }

    @GetMapping(value = "/authz/all", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<LmsLtiAuthz> getAuthzNoSecrets() {
        return getAuthzs(false);
    }

    @GetMapping(value = "/authz/all/{includesecrets}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<LmsLtiAuthz> getAuthzs(@PathVariable("includesecrets") boolean includeSecrets) {
        List<LmsLtiAuthz> results = (List<LmsLtiAuthz>)ltiAuthorizationRepository.findAll();

        if (!includeSecrets) {
            results.forEach(a -> a.setSecret("********"));
        }
        return results;
    }

    @PutMapping(value = "/authz/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public LmsLtiAuthz updateAuthz(@PathVariable("id") Long id, @RequestBody LmsLtiAuthz lmsLtiAuthz) {
        LmsLtiAuthz updatedAuthz = ltiAuthorizationRepository.findById(id).orElse(null);

        if (lmsLtiAuthz.getConsumerKey() != null) {
            updatedAuthz.setConsumerKey(lmsLtiAuthz.getConsumerKey());
        }
        if (lmsLtiAuthz.getContext() != null) {
            updatedAuthz.setContext(lmsLtiAuthz.getContext());
        }
        if (lmsLtiAuthz.getSecret() != null) {
            updatedAuthz.setSecret(lmsLtiAuthz.getSecret());
        }
        updatedAuthz.setActive(lmsLtiAuthz.isActive());

        return ltiAuthorizationRepository.save(updatedAuthz);
    }

    @PostMapping(value = "/authz", produces = {MediaType.APPLICATION_JSON_VALUE})
    public LmsLtiAuthz createAuthz(@RequestBody LmsLtiAuthz lmsLtiAuthz) {
        return ltiAuthorizationRepository.save(lmsLtiAuthz);
    }

    @DeleteMapping(value = "/authz/{id}")
    public String deleteAuthz(@PathVariable("id") Long id) {
        ltiAuthorizationRepository.deleteById(id);
        return "Delete success.";
    }
}
