package edu.iu.uits.lms.lti.controller.rest.legacy;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2023 Indiana University
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

import edu.iu.uits.lms.lti.model.legacy.LmsLti11Authz;
import edu.iu.uits.lms.lti.repository.legacy.Lti11AuthorizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.iu.uits.lms.lti.LTIConstants.LTIREST_PROFILE;
import static edu.iu.uits.lms.lti.LTIConstants.READ_SCOPE;
import static edu.iu.uits.lms.lti.LTIConstants.WRITE_SCOPE;

/**
 * @deprecated This class was temporarily added and is not intended for long-term use.  Please use the 1.3 launch mechanism instead.
 */
@Profile(LTIREST_PROFILE)
@RestController
@RequestMapping("/rest/lti/legacy/authz")
@Tag(name = "Lti11AuthorizationRestController", description = "Interact with the LmsLti11Authz repository with CRUD operations")
@Deprecated(since = "5.2.2", forRemoval = true)
public class Lti11AuthorizationRestController {

    @Autowired
    private Lti11AuthorizationRepository lti11AuthorizationRepository = null;

    @GetMapping("/")
    @Operation(summary = "Get an LmsLti11Authz by consumerKey and context")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLti11Authz findByKeyContextActive(@RequestParam("consumerKey") String consumerKey, @RequestParam("context") String context) {
        return lti11AuthorizationRepository.findByKeyContextActive(consumerKey, context);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an LmsLti11Authz by id")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLti11Authz findById(@PathVariable("id") Long id) {
        return lti11AuthorizationRepository.findById(id).orElse(null);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all LmsLti11Authz records")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public List<LmsLti11Authz> getAuthzs(@RequestParam(required = false, defaultValue = "false") boolean includeSecrets) {
        List<LmsLti11Authz> results = (List<LmsLti11Authz>) lti11AuthorizationRepository.findAll();

        if (!includeSecrets) {
            results.forEach(a -> a.setSecret("********"));
        }
        return results;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an LmsLti11Authz by id")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLti11Authz updateAuthz(@PathVariable("id") Long id, @RequestBody LmsLti11Authz lmsLtiAuthz) {
        LmsLti11Authz updatedAuthz = lti11AuthorizationRepository.findById(id).orElse(null);

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

        return lti11AuthorizationRepository.save(updatedAuthz);
    }

    @PostMapping("/")
    @Operation(summary = "Create a new LmsLti11Authz")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLti11Authz createAuthz(@RequestBody LmsLti11Authz lmsLtiAuthz) {
        return lti11AuthorizationRepository.save(lmsLtiAuthz);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an LmsLti11Authz by id")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public String deleteAuthz(@PathVariable("id") Long id) {
        lti11AuthorizationRepository.deleteById(id);
        return "Delete success.";
    }
}
