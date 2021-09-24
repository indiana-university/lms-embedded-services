# LMS Canvas Redis Configuration Helper

When this library is added to a project it allows for redis connection details to be setup.

## Installation
### From Maven
Add the following as a dependency in your pom.xml
```xml
<dependency>
    <groupId>edu.iu.uits.lms</groupId>
    <artifactId>lms-canvas-redis-config</artifactId>
    <version><!-- latest version --></version>
</dependency>
```

You can find the latest version in [Maven Central](https://search.maven.org/search?q=g:edu.iu.uits.lms%20AND%20a:lms-canvas-redis-config).

## Setup Examples
### Include annotation to enable to the configs
Add to any configuration class, or even the main application class `@EnableRedisConfiguration`. 