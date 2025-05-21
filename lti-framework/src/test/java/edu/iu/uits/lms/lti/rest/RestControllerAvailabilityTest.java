package edu.iu.uits.lms.lti.rest;

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

import edu.iu.uits.lms.lti.controller.JWKSController;
import edu.iu.uits.lms.lti.controller.rest.LtiAuthorizationRestController;
import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static edu.iu.uits.lms.lti.LTIConstants.LTIREST_PROFILE;

@ContextConfiguration
@SpringBootTest
public class RestControllerAvailabilityTest {

   private static final String PACKAGE = "edu.iu.uits.lms.lti";

   @MockitoBean
   public DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   @MockitoBean
   public LtiAuthorizationRepository ltiAuthorizationRepository;

   @Nested
   @ActiveProfiles({LTIREST_PROFILE})
   class Enabled {

      @Autowired
      private ApplicationContext appContext;

      @Test
      public void ensureBeansAreLoaded() {
         Set<Class<?>> typesAnnotatedWith = new Reflections(PACKAGE).getTypesAnnotatedWith(RestController.class);
         List<String> restControllerClassNames = typesAnnotatedWith.stream()
               .map(Class::getName)
               .filter(foo -> !foo.equals(JWKSController.class.getName()))
               .collect(Collectors.toList());

         Map<String, Object> beansWithAnnotation = appContext.getBeansWithAnnotation(RestController.class);
         List<String> beanClassNames = beansWithAnnotation.values().stream()
               .map(Object::getClass)
               .map(ClassUtils::getUserClass)
               .map(Class::getName)
               .collect(Collectors.toList());

         boolean allMatch = restControllerClassNames.stream().allMatch(beanClassNames::contains);

         Assertions.assertTrue(allMatch);
      }
   }

   @Nested
   class Disabled {
      @Autowired
      private ApplicationContext appContext;

      @Test
      public void ensureBeansNotLoaded() {
         Set<Class<?>> typesAnnotatedWith = new Reflections(PACKAGE).getTypesAnnotatedWith(RestController.class);
         List<String> restControllerClassNames = typesAnnotatedWith.stream().map(Class::getName).collect(Collectors.toList());

         Map<String, Object> beansWithAnnotation = appContext.getBeansWithAnnotation(RestController.class);
         List<String> beanClassNames = beansWithAnnotation.values().stream()
               .map(Object::getClass)
               .map(Class::getName)
               .collect(Collectors.toList());

         boolean noneMatch = beanClassNames.stream().noneMatch(foo -> restControllerClassNames.contains(foo));

         Assertions.assertTrue(noneMatch);
      }
   }

   @Configuration
   @ComponentScan(basePackageClasses = LtiAuthorizationRestController.class)
   static class LtiTestContextConfiguration {

      @MockitoBean
      private LtiAuthorizationRepository ltiAuthorizationRepository;

      @MockitoBean
      private DefaultInstructorRoleRepository defaultInstructorRoleRepository;

   }

}
