package edu.iu.uits.lms.iuonly.services.rest;

/*-
 * #%L
 * lms-canvas-iu-custom-services
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

import edu.iu.uits.lms.canvas.model.Account;
import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.AccountService;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.iuonly.services.SisServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;

@Profile(IUCUSTOMREST_PROFILE)
@RestController
@RequestMapping({"/rest/iu/honorlock"})
@Tag(name = "HonorLockRestController", description = "Endpoint for HonorLock")
public class HonorLockRestController {

   @Autowired
   private SisServiceImpl sisService;

   @Autowired
   private CourseService courseService;

   @Autowired
   private FeatureAccessServiceImpl featureAccessService;

   @Autowired
   private AccountService accountService;

   /**
    * NOTE: CrossOrigin annotation needs to be on this method and not the class in order for it to be properly handled
    * by the api-portal
    * @param canvasCourseId
    * @return
    */
   @GetMapping(value = "/eligible/{canvasCourseId}")
   @Operation(summary = "Check if a course is eligible to enable the HonorLock tool")
   @CrossOrigin(origins = {"${lms.js.cors.origin}"})
   public ResponseEntity<HonorLockEligible> checkEligible(@PathVariable(name = "canvasCourseId") String canvasCourseId) {
      boolean isEligible = false;
      Course course = courseService.getCourse(canvasCourseId);
      if (course != null) {
         String sisCourseId = course.getSisCourseId();
         boolean isSisCourseEligible = sisService.isHonorLockEligible(sisCourseId);
         boolean isCourseOverridden = false;

         // Only check if we don't already know the course is eligible via SIS
         if (!isSisCourseEligible) {
             // Typically the featureAccessService uses account ids, but we can also use course ids for more granular control
             List<Account> parentAccounts = accountService.getParentAccounts(course.getAccountId());
             List<String> parentAccountIds = new ArrayList<>(parentAccounts.stream().map(Account::getId).toList());
             parentAccountIds.add(course.getAccountId());
             isCourseOverridden = featureAccessService.isFeatureEnabledForAccount("honorlock.override", course.getId(), parentAccountIds);
         }
         isEligible = isSisCourseEligible || isCourseOverridden;
      }
      return ResponseEntity.ok().body(new HonorLockEligible(isEligible));
   }

   @Data
   @AllArgsConstructor
   public static class HonorLockEligible implements Serializable {
      private boolean honorLockEligible;
   }

}
