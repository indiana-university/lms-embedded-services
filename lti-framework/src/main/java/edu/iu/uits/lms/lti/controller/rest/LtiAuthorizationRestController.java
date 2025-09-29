package edu.iu.uits.lms.lti.controller.rest;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

@Profile(LTIREST_PROFILE)
@RestController
@RequestMapping("/rest/lti/authz")
@Tag(name = "LtiAuthorizationRestController", description = "Interact with the LmsLtiAuthz repository with CRUD operations")
public class LtiAuthorizationRestController {

    @Autowired
    private LtiAuthorizationRepository ltiAuthorizationRepository = null;

    @GetMapping("/")
    @Operation(summary = "Get an LmsLtiAuthz by registrationId and env")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLtiAuthz findByRegistrationEnvActive(@RequestParam("registrationId") String registrationId, @RequestParam("env") String env) {
        return ltiAuthorizationRepository.findByRegistrationEnvActive(registrationId, env);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an LmsLtiAuthz by id")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLtiAuthz findById(@PathVariable("id") Long id) {
        return ltiAuthorizationRepository.findById(id).orElse(null);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all LmsLtiAuthz records")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public List<LmsLtiAuthz> getAuthzs(@RequestParam(required = false, defaultValue = "false") boolean includeSecrets) {
        List<LmsLtiAuthz> results = (List<LmsLtiAuthz>)ltiAuthorizationRepository.findAll();

        return results;
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an LmsLtiAuthz by id")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLtiAuthz updateAuthz(@PathVariable("id") Long id, @RequestBody LmsLtiAuthz lmsLtiAuthz) {
        LmsLtiAuthz updatedAuthz = ltiAuthorizationRepository.findById(id).orElse(null);

        if (lmsLtiAuthz.getRegistrationId() != null) {
            updatedAuthz.setRegistrationId(lmsLtiAuthz.getRegistrationId());
        }
        if (lmsLtiAuthz.getClientId() != null) {
            updatedAuthz.setClientId(lmsLtiAuthz.getClientId());
        }
        if (lmsLtiAuthz.getEnv() != null) {
            updatedAuthz.setEnv(lmsLtiAuthz.getEnv());
        }
        updatedAuthz.setActive(lmsLtiAuthz.isActive());

        return ltiAuthorizationRepository.save(updatedAuthz);
    }

    @PostMapping("/")
    @Operation(summary = "Create a new LmsLtiAuthz")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLtiAuthz createAuthz(@RequestBody LmsLtiAuthz lmsLtiAuthz) {
        return ltiAuthorizationRepository.save(lmsLtiAuthz);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an LmsLtiAuthz by id")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public String deleteAuthz(@PathVariable("id") Long id) {
        ltiAuthorizationRepository.deleteById(id);
        return "Delete success.";
    }
}
