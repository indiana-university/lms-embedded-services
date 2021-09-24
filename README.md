# LMS Canvas Services

When this library is added to a project it allows for redis connection details to be setup.

## Installation
### From Maven
Add the following as a dependency in your pom.xml
```xml
<dependency>
    <groupId>edu.iu.uits.lms</groupId>
    <artifactId>lms-canvas-services</artifactId>
    <version><!-- latest version --></version>
</dependency>
```

You can find the latest version in [Maven Central](https://search.maven.org/search?q=g:edu.iu.uits.lms%20AND%20a:lms-canvas-services).

## Setup Examples
### Include annotation to enable to the configs
Add to any configuration class, or even the main application class `@EnableCanvasClient`. 

Once that has been done, you can autowire in and use any canvas service.


## Configuration
If choosing to use properties files for the configuration values, the default location is `/usr/src/app/config`, but that can be overridden by setting the `app.fullFilePath` value via system property or environment variable.

### Canvas Configuration
The following properties need to be set to configure the communication with Canvas and Canvas Catalog.
They can be set in a security.properties file, or overridden as environment variables.

| Property | Default Value | Description |
|-------|--------------------------------|-------------|
| `canvas.host`         |   | Hostname of the Canvas instance |
| `canvas.baseUrl`      | https://`${canvas.host}`           | Base URL of the Canvas instance |
| `canvas.baseApiUrl`   | `${canvas.baseUrl}`/api/v1         | Base URL for the Canvas API |
| `canvas.token`        |   | Token for access to Canvas instance |
| `canvas.accountId`        |   | Your institution's root accountId in your Canvas instance |
| `catalog.baseUrl`      |   | Base URL of the Canvas Catalog instance |
| `catalog.baseApiUrl`   | `${catalog.baseUrl}`/api/v1     | Base URL for the Canvas Catalog API |
| `catalog.token`        |   | Token for access to the Canvas Catalog instance |
