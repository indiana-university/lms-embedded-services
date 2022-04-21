# LMS Canvas LTI Framework Services

When this library is added to a project it allows for LTI framework service details to be setup.

## Installation
### From Maven
Add the following as a dependency in your pom.xml
```xml
<dependency>
    <groupId>edu.iu.uits.lms</groupId>
    <artifactId>lms-canvas-lti-framework</artifactId>
    <version><!-- latest version --></version>
</dependency>
```

You can find the latest version in [Maven Central](https://search.maven.org/search?q=g:edu.iu.uits.lms%20AND%20a:lms-canvas-lti-framework).

## Setup Examples
### Include annotation to enable to the configs
Add to any configuration class, or even the main application class `@EnableLtiClient(toolKeys = {"example_tool_id"})`.

Replace `example_tool_id` with something that will uniquely identify your tool. Once that has been done, you can autowire in and use any lti service.  Generally, you won't use them directly, but they are used in various configurations.

### Override default role mappings
Implement a class, similar to the below
```java
public class CustomRoleMapper extends LmsDefaultGrantedAuthoritiesMapper {
   @Override
   protected List<String> getDefaultInstructorRoles() {
      return Arrays.asList("special", "roles", "here");
   }
}
```
Then, just use that when configuring...
```java
Lti13Configurer lti13Configurer = new Lti13Configurer()
      .grantedAuthoritiesMapper(new CustomRoleMapper());
```

## Setup Database
After compiling, see `target/generated-resources/sql/ddl/auto/postgresql9.sql` for appropriate ddl.
Insert a record into the `LTI_13_AUTHZ` table with your tool's registration_id (`example_tool_id`, from above), along with the client_id and secret from Canvas's Developer Key.  An `env` designator is also required here, and allows a database to support multiple environments simultaneously (dev and reg, for example).

## Configuration
If choosing to use properties files for the configuration values, the default location is `/usr/src/app/config`, but that can be overridden by setting the `app.fullFilePath` value via system property or environment variable.

### Database Configuration
The following properties need to be set to configure the communication with a database.
They can be set in a security.properties file, or overridden as environment variables.

| Property | Description |
|-------|----------------|
| `lms.db.user`         | Username used to access the database |
| `lms.db.url`          | JDBC URL of the database.  Will have the form `jdbc:<host>:<port>/<database>` |
| `lms.db.driverClass`  | JDBC Driver class name |
| `lms.db.password`     | Password for the user accessing the database |

### Exposing the LTI authz REST endpoints
If you would like to expose the LTI authz endpoints in a tool (for CRUD operations on the LTI authorizations), you will
need to enable it by including the value `ltirest` into the `SPRING_PROFILES_ACTIVE` environment variable. Be aware that if the tool requires multiple values, that there could be more than one profile value in there.

#### OAuth2 requirements
In order to get access to the endpoints, you'd need to configure an OAuth2 server.  Once setup, the user(s) needing access 
would have to be granted the `lti:read` and/or the `lti:write` scopes as appropriate.  Grant type should be set as `Authorization Code`. 

#### REST endpoint documentation
See the [wiki](wiki/API-Endpoint-Documentation) for details.

#### Dev notes
To generate the REST docs (asciidoc) that live in the github wiki, take the following steps:
1. Enable the rest endpoints and swagger in the tool of choice and start it up
2. Note the api docs url.  Should be something like http://localhost:8080/api/lti/v3/api-docs
3. Download the openapi-generator from [here](https://openapi-generator.tech/docs/installation)
4. Run the following (:warning: *Command could be slightly different based on your OS and install method*):
`openapi-generator generate -g asciidoc -i <url_from_above_step>`
5. Take the generated output and update the wiki page (:warning: *Some hand editing may be required*)
