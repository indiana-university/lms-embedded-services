package edu.iu.uits.lms.iuonly.services.rest;

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
