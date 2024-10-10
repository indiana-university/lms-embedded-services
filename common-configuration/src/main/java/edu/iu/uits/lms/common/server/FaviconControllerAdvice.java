package edu.iu.uits.lms.common.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(annotations = Controller.class)
@Slf4j
public class FaviconControllerAdvice {

    private FaviconProperties faviconProperties;

    public FaviconControllerAdvice(FaviconProperties faviconProperties) {
        this.faviconProperties = faviconProperties;
    }

    @ModelAttribute("FaviconProperties")
    public FaviconProperties getFaviconProperties() {
        return faviconProperties;
    }
}
