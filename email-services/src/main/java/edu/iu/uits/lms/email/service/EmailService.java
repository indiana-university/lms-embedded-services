package edu.iu.uits.lms.email.service;

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

import edu.iu.uits.lms.email.config.EmailServiceConfig;
import edu.iu.uits.lms.email.model.EmailDetails;
import edu.iu.uits.lms.email.model.EmailServiceAttachment;
import edu.iu.uits.lms.email.model.Priority;
import jakarta.activation.URLDataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class EmailService {

   private static final int SUBJECT_MAX_LENGTH = 500;

   /**
    * this should be roughly the equivalent of 5 MB
    */
   private static final int BODY_MAX_LENGTH = 5242880;

   @Autowired
   private JavaMailSender javaMailSender;

   @Autowired
   private EmailServiceConfig emailServiceConfig;

   /**
    * {@link EmailServiceAttachment#getUrl()} is fetched directly via {@link URLDataSource}, so a
    * caller-supplied URL of an unexpected scheme (or a {@code file:} URL outside our own temp
    * directory) could otherwise be used to attach arbitrary local files or reach arbitrary hosts.
    * {@code EmailRestController#sendEmail} accepts an {@link EmailDetails} request body directly
    * from any REST caller holding the {@code send} scope, so the attachment URL is genuinely
    * attacker-influenceable, not just internally generated - an {@code http(s)} URL must also be
    * checked to make sure it doesn't resolve to a private/loopback/link-local address (which would
    * let a caller reach internal-only services or a cloud metadata endpoint). The one internal
    * caller (a {@code file:} URL to a CSV it just wrote to a temp directory) is unaffected.
    *
    * @param url the attachment URL to check
    * @return true if the URL is safe to fetch via {@link URLDataSource}
    */
   boolean isAllowedAttachmentUrl(URL url) {
      String protocol = url.getProtocol().toLowerCase(Locale.ROOT);
      if (protocol.equals("http") || protocol.equals("https")) {
         return isPubliclyRoutable(url.getHost());
      }

      if (protocol.equals("file")) {
         try {
            // toRealPath() (rather than toAbsolutePath().normalize()) resolves symlinks on both
            // sides of the comparison, so a symlinked temp dir (e.g. macOS's /tmp -> /private/tmp)
            // can't cause a legitimate attachment to be rejected due to alias mismatch.
            Path attachmentPath = Paths.get(url.toURI()).toRealPath();
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).toRealPath();
            return attachmentPath.startsWith(tempDir);
         } catch (URISyntaxException | IllegalArgumentException | IOException e) {
            return false;
         }
      }

      return false;
   }

   /**
    * Resolves every address the given host name resolves to and rejects loopback, link-local
    * (this also covers the {@code 169.254.169.254} cloud-metadata address), site-local/private,
    * wildcard, and multicast addresses. This doesn't defend against DNS rebinding (a host that
    * resolves to a public address at check time but a private one at connect time) - {@link
    * URLDataSource}/{@link java.net.URL#openConnection()} re-resolve the host themselves, so there's
    * no practical way to pin the checked address here without replacing that connection mechanism.
    */
   private boolean isPubliclyRoutable(String host) {
      try {
         for (InetAddress address : InetAddress.getAllByName(host)) {
            if (address.isLoopbackAddress() || address.isLinkLocalAddress() || address.isSiteLocalAddress()
                    || address.isAnyLocalAddress() || address.isMulticastAddress()) {
               return false;
            }
         }
         return true;
      } catch (UnknownHostException e) {
         return false;
      }
   }

   public String getStandardHeader() {
      return "[LMS " + emailServiceConfig.getEnv().toUpperCase() + " Notifications]";
   }

   public void sendEmail(EmailDetails emailDetails) throws LmsEmailTooBigException, MessagingException {
      sendEmail(emailDetails, null);
   }

   public void sendEmail(EmailDetails emailDetails, String emailToUseInPreProd) throws LmsEmailTooBigException, MessagingException {
      String subject = emailDetails.getSubject();
      String body = emailDetails.getBody();
      String[] recipients = emailDetails.getRecipients();
      List<EmailServiceAttachment> emailServiceAttachmentList = emailDetails.getEmailServiceAttachmentList();
      boolean enableHtml = emailDetails.isEnableHtml();
      Priority priority = emailDetails.getPriority();
      String from = emailDetails.getFrom();

      if (from == null) {
         from = emailServiceConfig.getDefaultFrom();
      }

      if (priority == null) {
         priority = Priority.NORMAL;
      }

      if (!emailServiceConfig.isEnabled()) {
         log.info("mail.enabled is false. Logging message\nrecipients: "
                 + StringUtils.arrayToCommaDelimitedString(recipients)
                 + "\nSubject: " + subject + "\nBody:\n" + body + "\n");
         return;
      }

      if (subject != null && subject.length() > SUBJECT_MAX_LENGTH) {
         subject = subject.substring(0, SUBJECT_MAX_LENGTH);
      }
      if (body != null && body.length() > BODY_MAX_LENGTH) {
         body = body.substring(0, BODY_MAX_LENGTH);
         body += "\nThe message body exceeded " + BODY_MAX_LENGTH + " characters and this message was truncated!";
      }

      log.warn("Sending unsigned email");

      if (! "prd".equals(emailServiceConfig.getEnv())) {
         String htmlNewLineString = enableHtml ? "<br />" : "";

         String preBody = "** In production, this message will go to " + htmlNewLineString + "\r\n";

         for (String recipient : recipients) {
            preBody += String.format(" - TO: %s "  + htmlNewLineString + "\r\n", recipient);
         }

         body = preBody + "\r\n" + body;

         recipients = new String[] {   emailToUseInPreProd != null && !emailToUseInPreProd.trim().isEmpty()
                 ? emailToUseInPreProd
                 : emailServiceConfig.getDefaultUnsignedTo() };
      }

      MimeMessage message = javaMailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true);

      for(String email : recipients) {
         helper.addTo(email);
      }

      helper.setFrom(from);

      helper.setText(body, enableHtml);
      helper.setSubject(subject);
      helper.setPriority(priority.getPriority());


      if (emailServiceAttachmentList != null) {
         for (EmailServiceAttachment emailServiceAttachment : emailServiceAttachmentList) {
            if (emailServiceAttachment.getFilename() != null && emailServiceAttachment.getUrl() != null) {
               URL attachmentUrl = emailServiceAttachment.getUrl();
               if (!isAllowedAttachmentUrl(attachmentUrl)) {
                  log.warn("Refusing to attach '{}' - disallowed attachment URL: {}",
                          emailServiceAttachment.getFilename(), attachmentUrl);
                  continue;
               }
               URLDataSource attachmentDataSource = new URLDataSource(attachmentUrl);
               helper.addAttachment(emailServiceAttachment.getFilename(), attachmentDataSource);
            }
         }
      }

      if (message.getSize() > BODY_MAX_LENGTH) {
         throw new LmsEmailTooBigException();
      }

      javaMailSender.send(message);

   }
}
