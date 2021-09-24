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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LtiAuthorizationServiceImpl extends BaseService {

    @Autowired
    private LtiAuthorizationRepository ltiAuthorizationRepository = null;

    public LmsLtiAuthz findByKeyContextActive(String consumerKey, String context) {
        return ltiAuthorizationRepository.findByKeyContextActive(consumerKey, context);
    }

    public LmsLtiAuthz findById(Long id) {
        return ltiAuthorizationRepository.findById(id).orElse(null);
    }

    public List<LmsLtiAuthz> getAuthzs(boolean includeSecrets) {
        List<LmsLtiAuthz> results = (List<LmsLtiAuthz>)ltiAuthorizationRepository.findAll();

        if (!includeSecrets) {
            results.forEach(a -> a.setSecret("********"));
        }
        return results;
    }

    public LmsLtiAuthz updateAuthz(Long id, LmsLtiAuthz lmsLtiAuthz) {
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

    public LmsLtiAuthz createAuthz(LmsLtiAuthz lmsLtiAuthz) {
        return ltiAuthorizationRepository.save(lmsLtiAuthz);
    }

    public String deleteAuthz(Long id) {
        ltiAuthorizationRepository.deleteById(id);
        return "Delete success.";
    }
}
