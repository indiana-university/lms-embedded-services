package edu.iu.uits.lms.email.rest;

/*-
 * #%L
 * lms-email-service
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

import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.service.EmailService;
import edu.iu.uits.lms.email.service.LmsEmailTooBigException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.mail.MessagingException;

import static edu.iu.uits.lms.email.EmailConstants.EMAILREST_PROFILE;
import static edu.iu.uits.lms.email.EmailConstants.SEND_SCOPE;

@Profile(EMAILREST_PROFILE)
@RestController
@RequestMapping("/rest/email")
@Tag(name = "EmailRestController", description = "Email sending operations")
@Slf4j
public class EmailRestController {

   @Autowired
   private EmailService emailService;

   @PostMapping("/sendViaSecondary")
   @PreAuthorize("hasAuthority('" + SEND_SCOPE + "')")
   @Operation(summary = "Send an unsigned email via the secondary mechanism")
   public void sendUnsignedEmail(@RequestBody EmailDetails emailDetails,
                                 @RequestParam(name = "unsignedToEmailToUseInPreProd", required = false, defaultValue = "") String unsignedToEmailToUseInPreProd) throws LmsEmailTooBigException, MessagingException {


      emailService.sendEmail(emailDetails, false, unsignedToEmailToUseInPreProd, EmailService.SENDING_METHOD.SECONDARY);
   }

   @PostMapping("/send")
   @PreAuthorize("hasAuthority('" + SEND_SCOPE + "')")
   @Operation(summary = "Send an email")
   public void sendEmail(@RequestBody EmailDetails emailDetails,
                         @RequestParam(name = "digitallySign", required = false, defaultValue = "true") boolean digitallySign,
                         @RequestParam(name = "unsignedToEmailToUseInPreProd", required = false, defaultValue = "") String unsignedToEmailToUseInPreProd) throws LmsEmailTooBigException, MessagingException {

      emailService.sendEmail(emailDetails, digitallySign, unsignedToEmailToUseInPreProd, EmailService.SENDING_METHOD.PRIMARY);
   }

}
