# LMS Canvas Error Contact Service

When this library is added to a project it provides job failure alerting for batch jobs: when a job fails, it sends an email notification and — if the failure count within a configurable time window exceeds a threshold — pages the on-call team via Derdack. Job profiles and failure events are persisted in a PostgreSQL database.

## How It Works

1. The consuming application calls `ErrorContactServiceImpl.postEvent(ErrorContactPostForm)` with a `jobCode` and `message`.
2. The service looks up the matching `ErrorContactJobProfile` in the database.
3. It counts how many times that job has failed within `duplicateMinutesThreshold` minutes.
4. If the failure count reaches `duplicateMaxCount`, or if `alwaysPage` is set to `true`, a Derdack page is sent to the configured team in addition to an email notification.
5. Otherwise, only an email notification is sent.

All core beans are active only when the `derdack` Spring profile is enabled.

## Installation
### From Maven
Add the following as a dependency in your pom.xml
```xml
<dependency>
    <groupId>edu.iu.uits.lms</groupId>
    <artifactId>lms-canvas-error-contact-service</artifactId>
    <version><!-- latest version --></version>
</dependency>
```

You can find the latest version in [Maven Central](https://search.maven.org/search?q=g:edu.iu.uits.lms%20AND%20a:lms-canvas-error-contact-service).

## Setup Examples
### Include annotation to enable the configs
Add `@EnableErrorContactClient` to any configuration class or the main application class.

Once that has been done, you can autowire in and use the service:

```java
@Autowired
private ErrorContactServiceImpl errorContactService;
```

### Submitting a failure event
```java
ErrorContactPostForm form = new ErrorContactPostForm();
form.setJobCode("MY_JOB_CODE");
form.setMessage("Detailed error description");
form.setAlwaysPage(false); // set true to bypass the threshold check and always page
ErrorContactResponse response = errorContactService.postEvent(form);
```

## Configuration
If choosing to use properties files for the configuration values, the default location is `/usr/src/app/config`, but 
that can be overridden by setting the `app.fullFilePath` value via system property or environment variable.

### Database Configuration
The following properties configure the PostgreSQL database used to store job profiles and failure events.
They can be set in a `security.properties` file, or overridden as environment variables.

| Property          | Description                                                                                                            |
|-------------------|------------------------------------------------------------------------------------------------------------------------|
| `lms.db.user`     | Username used to access the database                                                                                   |
| `lms.db.url`      | JDBC URL of the database. Will have the form `jdbc:<dbtype>://<host>:<port>/<database>`                               |
| `lms.db.password` | Password for the user accessing the database                                                                           |
| `lms.db.poolType` | Fully qualified name of the connection pool implementation to use. By default, it is auto-detected from the classpath. |

### Derdack Configuration
The following properties configure the Derdack paging integration.
They can be set in a `security.properties` file, or overridden as environment variables.

You must enable the `derdack` Spring profile to activate the service beans:

```
SPRING_PROFILES_ACTIVE=derdack
```

| Property                 | Default Value                             | Description                          |
|--------------------------|-------------------------------------------|--------------------------------------|
| `derdack.rest.baseUrl`   |                                           | Base URL for Derdack API             |
| `derdack.rest.apiKey`    |                                           | Derdack API key                      |
| `derdack.rest.team`      |                                           | Team name for Derdack pages          |
| `derdack.recipientEmail` | iu-uits-es-ess-lms-notify@exchange.iu.edu | Email address for notifications      |

### Exposing the REST endpoints
If you would like to expose the REST endpoints in a tool, enable them by including `errorcontactrest` in `SPRING_PROFILES_ACTIVE`:

```
SPRING_PROFILES_ACTIVE=derdack,errorcontactrest
```

The REST endpoints are secured under `/rest/errorcontact/**`.

#### OAuth2 requirements
Requests must carry a JWT with both the `lms:rest` scope and the `LMS_REST_ADMINS` role.

#### REST endpoint documentation
See the [wiki](wiki/API-Endpoint-Documentation) for details.

#### Dev notes
To generate the REST docs (asciidoc) that live in the GitHub wiki, take the following steps:
1. Enable the rest endpoints and swagger in the tool of choice and start it up
2. Note the api docs url. Should be something like `http://localhost:8080/api/iu/v3/api-docs`
3. Download the openapi-generator from [here](https://openapi-generator.tech/docs/installation)
4. Run the following (:warning: *Command could be slightly different based on your OS and install method*):
   `openapi-generator generate -g asciidoc -i <url_from_above_step>`
5. Take the generated output and update the wiki page (:warning: *Some hand editing may be required*)
