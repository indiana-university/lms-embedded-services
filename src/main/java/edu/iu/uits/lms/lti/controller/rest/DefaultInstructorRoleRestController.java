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

import edu.iu.uits.lms.lti.model.DefaultInstructorRole;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/rest/lti/instructors")
public class DefaultInstructorRoleRestController {

    protected final static String READ_SCOPE = "SCOPE_lti:read";
    protected final static String WRITE_SCOPE = "SCOPE_lti:write";

    @Autowired
    private DefaultInstructorRoleRepository defaultInstructorRoleRepository = null;

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public DefaultInstructorRole findById(@PathVariable("id") Long id) {
        return defaultInstructorRoleRepository.findById(id).orElse(null);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public List<DefaultInstructorRole> getAll() {
        List<DefaultInstructorRole> results = (List<DefaultInstructorRole>) defaultInstructorRoleRepository.findAll();
        return results;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public DefaultInstructorRole update(@PathVariable("id") Long id, @RequestBody DefaultInstructorRole defaultInstructorRole) {
        DefaultInstructorRole updated = defaultInstructorRoleRepository.findById(id).orElse(null);

        if (defaultInstructorRole.getRole() != null) {
            updated.setRole(defaultInstructorRole.getRole());
        }

        return defaultInstructorRoleRepository.save(updated);
    }

    @PostMapping("/")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public DefaultInstructorRole create(@RequestBody DefaultInstructorRole defaultInstructorRole) {
        return defaultInstructorRoleRepository.save(defaultInstructorRole);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public String delete(@PathVariable("id") Long id) {
        defaultInstructorRoleRepository.deleteById(id);
        return "Delete success.";
    }
}
