package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.newquizzes.Quiz;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.net.URI;
import java.util.List;

@Service
@Slf4j
public class NewQuizzesService extends SpringBaseService {
    ///api/quiz/v1/courses/:course_id/quizzes
    // Note that this api url is different from most other api urls.  New quizzes is "special"
    private static final String COURSES_BASE_URI = "{url}/api/quiz/v1/courses";
    private static final String COURSE_QUIZZES_URI = COURSES_BASE_URI + "/{id}/quizzes";
    private static final String SINGLE_QUIZ_URI = COURSE_QUIZZES_URI + "/{id}";

    private final UriTemplate COURSE_QUIZZES_TEMPLATE = new UriTemplate(COURSE_QUIZZES_URI);
    private final UriTemplate SINGLE_QUIZ_TEMPLATE = new UriTemplate(SINGLE_QUIZ_URI);

    /**
     * Get all quizzes in a particular course, that the given user has access to see.
     *
     * @param courseId The ID of the course.
     * @param asUser Masquerade as this user when getting the quizzes. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @return A list of quizzes in the course.
     */
    public List<Quiz> getQuizzesInCourse(String courseId, String asUser) {
        URI uri = COURSE_QUIZZES_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);
        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Quiz[].class);
    }

    /**
     * Get a specific quiz from a course, as seen by the given user.
     *
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param asUser Masquerade as this user when getting the quiz. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @return The quiz object, or null if not found.
     */
    public Quiz getSingleQuizFromCourse(String courseId, String quizId, String asUser) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            ResponseEntity<Quiz> quizResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), Quiz.class);
            log.debug("quizResponseEntity: {}", quizResponseEntity);

            return quizResponseEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    /**
     * Update the instructions of a quiz in a course, as seen by the given user.
     *
     * @param courseId The ID of the course.
     * @param quizId The ID of the quiz.
     * @param asUser Masquerade as this user when updating the quiz. If you wish to use an sis_login_id,
     *               prefix your asUser with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (i.e., sis_login_id:octest1)
     * @param instructions The new instructions for the quiz.
     * @return The updated quiz object, or null if the update failed.
     */
    public Quiz updateQuizInstructions(String courseId, String quizId, String asUser, String instructions) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("quiz[instructions]", instructions);

            HttpEntity<MultiValueMap<String, String>> updateRequest = new HttpEntity<>(multiValueMap, headers);
            ResponseEntity<Quiz> quizResponseEntity = this.restTemplateHttpComponent.exchange(builder.build().toUri(), HttpMethod.PATCH, updateRequest, Quiz.class);
            log.debug("quizResponseEntity: {}", quizResponseEntity);

            if (quizResponseEntity != null) {
                return quizResponseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }
}
