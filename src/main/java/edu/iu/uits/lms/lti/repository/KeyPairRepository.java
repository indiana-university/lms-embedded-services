package edu.iu.uits.lms.lti.repository;

import edu.iu.uits.lms.lti.model.KeyPair;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

@Component
public interface KeyPairRepository extends PagingAndSortingRepository<KeyPair, Long> {

   KeyPair findFirstByOrderByIdAsc();
}
