package edu.iu.uits.lms.common.samesite;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Disabled
public class UserAgentRegexTest {

   static Pattern pattern = null;


   @BeforeEach
   void setUp() {
      /*
<If "%{HTTP_USER_AGENT} !~ /(iPhone; CPU iPhone OS 1[0-2]|iPad; CPU OS 1[0-2]|iPod touch; CPU iPhone OS 1[0-2]|Macintosh; Intel Mac OS X.*Version\x2F1[0-2].*Safari|Macintosh;.*Mac OS X 10_14.* AppleWebKit.*Version\x2F1[0-3].*Safari)/i">
    Header edit Set-Cookie ^(.*)$ $1;SameSite=None;Secure
</If>
       */

      String uaPattern = "(iPhone; CPU iPhone OS 1[0-2]|iPad; CPU OS 1[0-2]|iPod touch; CPU iPhone OS 1[0-2]|Macintosh; Intel Mac OS X.*Version/1[0-2].*Safari|Macintosh;.*Mac OS X 10_14.* AppleWebKit.*Version/1[0-3].*Safari)";
      pattern = Pattern.compile(uaPattern, Pattern.CASE_INSENSITIVE);
      log.info("{}", pattern.flags());
   }

   @Test
   void matchUA1() {
      String text = "";
      testMatch(text, false);

      text = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36";
      testMatch(text, false);

//      text = "Mozilla/5.0 (iPad; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";
//      testMatch(text, true);
//
//      text = "Mozilla/5.0 (iPhone; CPU iPhone OS 13_2_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Mobile/15E148 Safari/604.1";
//      testMatch(text, true);

      text = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/13.0.3 Safari/605.1.15";
      testMatch(text, true);

      text = "Mozilla/5.0 (iPhone; CPU iPhone OS 10_3_1 like Mac OS X) AppleWebKit/603.1.30 (KHTML, like Gecko) Version/10.0 Mobile/14E304 Safari/602.1";
      testMatch(text, true);

   }

   private void testMatch(String string, boolean desiredResult) {
      Matcher matcher = pattern.matcher(string);
      Assertions.assertEquals(desiredResult, matcher.matches(), "incorrect match");
   }
}
