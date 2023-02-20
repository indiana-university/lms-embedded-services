package edu.iu.uits.lms.common.actuator;

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
