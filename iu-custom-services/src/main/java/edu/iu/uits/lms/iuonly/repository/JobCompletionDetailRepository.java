package edu.iu.uits.lms.iuonly.repository;

import edu.iu.uits.lms.iuonly.model.JobCompletionDetail;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface JobCompletionDetailRepository extends PagingAndSortingRepository<JobCompletionDetail, Long> {

    List<JobCompletionDetail> findByJobHost(String jobHost);
}
