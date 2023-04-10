package edu.iu.uits.lms.common.it12logging;

import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;

public class LmsFilterSecurityInterceptorObjectPostProcessor implements ObjectPostProcessor<FilterSecurityInterceptor> {
   @Override
    public <O extends FilterSecurityInterceptor> O postProcess(O fsi) {
        fsi.setPublishAuthorizationSuccess(true);
        return fsi;
    }
}