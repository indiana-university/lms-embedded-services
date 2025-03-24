package edu.iu.uits.lms.canvas.services;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import edu.iu.uits.lms.canvas.model.classicquizzes.Quiz;
import edu.iu.uits.lms.canvas.model.classicquizzes.QuizSubmission;
import edu.iu.uits.lms.canvas.model.classicquizzes.QuizSubmissionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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
public class ClassicQuizzesService extends SpringBaseService {
    private static final String COURSES_BASE_URI = "{url}/courses";
    private static final String COURSE_QUIZZES_URI = COURSES_BASE_URI + "/{id}/quizzes";
    private static final String SINGLE_QUIZ_URI = COURSE_QUIZZES_URI + "/{id}";
    private static final String QUIZ_SUBMISSION_URI = SINGLE_QUIZ_URI + "/submission";

    private final UriTemplate COURSE_QUIZZES_TEMPLATE = new UriTemplate(COURSE_QUIZZES_URI);
    private final UriTemplate SINGLE_QUIZ_TEMPLATE = new UriTemplate(SINGLE_QUIZ_URI);
    private final UriTemplate QUIZ_SUBMISSION_TEMPLATE = new UriTemplate(QUIZ_SUBMISSION_URI);

    /**
     * Get all quizzes in a particular course, that the given user has access to see
     * @param courseId
     * @param asUser
     * @return
     */
    public List<Quiz> getQuizzesInCourse(String courseId, String asUser) {
        URI uri = COURSE_QUIZZES_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);
        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Quiz[].class);
    }

    /**
     * Get a specific quiz form a course, as seen by the given user
     * @param courseId
     * @param quizId
     * @param asUser
     * @return
     */
    public Quiz getSingleQuizFromCourse(String courseId, String quizId, String asUser) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpEntity<Quiz> quizResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), Quiz.class);
            log.debug("quizResponseEntity: {}", quizResponseEntity);

            if (quizResponseEntity != null) {
                return quizResponseEntity.getBody();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    /**
     * Get all quiz submissions from a quiz in a course, as seen by the given user
     * @param courseId
     * @param quizId
     * @param asUser
     * @return
     */
    public List<QuizSubmission> getQuizSubmissionFromQuiz(String courseId, String quizId, String asUser) {
        URI uri = QUIZ_SUBMISSION_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpEntity<QuizSubmissionWrapper> quizResponseEntity = this.restTemplate.getForEntity(builder.build().toUri(), QuizSubmissionWrapper.class);
            log.debug("quizResponseEntity: {}", quizResponseEntity);

            if (quizResponseEntity != null) {
                return quizResponseEntity.getBody().getQuizSubmissions();
            }
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return null;
    }

    public Quiz updateQuizDescription(String courseId, String quizId, String asUser, String description) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", asUser);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
            multiValueMap.add("quiz[description]", description);

            HttpEntity<MultiValueMap<String, String>> updateRequest = new HttpEntity<>(multiValueMap, headers);
            HttpEntity<Quiz> quizResponseEntity = this.restTemplate.exchange(builder.build().toUri(), HttpMethod.PUT, updateRequest, Quiz.class);
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
