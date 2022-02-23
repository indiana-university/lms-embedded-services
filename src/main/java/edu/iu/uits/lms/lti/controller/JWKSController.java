package edu.iu.uits.lms.lti.controller;

import com.nimbusds.jose.jwk.RSAKey;
import edu.iu.uits.lms.lti.service.Lti13Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class JWKSController {

    @Autowired
    private Lti13Service lti13Service;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> keys() {
        RSAKey jks = lti13Service.getJKS();
        return jks.toJSONObject();
    }

}
