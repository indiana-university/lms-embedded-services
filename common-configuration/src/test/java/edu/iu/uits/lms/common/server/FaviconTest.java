package edu.iu.uits.lms.common.server;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.NestedTestConfiguration;

@ContextConfiguration(classes = {FaviconConfiguration.class})
@ActiveProfiles("none")
@NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
public class FaviconTest {


    @Nested
    @SpringBootTest
    public class NotConfiguredTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertFalse(faviconControllerAdvice.getFaviconProperties().isEnabled());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=false"})
    public class DisabledTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertFalse(faviconControllerAdvice.getFaviconProperties().isEnabled());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true"})
    public class EnabledDefaultTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getFaviconProperties().isEnabled());
            Assertions.assertEquals("/favicon.ico", faviconControllerAdvice.getFaviconProperties().getUrl());
            Assertions.assertEquals(FaviconProperties.TYPE.PATH, faviconControllerAdvice.getFaviconProperties().getType());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true", "lms.favicon.url=http://asdf.foo/favicon.ico"})
    public class EnabledUrlTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getFaviconProperties().isEnabled());
            Assertions.assertEquals("http://asdf.foo/favicon.ico", faviconControllerAdvice.getFaviconProperties().getUrl());
            Assertions.assertEquals(FaviconProperties.TYPE.URL, faviconControllerAdvice.getFaviconProperties().getType());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true", "lms.favicon.path=/asdf.ico"})
    public class EnabledPathTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getFaviconProperties().isEnabled());
            Assertions.assertEquals("/asdf.ico", faviconControllerAdvice.getFaviconProperties().getUrl());
            Assertions.assertEquals(FaviconProperties.TYPE.PATH, faviconControllerAdvice.getFaviconProperties().getType());
        }
    }



    private static class FaviconBase {
        @Autowired
//        protected FaviconProperties faviconProperties;
        protected FaviconControllerAdvice faviconControllerAdvice;
    }



}
