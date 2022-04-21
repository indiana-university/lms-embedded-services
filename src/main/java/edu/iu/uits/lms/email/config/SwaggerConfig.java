package edu.iu.uits.lms.email.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("emailrest & swagger")
@Configuration("EmailSwaggerConfig")
@SecurityScheme(name = "security_auth_email", type = SecuritySchemeType.OAUTH2,
      flows = @OAuthFlows(authorizationCode = @OAuthFlow(
            authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
            scopes = {@OAuthScope(name = "email:send")},
            tokenUrl = "${springdoc.oAuthFlow.tokenUrl}")))
public class SwaggerConfig {

   @Bean
   public GroupedOpenApi emailOpenApi() {
      return GroupedOpenApi.builder()
            .group("email")
            .packagesToScan("edu.iu.uits.lms.email")
            .pathsToMatch("/rest/email/**")
            .build();
   }
}
