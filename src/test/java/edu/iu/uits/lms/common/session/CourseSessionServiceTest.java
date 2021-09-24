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

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Slf4j
//@RunWith(SpringRunner.class)
public class CourseSessionServiceTest {

   private CourseSessionService courseSessionService;
   private static final String KEY1 = "key1";
   private static final String KEY2 = "key2";
   private static final String KEY3 = "key3";
   private static final String KEY4 = "key4";
   private static final String KEY5 = "key5";

   @BeforeEach
   public void setUp() throws Exception {
      courseSessionService = new CourseSessionService("QWERTY");
   }

   @Test
   public void sessionUtilTest() throws Exception {
      String courseId = "1234";
      String courseId2 = "9876";
      HttpSession session = new MockHttpSession();

      courseSessionService.addAttributeToSession(session, courseId2, KEY1, "FOOBAR");
      courseSessionService.addAttributeToSession(session, courseId, KEY1, "ASDF");
      courseSessionService.addAttributeToSession(session, courseId, KEY2, "0123456789");
      courseSessionService.addAttributeToSession(session, courseId, KEY5, "AAAAAAAA");

      String result = courseSessionService.getAttributeFromSession(session, courseId, KEY1, String.class);
      String result2 = courseSessionService.getAttributeFromSession(session, courseId2, KEY1, String.class);
      String result3 = courseSessionService.getAttributeFromSession(session, courseId, KEY2, String.class);
      String result4 = courseSessionService.getAttributeFromSession(session, courseId, KEY3, String.class);
      Map<String, String> result5 = courseSessionService.getAttributeFromSession(session, courseId, KEY4, Map.class);
      String result7 = courseSessionService.getAttributeFromSession(session, courseId, KEY5, String.class);
      Assertions.assertEquals("AAAAAAAA", result7);

      //More adds
      courseSessionService.addAttributeToSession(session, courseId2, KEY3, "EMAIL!");
      String result6 = courseSessionService.getAttributeFromSession(session, courseId2, KEY3, String.class);
      courseSessionService.addAttributeToSession(session, courseId, KEY5, "BBBBBBBB");
      String result8 = courseSessionService.getAttributeFromSession(session, courseId, KEY5, String.class);

      Assertions.assertEquals("ASDF", result);
      Assertions.assertEquals("FOOBAR", result2);
      Assertions.assertEquals("0123456789", result3);
      Assertions.assertNull(result4);
      Assertions.assertNull(result5);
      Assertions.assertEquals("EMAIL!", result6);

      Assertions.assertEquals("BBBBBBBB", result8);

      courseSessionService.removeAttributeFromSession(session, courseId, KEY5);
      String result9 = courseSessionService.getAttributeFromSession(session, courseId, KEY5, String.class);
      Assertions.assertNull(result9);
   }
}
