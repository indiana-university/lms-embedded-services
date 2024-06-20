package edu.iu.uits.lms.iuonly.services;

import edu.iu.uits.lms.iuonly.model.JobCompletionDetail;
import edu.iu.uits.lms.iuonly.repository.JobCompletionDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class JobCompletionDetailService {

    @Autowired
    private JobCompletionDetailRepository jobCompletionDetailRepository;

    public void addJobCompletionDetail(String jobCode, String jobHost, String details, boolean success, boolean hadErrors) {
        jobCompletionDetailRepository.save(JobCompletionDetail.builder()
                        .jobCode(jobCode).jobHost(jobHost).details(details).success(success).errors(hadErrors)
                .build());
    }

    public JobCompletionDetail getDetailsByJob(String jobHost) {
        List<JobCompletionDetail> jobDetails = jobCompletionDetailRepository.findByJobHost(jobHost);
        JobCompletionDetail jcd = null;
        if (jobDetails != null && !jobDetails.isEmpty()) {
            if (jobDetails.size() > 1) {
                log.warn("Returned more job details than expected for {}.  Returning the first result.", jobHost);
            }
            jcd = jobDetails.get(0);
        }
        return jcd;
    }
}
