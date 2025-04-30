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

import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.CanvasFile;
import edu.iu.uits.lms.canvas.model.CanvasFileUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

@Service
@Slf4j
public class FileUploadService extends SpringBaseService {

    private static final String BASE_URI = "{url}";
    private static final String COURSES_URI = BASE_URI + "/courses/{course_id}";
    private static final String USERS_URI = BASE_URI + "/users/{user_id}";

    private static final String FILES_PATH = "/files";

    private static final UriTemplate COURSE_FILE_TEMPLATE = new UriTemplate(COURSES_URI + FILES_PATH);
    private static final UriTemplate USER_FILE_TEMPLATE = new UriTemplate(USERS_URI + FILES_PATH);

    private static final String CONVERSATIONS_FOLDER = "conversation attachments";

    /**
     *
     * @param courseId
     * @param fileName
     * @param fileSize
     * @param contentTypeString
     * @param parentFolderPath
     * @param multipartFile
     * @param overwriteDuplicate true if you want to overwrite an existing file with the same name. If false, will modify the file name automatically
     * @return upload a new file to Canvas. Returns the created {@link CanvasFile}
     */
    public CanvasFile uploadCourseFile(String courseId, String fileName, long fileSize, String contentTypeString, String parentFolderPath,
                                       MultipartFile multipartFile, boolean overwriteDuplicate) {
        URI courseFileUri = COURSE_FILE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), courseId);
        CanvasFileUploadResponse response = initiateFileUpload(courseFileUri, fileName, fileSize, contentTypeString, parentFolderPath, overwriteDuplicate, null);

        File file = null;

        if (multipartFile != null) {
            try {
                file = new File(multipartFile.getOriginalFilename());
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(multipartFile.getBytes());
                fos.close();
            } catch (IOException e) {
                log.error("Error: ", e);
                file = null;
            }
        }

        return uploadFileToCanvas(response, file);
    }

    /**
     *
     * @param userId The user sending the message. Attachments are first uploaded to the sender's Files conversation attachments folder. If you wish to use an sis_login_id,
     *        prefix your user with {@link CanvasConstants#API_FIELD_SIS_LOGIN_ID} plus a colon (ie sis_login_id:octest1)
     * @param fileName
     * @param fileSize
     * @param contentTypeString
     * @param file
     * @param overwriteDuplicate true if you want to overwrite an existing file with the same name. If false, will modify the file name automatically
     * @return upload a new file to Canvas to be used for conversations. Returns the created {@link CanvasFile}
     */
    public CanvasFile uploadConversationFile(String userId, String fileName, long fileSize,
                                             String contentTypeString, File file, boolean overwriteDuplicate) {
        URI conversationFileUri = USER_FILE_TEMPLATE.expand(canvasConfiguration.getBaseApiUrl(), userId);
        CanvasFileUploadResponse response = initiateFileUpload(conversationFileUri, fileName, fileSize, contentTypeString, CONVERSATIONS_FOLDER, overwriteDuplicate, userId);

        return uploadFileToCanvas(response, file);
    }

    /**
     *
     * @param expandedUri
     * @param fileName
     * @param fileSize
     * @param contentType
     * @param parentFolderPath
     * @param overwriteDuplicate
     * @param asUser only use if you are uploading to an individual user's files. Leave null otherwise.
     * @return The first step of a file upload we tell Canvas information about the file and are returned a {@link CanvasFileUploadResponse} that includes the location where
     * we should upload the actual file.
     */
    private CanvasFileUploadResponse initiateFileUpload(URI expandedUri, String fileName, long fileSize, String contentType, String parentFolderPath, boolean overwriteDuplicate, String asUser) {

        String onDuplicate = overwriteDuplicate ? "overwrite" : "rename";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        UriComponentsBuilder builder = UriComponentsBuilder.fromUri(expandedUri);

        // If we include these as query params, we will hit issues with special and reserved characters (like ;) in the subject and body.
        // This may be reworked when we upgrade to Spring 5, but using the request entity avoids these issues now
        MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
        map.add("name", fileName);
        map.add("size", String.valueOf(fileSize));
        map.add("content_type", contentType);
        map.add("parent_folder_path", parentFolderPath);
        map.add("on_duplicate", onDuplicate);

        if (asUser != null) {
            map.add("as_user_id", asUser);
        }

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(map, headers);

        CanvasFileUploadResponse uploadResponse = null;

        try {
            ResponseEntity<CanvasFileUploadResponse> response = restTemplate.postForEntity(builder.build().toUri(), requestEntity, CanvasFileUploadResponse.class);
            uploadResponse = response.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Unable to initiate file upload.", hcee);
            throw new RuntimeException("Unable to initiate file upload to Canvas.");
        }

        return uploadResponse;

    }

    /**
     *
     * @param fileUploadResponse
     * @param file
     * @throws RuntimeException if file upload to Canvas fails
     * @return This is the second step of the upload process that uploads the actual file content given the {@link CanvasFileUploadResponse}.
     * Use {@link #uploadCourseFile(String, String, long, String, String, MultipartFile, boolean)} to do the complete upload.
     */
    private CanvasFile uploadFileToCanvas(CanvasFileUploadResponse fileUploadResponse, File file) {

        /* The file upload process via the Canvas API has 3 steps:
           1) Tell Canvas about the upload and get a token. This phase will return a CanvasFileUploadResponse object
              that includes an upload URL and upload params that will be variable. This object is temporary for 30 min.
           2) Upload the file to the URL in the previous response. Also need to include the upload params in the
              exact order they were returned in step 1. The last param must be the file.
           3) Canvas will return either a 201 or 3XX status. If 201, the upload is complete. Otherwise, you
              then have to confirm the upload's success by completing a GET to the location header returned in the response.
         */

        CanvasFile canvasFile = null;
        String uri = fileUploadResponse.getUploadUrl();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        for (Map.Entry<String, String> entry : fileUploadResponse.getUploadParams().entrySet()) {
            map.add(entry.getKey(), entry.getValue());
        }

        FileSystemResource fileAsResource = new FileSystemResource(file);
        map.add("file", fileAsResource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<Object> responseEntity = restTemplateNoBuffer.postForEntity(uri, requestEntity, Object.class);
            HttpStatusCode httpStatus = responseEntity.getStatusCode();

            if (httpStatus.equals(HttpStatus.OK)) {
                // upload was successful and no further action necessary
                canvasFile = (CanvasFile) responseEntity.getBody();

            } else if (httpStatus.equals(HttpStatus.CREATED) || httpStatus.is3xxRedirection() ) {
                // if the status is 3XX or 201 we need to GET at the location to complete the upload.
                String location = responseEntity.getHeaders().getFirst("location");
                if (location != null) {
                    canvasFile = verifyUpload(location);

                } else {
                    String exception = "No location returned by Canvas to complete upload despite status " + responseEntity.getStatusCode().value();
                    log.error(exception);
                    throw new RuntimeException(exception);
                }

            } else {
                String errorEntity = null;
                if (responseEntity.hasBody()) {
                    errorEntity = responseEntity.getBody().toString();
                }
                throw new RuntimeException("File Upload to Canvas was not successful. Response code: "
                        + responseEntity.getStatusCode().value() + ", reason: " + ((HttpStatus)httpStatus).getReasonPhrase()
                        + ", entity: " + errorEntity);
            }

        } catch(Exception error) {
            String exception = "An unexpected error occurred uploading a file to Canvas. File size: " + file.length() + " File name: " + file.getName();
            log.error(exception);
            throw new RuntimeException(exception, error);
        }

        return canvasFile;
    }

    private CanvasFile verifyUpload(String location) {
        CanvasFile canvasFile = null;

        try {
            HttpEntity<CanvasFile> fileUploadResponseEntity = this.restTemplate.getForEntity(location, CanvasFile.class);
            log.debug("fileUploadResponseEntity: {}", fileUploadResponseEntity);

            canvasFile = fileUploadResponseEntity.getBody();
        } catch (HttpClientErrorException hcee) {
            log.error("Error:", hcee);
        }

        return canvasFile;
    }

}
