package edu.iu.uits.lms.common.actuator;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2023 Indiana University
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

import org.apache.catalina.startup.Tomcat;
import org.springframework.boot.SpringBootVersion;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.metrics.buffering.StartupTimeline;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Endpoint(id="lms")
@Component
public class LmsCustomActuatorEndpoint {

   private final List<InfoContributor> infoContributors;

   private final BufferingApplicationStartup applicationStartup;

   /**
    * Create a new {@link InfoEndpoint} instance.
    * @param infoContributors the info contributors to use
    */
   public LmsCustomActuatorEndpoint(List<InfoContributor> infoContributors, BufferingApplicationStartup applicationStartup) {
      Assert.notNull(infoContributors, "Info contributors must not be null");
      Assert.notNull(applicationStartup, "BufferingApplicationStartup must not be null");
      this.infoContributors = infoContributors;
      this.applicationStartup = applicationStartup;
   }

   @ReadOperation
   public Map<String, Object> info() {
      Info.Builder builder = new Info.Builder();
      for (InfoContributor contributor : this.infoContributors) {
         contributor.contribute(builder);
      }

      StartupTimeline startupTimeline = this.applicationStartup.getBufferedTimeline();

      DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT)
            .withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault());

      builder.withDetail("startup", formatter.format(startupTimeline.getStartTime()));
//      builder.withDetail("startup-timeline", startupTimeline);
      builder.withDetail("spring-boot", SpringBootVersion.getVersion());

      Package tomcatPkg = Tomcat.class.getPackage();
      String tomcatVersion =  tomcatPkg != null ? tomcatPkg.getImplementationVersion() : null;

      builder.withDetail("tomcat", tomcatVersion);

      Info build = builder.build();
      return build.getDetails();
   }

}
