# LMS Canvas Email Services

When this library is added to a project it allows for email sending capabilities.

## Installation
### From Maven
Add the following as a dependency in your pom.xml
```xml
<dependency>
    <groupId>edu.iu.uits.lms</groupId>
    <artifactId>lms-email-service</artifactId>
    <version><!-- latest version --></version>
</dependency>
```

You can find the latest version in [Maven Central](https://search.maven.org/search?q=g:edu.iu.uits.lms%20AND%20a:lms-email-service).

## Setup Examples
### Include annotation to enable to the configs
Add to any configuration class, or even the main application class `@EnableEmailClient`.

Once that has been done, you can autowire in and use the email service:

```java
@Autowired
private EmailService emailService;
```

## Configuration
If choosing to use properties files for the configuration values, the default location is `/usr/src/app/config`, but that can be overridden by setting the `app.fullFilePath` value via system property or environment variable.

### Email Configuration
The following properties need to be set to configure the communication with a database.
They can be set in a security.properties file, or overridden as environment variables.

| Property                                           | Default Value                               | Description                                       |
|----------------------------------------------------|---------------------------------------------|---------------------------------------------------|
| `spring.mail.host`                                 | mail-relay.iu.edu                           | Hostname of the mail relay                        |
| `spring.mail.port`                                 | 587                                         | Port of the mail relay                            |
| `spring.mail.username`                             | lmssmtp                                     | Username for sending email                        |
| `smtpPassword`                                     |                                             | Password for sending email                        |
| `spring.mail.password`                             | `${smtpPassword}`                           | Password for sending email                        |
| `spring.mail.properties.mail.smtp.auth`            | true                                        | Additional JavaMail Session properties                                                  |
| `spring.mail.properties.mail.smtp.starttls.enable` | true                                        | Additional JavaMail Session properties                                                  |
| `lmsemail.enabled`                                 | true                                        | Enable/disable email sending                      |
| `lmsemail.signingEnabled`                          | true                                        | Enable/disable digitally signing emails           |
| `lmsemail.defaultFrom`                             | essnorep@iu.edu                             | Default `from` address                            |
| `lmsemail.defaultUnsignedTo`                       | iu-uits-es-ess-lms-notify@exchange.iu.edu   | Default recipient for emails in test environments |
| `sis.rest.url`                                     |                                             | URL for the email signing service                 |
| `sis.rest.region`                                  |                                             | Region for the email signing service              |
| `sis.rest.apiToken`                                |                                             | API token for the email signing service           |

### Exposing the email REST endpoints
If you would like to expose the email endpoints in a tool, you will
need to enable it by including the value `emailrest` into the `SPRING_PROFILES_ACTIVE` environment variable. Be aware that if the tool requires multiple values, that there could be more than one profile value in there.

#### OAuth2 requirements
In order to get access to the endpoints, you'd need to configure an OAuth2 server.  Once setup, the user(s) needing access
would have to be granted the `email:send` scope.  Grant type should be set as `Authorization Code`.

#### REST endpoint documentation
See the [wiki](wiki/API-Endpoint-Documentation) for details.

#### Dev notes
To generate the REST docs (asciidoc) that live in the github wiki, take the following steps:
1. Enable the rest endpoints and swagger in the tool of choice and start it up
2. Note the api docs url.  Should be something like http://localhost:8080/api/email/v3/api-docs
3. Download the openapi-generator from [here](https://openapi-generator.tech/docs/installation)
4. Run the following (:warning: *Command could be slightly different based on your OS and install method*):
   `openapi-generator generate -g asciidoc -i <url_from_above_step>`
5. Take the generated output and update the wiki page (:warning: *Some hand editing may be required*)
