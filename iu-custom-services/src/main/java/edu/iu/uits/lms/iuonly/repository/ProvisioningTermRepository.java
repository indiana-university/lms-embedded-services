package edu.iu.uits.lms.iuonly.repository;

import edu.iu.uits.lms.iuonly.model.ProvisioningTerm;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface ProvisioningTermRepository extends PagingAndSortingRepository<ProvisioningTerm, String>,
        ListCrudRepository<ProvisioningTerm, String> {
}
