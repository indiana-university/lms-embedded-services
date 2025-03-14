package edu.iu.uits.lms.canvas.services;

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
     * Get all quizzes in a particular course, that the given user has access to see
     * @param courseId
     * @param sis_login_id
     * @return
     */
    public List<Quiz> getQuizzesInCourse(String courseId, String sis_login_id) {
        URI uri = COURSE_QUIZZES_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + sis_login_id);
        builder.queryParam("per_page", "100");

        return doGet(builder.build().toUri(), Quiz[].class);
    }

    /**
     * Get a specific quiz form a course, as seen by the given user
     * @param courseId
     * @param quizId
     * @param sis_login_id
     * @return
     */
    public Quiz getSingleQuizFromCourse(String courseId, String quizId, String sis_login_id) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + sis_login_id);

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

    public Quiz updateQuizInstructions(String courseId, String quizId, String sis_login_id, String instructions) {
        URI uri = SINGLE_QUIZ_TEMPLATE.expand(canvasConfiguration.getBaseUrl(), courseId, quizId);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
        builder.queryParam("as_user_id", "sis_login_id:" + sis_login_id);

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
