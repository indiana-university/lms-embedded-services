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
import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.UserCustomDataRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests focused on the RestTemplate-parameterized overloads of the custom-data methods, to guard
 * against a regression where a caller-supplied RestTemplate (e.g. a stand-in for
 * CanvasRestTemplateAsUser) is silently dropped in favor of the shared admin RestTemplate.
 */
@SpringBootTest(classes = {UserService.class})
public class UserServiceTest {

    @Autowired
    private UserService userService;

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

    @BeforeEach
    void setup() {
        when(canvasConfiguration.getBaseApiUrl()).thenReturn("https://canvas.example.edu/api/v1");
    }

    private UserCustomDataRequest customDataRequest() {
        UserCustomDataRequest request = new UserCustomDataRequest();
        request.setUserId("sis_login_id:testuser");
        request.setField(CanvasConstants.API_FIELD_SIS_LOGIN_ID);
        request.setPathParts(Collections.singletonList("hidden_courses"));
        return request;
    }

    @Test
    void testGetUserCustomData_withSuppliedRestTemplate_usesSuppliedNotAdmin() {
        RestTemplate asUserRestTemplate = mock(RestTemplate.class);
        ResponseEntity<Object> response = new ResponseEntity<>(Collections.singletonMap("data", Collections.emptyMap()), new HttpHeaders(), HttpStatus.OK);
        when(asUserRestTemplate.getForEntity(any(URI.class), eq(Object.class))).thenReturn(response);

        Object result = userService.getUserCustomData(customDataRequest(), asUserRestTemplate);

        Assertions.assertNotNull(result);
        verify(asUserRestTemplate).getForEntity(any(URI.class), eq(Object.class));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }

    @Test
    void testSetUserCustomData_withSuppliedRestTemplate_usesSuppliedNotAdmin() {
        RestTemplate asUserRestTemplate = mock(RestTemplate.class);
        ResponseEntity<Object> response = new ResponseEntity<>("ok", new HttpHeaders(), HttpStatus.OK);
        when(asUserRestTemplate.exchange(any(URI.class), eq(HttpMethod.PUT), any(), eq(Object.class))).thenReturn(response);

        UserCustomDataRequest request = customDataRequest();
        request.setData("123");
        Object result = userService.setUserCustomData(request, asUserRestTemplate);

        Assertions.assertNotNull(result);
        verify(asUserRestTemplate).exchange(any(URI.class), eq(HttpMethod.PUT), any(), eq(Object.class));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }

    @Test
    void testDeleteUserCustomData_withSuppliedRestTemplate_usesSuppliedNotAdmin() {
        RestTemplate asUserRestTemplate = mock(RestTemplate.class);
        ResponseEntity<Object> response = new ResponseEntity<>("ok", new HttpHeaders(), HttpStatus.OK);
        // deleteUserCustomData sends a real form-encoded "ns" body on the DELETE request (unlike
        // CourseService.removeCourseAsFavorite, which sends null) - match any() body, not eq(null).
        when(asUserRestTemplate.exchange(any(URI.class), eq(HttpMethod.DELETE), any(), eq(Object.class))).thenReturn(response);

        Object result = userService.deleteUserCustomData(customDataRequest(), asUserRestTemplate);

        Assertions.assertNotNull(result);
        verify(asUserRestTemplate).exchange(any(URI.class), eq(HttpMethod.DELETE), any(), eq(Object.class));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }
}
