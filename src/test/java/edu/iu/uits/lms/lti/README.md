# README

## File Bundling Notes

As of v5.1.13, any files in the `edu.iu.uits.lms.lti.rest` and `edu.iu.uits.lms.lti.service` packages will NOT be included in the test-jar bundle.

Files anywhere else in the `edu.iu.uits.lms.lti` package will be included.

Configuration of this is handled by the `maven-jar-plugin` in the project's pom.xml.
