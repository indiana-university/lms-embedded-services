package edu.iu.uits.lms.iuonly.services.rest;

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

import edu.iu.uits.lms.iuonly.model.ProvisioningTerm;
import edu.iu.uits.lms.iuonly.repository.ProvisioningTermRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;

@Profile(IUCUSTOMREST_PROFILE)
@RestController
@RequestMapping("/rest/iu/prov_terms")
public class ProvisioningTermRestController {

   @Autowired
   private ProvisioningTermRepository provisioningTermRepository = null;

   @GetMapping("/all")
   public List<ProvisioningTerm> getAll() {
      return (List<ProvisioningTerm>) provisioningTermRepository.findAll();
   }

   @PutMapping("/{sisTermId}")
   public ProvisioningTerm update(@PathVariable String sisTermId, @RequestBody ProvisioningTerm provisioningTerm) {
      ProvisioningTerm updated = provisioningTermRepository.findById(sisTermId).orElse(null);

      if (provisioningTerm.getName() != null) {
         updated.setName(provisioningTerm.getName());
      }
      if (provisioningTerm.getStartDate() != null) {
         updated.setStartDate(provisioningTerm.getStartDate());
      }
      if (provisioningTerm.getEndDate() != null) {
         updated.setEndDate(provisioningTerm.getEndDate());
      }

      return provisioningTermRepository.save(updated);
   }

   @PostMapping("/")
   public ProvisioningTerm create(@RequestBody ProvisioningTerm provisioningTerm) {
      return provisioningTermRepository.save(provisioningTerm);
   }

   @DeleteMapping("/{sisTermId}")
   public String delete(@PathVariable String sisTermId) {
      provisioningTermRepository.deleteById(sisTermId);
      return "Delete success.";
   }

}
