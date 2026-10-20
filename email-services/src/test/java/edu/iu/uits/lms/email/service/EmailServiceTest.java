package edu.iu.uits.lms.email.service;

/*-
 * #%L
 * lms-email-service
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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
import jakarta.mail.BodyPart;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for the CodeQL SSRF finding on {@link EmailService#sendEmail}: attaching a
 * {@link jakarta.activation.URLDataSource} built directly from a caller-supplied URL.
 */
@SpringBootTest(classes = {EmailService.class})
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private EmailServiceConfig emailServiceConfig;

    private MimeMessage mimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    private EmailDetails baseDetails(List<EmailServiceAttachment> attachments) {
        EmailDetails details = new EmailDetails();
        details.setRecipients(new String[] {"someone@example.edu"});
        details.setSubject("Test Subject");
        details.setBody("Test Body");
        details.setFrom("noreply@example.edu");
        details.setEmailServiceAttachmentList(attachments);
        return details;
    }

    // --- isAllowedAttachmentUrl() unit coverage --------------------------------------------

    @Test
    void publiclyRoutableHttpAndHttpsUrlsAreAllowed() throws Exception {
        // Literal IPs so this doesn't depend on real DNS resolution in CI.
        assertTrue(emailService.isAllowedAttachmentUrl(new URI("http://8.8.8.8/report.csv").toURL()));
        assertTrue(emailService.isAllowedAttachmentUrl(new URI("https://8.8.8.8/report.csv").toURL()));
    }

    @Test
    void loopbackUrlIsRejected() throws Exception {
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("http://127.0.0.1/admin").toURL()));
    }

    @Test
    void linkLocalUrlIsRejected() throws Exception {
        // 169.254.169.254 is also the cloud-provider metadata endpoint (AWS/GCP/Azure) - the classic
        // SSRF target this check exists to block.
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("http://169.254.169.254/latest/meta-data/").toURL()));
    }

    @Test
    void privateRfc1918UrlIsRejected() throws Exception {
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("http://10.0.0.5/internal").toURL()));
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("http://192.168.1.1/internal").toURL()));
    }

    @Test
    void fileUrlInsideTempDirIsAllowed() throws Exception {
        // Mirrors ETextService's real usage: a file it just wrote under java.io.tmpdir.
        Path tempFile = Files.createTempFile("email-service-test-", ".csv");
        try {
            URL fileUrl = tempFile.toUri().toURL();
            assertTrue(emailService.isAllowedAttachmentUrl(fileUrl));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void fileUrlOutsideTempDirIsRejected() throws Exception {
        URL outsideTempDir = new File(System.getProperty("user.home"), "not-a-temp-file.csv").toURI().toURL();
        assertFalse(emailService.isAllowedAttachmentUrl(outsideTempDir));
    }

    @Test
    void unrecognizedSchemeIsRejected() throws Exception {
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("ftp://example.edu/report.csv").toURL()));
        assertFalse(emailService.isAllowedAttachmentUrl(new URI("jar:file:/tmp/x.jar!/report.csv").toURL()));
    }

    // --- sendEmail() integration coverage --------------------------------------------------

    @Test
    void sendEmail_allowedTempFileAttachment_isIncludedInMessage() throws Exception {
        when(emailServiceConfig.isEnabled()).thenReturn(true);
        when(emailServiceConfig.getEnv()).thenReturn("prd");

        byte[] fileContent = "hello,world\n".getBytes(StandardCharsets.UTF_8);
        Path tempFile = Files.createTempFile("email-service-test-", ".csv");
        Files.write(tempFile, fileContent);

        MimeMessage message = mimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        try {
            EmailServiceAttachment attachment = new EmailServiceAttachment();
            attachment.setFilename("data.csv");
            attachment.setUrl(tempFile.toUri().toURL());

            emailService.sendEmail(baseDetails(List.of(attachment)));

            BodyPart matchingPart = findPartByFilename(message, "data.csv");
            assertTrue(matchingPart != null, "expected attachment 'data.csv' to be present on the sent message");
            assertArrayEquals(fileContent, matchingPart.getInputStream().readAllBytes());
            verify(javaMailSender, times(1)).send(message);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void sendEmail_disallowedAttachment_isSkippedButEmailStillSends() throws Exception {
        when(emailServiceConfig.isEnabled()).thenReturn(true);
        when(emailServiceConfig.getEnv()).thenReturn("prd");

        MimeMessage message = mimeMessage();
        when(javaMailSender.createMimeMessage()).thenReturn(message);

        EmailServiceAttachment attachment = new EmailServiceAttachment();
        attachment.setFilename("passwd");
        attachment.setUrl(new File(System.getProperty("user.home"), "not-a-temp-file.csv").toURI().toURL());

        emailService.sendEmail(baseDetails(List.of(attachment)));

        assertFalse(findPartByFilename(message, "passwd") != null,
                "disallowed attachment must not be present on the sent message");
        verify(javaMailSender, times(1)).send(message);
    }

    private static BodyPart findPartByFilename(MimeMessage message, String filename) throws Exception {
        Object content = message.getContent();
        if (!(content instanceof Multipart multipart)) {
            return null;
        }
        for (int i = 0; i < multipart.getCount(); i++) {
            BodyPart part = multipart.getBodyPart(i);
            if (filename.equals(part.getFileName())) {
                return part;
            }
        }
        return null;
    }
}
