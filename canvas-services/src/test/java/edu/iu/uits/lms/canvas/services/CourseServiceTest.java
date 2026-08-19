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
import edu.iu.uits.lms.canvas.model.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests focused on the RestTemplate-parameterized overloads of {@link CourseService#getRosterForCourseAsUser}
 * (and, transitively, {@link SpringBaseService#doGet(URI, Class, RestTemplate)}), to guard against a regression
 * where a caller-supplied RestTemplate (e.g. a stand-in for CanvasRestTemplateAsUser) is silently dropped in
 * favor of the shared admin RestTemplate, especially across pagination.
 */
@SpringBootTest(classes = {CourseService.class})
public class CourseServiceTest {

    @Autowired
    private CourseService courseService;

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

    private User createUser(String id) {
        User user = new User();
        user.setId(id);
        return user;
    }

    @Test
    void testGetRosterForCourseAsUser_defaultOverload_usesAdminRestTemplate() {
        ResponseEntity<User[]> response = new ResponseEntity<>(new User[]{createUser("1")}, new HttpHeaders(), HttpStatus.OK);
        when(restTemplate.getForEntity(any(URI.class), eq(User[].class))).thenReturn(response);

        List<User> result = courseService.getRosterForCourseAsUser("123", null, null);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("1", result.get(0).getId());

        verify(restTemplate).getForEntity(any(URI.class), eq(User[].class));
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }

    @Test
    void testGetRosterForCourseAsUser_withSuppliedRestTemplate_usesSuppliedNotAdmin() {
        // Distinct mock, standing in for a future CanvasRestTemplateAsUser bean. This is intentionally
        // NOT one of the Spring-wired RestTemplate beans above - it is passed directly into the new
        // 4-arg overload, exactly as a future caller would pass CanvasRestTemplateAsUser.
        RestTemplate asUserRestTemplate = mock(RestTemplate.class);

        ResponseEntity<User[]> response = new ResponseEntity<>(new User[]{createUser("42")}, new HttpHeaders(), HttpStatus.OK);
        when(asUserRestTemplate.getForEntity(any(URI.class), eq(User[].class))).thenReturn(response);

        List<User> result = courseService.getRosterForCourseAsUser("123", "sis_login_id:asuser", List.of("active"), asUserRestTemplate);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("42", result.get(0).getId());

        // The call must have gone through the supplied RestTemplate...
        verify(asUserRestTemplate).getForEntity(any(URI.class), eq(User[].class));
        // ...and NEVER through the shared admin RestTemplate (or any other wired RestTemplate bean).
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }

    @Test
    void testGetRosterForCourseAsUser_pagination_carriesSuppliedRestTemplateAcrossPages() {
        RestTemplate asUserRestTemplate = mock(RestTemplate.class);

        // Explicitly stub the admin RestTemplate to blow up with a purpose-built message if it's ever
        // invoked, so a regression that reintroduces a fall-back to the admin token during pagination
        // fails with a clear assertion here instead of an incidental NPE from an unstubbed mock.
        when(restTemplate.getForEntity(any(URI.class), eq(User[].class)))
                .thenThrow(new AssertionError("admin RestTemplate should never be called during a per-user paginated request"));

        String nextUrl = "https://canvas.example.edu/api/v1/courses/123/users?page=2";

        HttpHeaders page1Headers = new HttpHeaders();
        // RFC 5988 format expected by LinkHeaderParser/JerseyLink: <url>; rel="next"
        page1Headers.add(HttpHeaders.LINK, "<" + nextUrl + ">; rel=\"next\"");
        ResponseEntity<User[]> page1Response = new ResponseEntity<>(new User[]{createUser("1")}, page1Headers, HttpStatus.OK);

        ResponseEntity<User[]> page2Response = new ResponseEntity<>(new User[]{createUser("2")}, new HttpHeaders(), HttpStatus.OK);

        // First call (page 1) returns a Link header pointing at page 2; second call (page 2, followed
        // recursively by doGet) returns no further Link header, ending pagination.
        when(asUserRestTemplate.getForEntity(any(URI.class), eq(User[].class)))
                .thenReturn(page1Response)
                .thenReturn(page2Response);

        List<User> result = courseService.getRosterForCourseAsUser("123", null, null, asUserRestTemplate);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.stream().anyMatch(u -> "1".equals(u.getId())));
        Assertions.assertTrue(result.stream().anyMatch(u -> "2".equals(u.getId())));

        // Both the initial call AND the recursive pagination call must have used the supplied
        // RestTemplate - if the recursive call in doGet silently fell back to the admin RestTemplate,
        // this would only see 1 invocation here (and the admin mock would see the second one instead).
        verify(asUserRestTemplate, times(2)).getForEntity(any(URI.class), eq(User[].class));
        verifyNoInteractions(restTemplate);
        verifyNoInteractions(restTemplateNoBuffer);
        verifyNoInteractions(restTemplateHttpComponent);
    }
}
