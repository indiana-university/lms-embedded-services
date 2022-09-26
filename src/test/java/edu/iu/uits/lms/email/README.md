# README

## File Bundling Notes

As of v5.0.4, any files in the `edu.iu.uits.lms.email.rest` package will NOT be included in the test-jar bundle.

Only files in the `edu.iu.uits.lms.email` package will be included.

Configuration of this is handled by the `maven-jar-plugin` in the project's pom.xml.
