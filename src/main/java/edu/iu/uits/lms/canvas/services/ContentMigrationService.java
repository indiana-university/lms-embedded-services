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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.iu.uits.lms.canvas.helpers.CanvasConstants;
import edu.iu.uits.lms.canvas.model.ContentMigration;
import edu.iu.uits.lms.canvas.model.ContentMigrationCreateWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriTemplate;

import java.io.IOException;
import java.net.URI;
import java.util.List;

/**
 * API for accessing content migrations and migration issues
 * @see <a href="https://canvas.instructure.com/doc/api/content_migrations.html">Content Migrations API</a>
 */
@Service
@Slf4j
public class ContentMigrationService extends SpringBaseService {

   private static final String BASE_URI = "{url}/courses/{course_id}/content_migrations";
   private static final UriTemplate CREATE_COURSE_MIGRATION = new UriTemplate(BASE_URI);
   private static final UriTemplate GET_MIGRATION_STATUS_BY_COURSE = new UriTemplate(BASE_URI);
   private static final UriTemplate GET_MIGRATION_STATUS_BY_ID = new UriTemplate(BASE_URI + "/{migrationId}");

   /**
    * Create a content migration
    * @param courseId Course Id
    * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
    *                    the Canvas Course API allows retrieving Course objects by identifiers other than the
    *                    internal id. See {@link CanvasConstants} for API_FIELD options
    * @param fileUrl A URL to download the file from. Must not require authentication.
    * @return content migration
    * @deprecated Use initiateContentMigration with the MIGRATION_TYPE_CC type
    */
   @Deprecated(since = "5.0.8")
   public ContentMigration importCCIntoCourse(String courseId, String idFieldName, String fileUrl) {
      String altCourseId = buildAlternateId(courseId, idFieldName);
      URI uri = CREATE_COURSE_MIGRATION.expand(canvasConfiguration.getBaseApiUrl(), altCourseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);

      builder.queryParam("settings[file_url]", fileUrl);
      builder.queryParam("migration_type", ContentMigration.MIGRATION_TYPE_CC);

      log.debug("woooooo: " + builder.build().toUri());

      ContentMigration contentMigration = null;

      try {
         HttpEntity<ContentMigration> response = restTemplate.postForEntity(builder.build().toUri(), null, ContentMigration.class);
         contentMigration = response.getBody();
      } catch (HttpStatusCodeException rce) {
         log.error("Error performing content migration for course: " + courseId, rce);
         ObjectMapper mapper = new ObjectMapper();

         try {
            String status = mapper.readValue(rce.getResponseBodyAsString(), String.class);
            log.error("Unable to begin a migration: " + status);
         } catch (IOException e) {
            log.error("Error parsing error message", e);
         }
      }
      return contentMigration;
   }

   /**
    * Create a content migration
    * @param courseId Course Id
    * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
    *                    the Canvas Course API allows retrieving Course objects by identifiers other than the
    *                    internal id. See {@link CanvasConstants} for API_FIELD options
    * @param wrapper ContentMigrationCreateWrapper object used to create the content migration
    * @return content migration
    */
   public ContentMigration initiateContentMigration(String courseId, String idFieldName, ContentMigrationCreateWrapper wrapper) {
      String altCourseId = buildAlternateId(courseId, idFieldName);
      URI uri = CREATE_COURSE_MIGRATION.expand(canvasConfiguration.getBaseApiUrl(), altCourseId);

      UriComponentsBuilder builder = UriComponentsBuilder.fromUri(uri);
      log.debug("woooooo: " + builder.build().toUri());

      ContentMigration contentMigration = null;

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<ContentMigrationCreateWrapper> requestEntity = new HttpEntity<>(wrapper, headers);

      try {
         HttpEntity<ContentMigration> response = restTemplate.postForEntity(builder.build().toUri(), requestEntity, ContentMigration.class);
         contentMigration = response.getBody();
      } catch (HttpStatusCodeException rce) {
         log.error("Error performing content migration for course: " + courseId, rce);
         ObjectMapper mapper = new ObjectMapper();

         try {
            String status = mapper.readValue(rce.getResponseBodyAsString(), String.class);
            log.error("Unable to begin a migration: " + status);
         } catch (IOException e) {
            log.error("Error parsing error message", e);
         }
      } catch (Exception e) {
         log.error("Error performing content migration for course: " + courseId, e);
      }
      return contentMigration;
   }

   /**
    * Returns all content migrations for a course
    * @param courseId Course Id
    * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
    *                    the Canvas Course API allows retrieving Course objects by identifiers other than the
    *                    internal id. See {@link CanvasConstants} for API_FIELD options
    * @return content migrations
    */
   public List<ContentMigration> getMigrationStatuses(String courseId, String idFieldName) {
      String altCourseId = buildAlternateId(courseId, idFieldName);
      URI uri = GET_MIGRATION_STATUS_BY_COURSE.expand(canvasConfiguration.getBaseApiUrl(), altCourseId);
      log.debug("uri {}", uri);
      return doGet(uri, ContentMigration[].class);
   }

   /**
    * Returns data on an individual content migration
    * @param courseId Course Id
    * @param idFieldName Optional. Leave null if the id param given is the Canvas internal id. Otherwise,
    *                    the Canvas Course API allows retrieving Course objects by identifiers other than the
    *                    internal id. See {@link CanvasConstants} for API_FIELD options
    * @param migrationId Migration Id
    * @return content migration
    */
   public ContentMigration getMigrationStatus(String courseId, String idFieldName, String migrationId) {
      String altCourseId = buildAlternateId(courseId, idFieldName);
      URI uri = GET_MIGRATION_STATUS_BY_ID.expand(canvasConfiguration.getBaseApiUrl(), altCourseId, migrationId);
      log.debug("uri {}", uri);
      HttpEntity<ContentMigration> template = this.restTemplate.getForEntity(uri, ContentMigration.class);
      log.debug("template {}", template);
      return template.getBody();
   }
}
