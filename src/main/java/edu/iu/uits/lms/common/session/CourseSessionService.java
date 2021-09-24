package edu.iu.uits.lms.common.session;

/*-
 * #%L
 * lms-canvas-common-configuration
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

import lombok.AllArgsConstructor;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for interacting with the session so that we can easily get/set course-specific values
 */
@AllArgsConstructor
public class CourseSessionService {

   private String sessionKey;

   /**
    * Add an attribute to the session for a course
    * @param session
    * @param courseId CourseId key to use since the session could be reused for a different course launch.
    * @param key Key within the course context
    * @param value Value to set
    */
   public void addAttributeToSession(HttpSession session, String courseId, String key, Object value)  {
      Map<String, Map<String, Object>> courseMap = (Map<String, Map<String, Object>>) session.getAttribute(sessionKey);
      if (courseMap == null) {
         courseMap = new HashMap<>();
      }

      Map<String, Object> courseData = courseMap.computeIfAbsent(courseId, k -> new HashMap<>());
      courseData.put(key, value);
      session.setAttribute(sessionKey, courseMap);
   }

   /**
    * Get an attribute from the session for a course
    * @param session
    * @param courseId CourseId key to use since the session could be reused for a different course launch.
    * @param key Key within the course context
    * @param clazz Return type
    * @return The object from the session, or null if not found
    */
   public <T> T getAttributeFromSession(HttpSession session, String courseId, String key, Class<T> clazz) {
      Map<String, Map<String, Object>> courseMap = (Map<String, Map<String, Object>>) session.getAttribute(sessionKey);
      if (courseMap != null) {
         Map<String, Object> courseData = courseMap.get(courseId);
         if (courseData != null) {
            return (T)courseData.get(key);
         }
      }
      return null;
   }

   /**
    * Remove an attribute from the session for a course
    * @param session
    * @param courseId CourseId key to use since the session could be reused for a different course launch
    * @param key Key within the course context
    * @return The object from the session, or null if not found
    */
   public <T> T removeAttributeFromSession(HttpSession session, String courseId, String key) {
      Map<String, Map<String, Object>> courseMap = (Map<String, Map<String, Object>>) session.getAttribute(sessionKey);
      if (courseMap != null) {
         Map<String, Object> courseData = courseMap.get(courseId);
         if (courseData != null) {
            return (T)courseData.remove(key);
         }
      }
      return null;
   }
}
