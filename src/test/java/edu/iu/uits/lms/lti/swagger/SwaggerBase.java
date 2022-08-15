package edu.iu.uits.lms.lti.swagger;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.HealthContributorAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.health.HealthEndpointAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.mail.MailHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static edu.iu.uits.lms.lti.LTIConstants.LTI_GROUP_CODE_PATH;
import static org.springdoc.core.Constants.DEFAULT_API_DOCS_URL;
import static org.springdoc.core.Constants.DEFAULT_SWAGGER_UI_PATH;

@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {HealthContributorAutoConfiguration.class, HealthEndpointAutoConfiguration.class,
      MailHealthContributorAutoConfiguration.class})
@Slf4j
public abstract class SwaggerBase {

   @Autowired
   protected MockMvc mvc;

   @Value("${springdoc.swagger-ui.path:#{null}}")
   protected String swagPath;

   @Value("${springdoc.api-docs.path:#{null}}")
   protected String apiPath;

   @Value("${springdoc.swagger-ui.enabled:#{null}}")
   protected String swagEnabled;

   @Value("${springdoc.api-docs.enabled:#{null}}")
   protected String apiEnabled;

   @BeforeEach
   void setUp() {
      logStuff();
   }

   protected void logStuff() {
      log.info("Swagger enabled: {}", swagEnabled);
      log.info("Swagger path: {}", swagPath);
      log.info("Api path enabled: {}", apiEnabled);
      log.info("Api path: {}", apiPath);
   }

   /**
    * Base path to the swagger docs
    * @return
    */
   protected String getBasePath() {
      return "/api" + getExtraPath();
   }

   /**
    * Extra customizable path segment to the swagger docs
    * @return
    */
   public String getExtraPath() {
      return "";
   }

   /**
    * Path to the swagger-ui.html page
    * @return
    */
   protected String getCustomSwaggerUiPath() {
      return getBasePath() + DEFAULT_SWAGGER_UI_PATH;
   }

   /**
    * Path to the api-docs
    * @return
    */
   protected String getCustomApiPath() {
      return getBasePath() + DEFAULT_API_DOCS_URL;
   }

   /**
    * Get the list of embedded tool paths to check
    * @return Default list contains the path to the lti-framework
    */
   protected List<String> getEmbeddedSwaggerToolPaths() {
      return Collections.singletonList(LTI_GROUP_CODE_PATH);
   }

}
