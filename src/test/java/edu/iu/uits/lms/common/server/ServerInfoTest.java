package edu.iu.uits.lms.common.server;

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

import java.util.Calendar;
import java.util.Date;

@Slf4j
public class ServerInfoTest {

   private static Date theDate;
   private static final String serverString = "Bob";
   private static final String dateString = "2020/7/31 - 15:48";
   private static final String envString = "fooenv";
   private static final String gitString = "asdf@qwerty";
   private static final String versionString = "1.2.3";
   private static final String SEP = " - ";

   @BeforeEach
   public void setUp() throws Exception {
      Calendar cal = Calendar.getInstance();
      cal.set(2020, Calendar.JULY, 31, 15, 48);
      theDate = cal.getTime();
   }

   @Test
   public void testToString() {

      String info = ServerInfo.builder().serverName(serverString).buildDate(theDate).build().toString();
      Assertions.assertEquals(serverString + " - " + dateString, info);

      info = ServerInfo.builder().serverName(serverString).buildDate(theDate).environment(envString).build().toString();
      Assertions.assertEquals(serverString + "-" + envString + SEP + dateString, info);

      info = ServerInfo.builder().serverName(serverString).buildDate(theDate).environment(envString).gitInfo(gitString)
            .build().toString();
      Assertions.assertEquals(serverString + "-" + envString + SEP + dateString + SEP + gitString, info);

      info = ServerInfo.builder().serverName(serverString).buildDate(theDate).environment(envString).gitInfo(gitString)
            .artifactVersion(versionString).build().toString();
      Assertions.assertEquals(serverString + "-" + envString + SEP + dateString + SEP + gitString + SEP + versionString, info);

      info = ServerInfo.builder().serverName(serverString).buildDate(theDate).environment(envString)
            .artifactVersion(versionString).build().toString();
      Assertions.assertEquals(serverString + "-" + envString + SEP + dateString + SEP + versionString, info);

   }
}
