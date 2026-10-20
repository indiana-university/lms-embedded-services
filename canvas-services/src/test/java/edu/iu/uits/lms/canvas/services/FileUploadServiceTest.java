package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
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

import edu.iu.uits.lms.canvas.config.CanvasConfiguration;
import edu.iu.uits.lms.canvas.model.CanvasFile;
import edu.iu.uits.lms.canvas.model.CanvasFileUploadResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Regression coverage for the two CodeQL findings fixed in {@link FileUploadService}:
 * path injection via {@code MultipartFile#getOriginalFilename()}, and SSRF via the
 * Canvas-supplied {@code upload_url} / {@code Location} redirect.
 */
@SpringBootTest(classes = {FileUploadService.class})
class FileUploadServiceTest {

    private static final String BASE_API_URL = "https://canvas.example.edu/api/v1";
    private static final URI INITIATE_URI = URI.create(BASE_API_URL + "/courses/123/files");
    private static final String TRAVERSAL_TARGET = new File(System.getProperty("java.io.tmpdir"),
            "fileuploadservicetest-should-not-be-created.txt").getAbsolutePath();

    @Autowired
    private FileUploadService fileUploadService;

    @MockitoBean
    @Qualifier("CanvasRestTemplate")
    protected RestTemplate restTemplate;

    @MockitoBean
    @Qualifier("restTemplateNoBuffer")
    protected RestTemplate restTemplateNoBuffer;

    @MockitoBean
    @Qualifier("RestTemplateHttpComponent")
    protected RestTemplate restTemplateHttpComponent;

    @MockitoBean
    protected CanvasConfiguration canvasConfiguration;

    @AfterEach
    void cleanup() {
        new File(TRAVERSAL_TARGET).delete();
    }

    private void stubTrustedHost(List<String> trustedUploadHosts) {
        when(canvasConfiguration.getBaseApiUrl()).thenReturn(BASE_API_URL);
        when(canvasConfiguration.getHost()).thenReturn("canvas.example.edu");
        when(canvasConfiguration.getTrustedUploadHosts()).thenReturn(trustedUploadHosts);
    }

    private void stubInitiateFileUpload(String uploadUrl) {
        CanvasFileUploadResponse response = new CanvasFileUploadResponse();
        response.setUploadUrl(uploadUrl);
        response.setUploadParams(Map.of("key", "abc"));
        when(restTemplate.postForEntity(eq(INITIATE_URI), any(HttpEntity.class), eq(CanvasFileUploadResponse.class)))
                .thenReturn(ResponseEntity.ok(response));
    }

    @Test
    void uploadCourseFile_happyPath_writesToTempFileAndSucceeds() throws Exception {
        stubTrustedHost(List.of());
        stubInitiateFileUpload("https://canvas.example.edu/upload-target");

        CanvasFile expectedFile = new CanvasFile();
        expectedFile.setId("999");
        expectedFile.setFilename("safe.txt");
        when(restTemplateNoBuffer.postForEntity(eq("https://canvas.example.edu/upload-target"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(expectedFile));

        // The originalFilename is entirely attacker-controlled (multipart Content-Disposition) and is
        // set here to the exact absolute path we're asserting never gets written to.
        MultipartFile multipartFile = new MockMultipartFile("file", TRAVERSAL_TARGET, "text/plain", "hello".getBytes());

        CanvasFile result = fileUploadService.uploadCourseFile("123", "safe.txt", 5L, "text/plain", "/", multipartFile, false);

        assertNotNull(result);
        assertEquals("999", result.getId());
        assertFalse(new File(TRAVERSAL_TARGET).exists(),
                "upload must never write to the path named by the client-supplied original filename");
    }

    @Test
    void uploadCourseFile_untrustedUploadUrl_isRejectedBeforeSendingTheFile() {
        stubTrustedHost(List.of());
        stubInitiateFileUpload("https://evil.attacker.example/steal");

        MultipartFile multipartFile = new MockMultipartFile("file", "safe.txt", "text/plain", "hello".getBytes());

        // validateTrustedHost() runs before uploadFileToCanvas's try/catch, so this one surfaces
        // unwrapped (unlike the redirect-Location case below, which fails from inside the try block).
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                fileUploadService.uploadCourseFile("123", "safe.txt", 5L, "text/plain", "/", multipartFile, false));

        assertInstanceOf(SecurityException.class, ex);
        // The bearer-token-bearing RestTemplate must never be pointed at the untrusted host.
        verify(restTemplateNoBuffer, never()).postForEntity(anyString(), any(), eq(Object.class));
    }

    @Test
    void uploadCourseFile_untrustedRedirectLocation_isRejected() {
        stubTrustedHost(List.of());
        stubInitiateFileUpload("https://canvas.example.edu/upload-target");

        HttpHeaders redirectHeaders = new HttpHeaders();
        redirectHeaders.add("Location", "https://evil.attacker.example/verify");
        ResponseEntity<Object> redirectResponse = new ResponseEntity<>(null, redirectHeaders, HttpStatus.CREATED);
        when(restTemplateNoBuffer.postForEntity(eq("https://canvas.example.edu/upload-target"), any(), eq(Object.class)))
                .thenReturn(redirectResponse);

        MultipartFile multipartFile = new MockMultipartFile("file", "safe.txt", "text/plain", "hello".getBytes());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                fileUploadService.uploadCourseFile("123", "safe.txt", 5L, "text/plain", "/", multipartFile, false));

        assertInstanceOf(SecurityException.class, ex.getCause());
        verify(restTemplate, never()).getForEntity(anyString(), eq(CanvasFile.class));
    }

    @Test
    void uploadCourseFile_configuredTrustedUploadHost_isAllowed() {
        stubTrustedHost(List.of("storage.trusted.example"));
        stubInitiateFileUpload("https://storage.trusted.example/upload-target");

        CanvasFile expectedFile = new CanvasFile();
        expectedFile.setId("1000");
        when(restTemplateNoBuffer.postForEntity(eq("https://storage.trusted.example/upload-target"), any(), eq(Object.class)))
                .thenReturn(ResponseEntity.ok(expectedFile));

        MultipartFile multipartFile = new MockMultipartFile("file", "safe.txt", "text/plain", "hello".getBytes());

        CanvasFile result = fileUploadService.uploadCourseFile("123", "safe.txt", 5L, "text/plain", "/", multipartFile, false);

        assertNotNull(result);
        assertEquals("1000", result.getId());
    }
}
