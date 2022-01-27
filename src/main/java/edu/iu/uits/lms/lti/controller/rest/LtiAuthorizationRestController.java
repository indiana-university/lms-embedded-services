package edu.iu.uits.lms.lti.controller.rest;

import edu.iu.uits.lms.lti.model.LmsLtiAuthz;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
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

@RestController
@RequestMapping("/rest/lti/authz")
public class LtiAuthorizationRestController {

    protected final static String READ_SCOPE = "SCOPE_lti:read";
    protected final static String WRITE_SCOPE = "SCOPE_lti:write";

    @Autowired
    private LtiAuthorizationRepository ltiAuthorizationRepository = null;

    @GetMapping("/")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLtiAuthz findByKeyContextActive(@RequestParam("consumerKey") String consumerKey,
                                              @RequestParam("context") String context) {
        return ltiAuthorizationRepository.findByKeyContextActive(consumerKey, context);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public LmsLtiAuthz findById(@PathVariable("id") Long id) {
        return ltiAuthorizationRepository.findById(id).orElse(null);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('" + READ_SCOPE + "')")
    public List<LmsLtiAuthz> getAuthzs(@RequestParam(required = false, defaultValue = "false") boolean includeSecrets) {
        List<LmsLtiAuthz> results = (List<LmsLtiAuthz>)ltiAuthorizationRepository.findAll();

        if (!includeSecrets) {
            results.forEach(a -> a.setSecret("********"));
        }
        return results;
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLtiAuthz updateAuthz(@PathVariable("id") Long id, @RequestBody LmsLtiAuthz lmsLtiAuthz) {
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

    @PostMapping("/")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public LmsLtiAuthz createAuthz(@RequestBody LmsLtiAuthz lmsLtiAuthz) {
        return ltiAuthorizationRepository.save(lmsLtiAuthz);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + WRITE_SCOPE + "')")
    public String deleteAuthz(@PathVariable("id") Long id) {
        ltiAuthorizationRepository.deleteById(id);
        return "Delete success.";
    }
}
