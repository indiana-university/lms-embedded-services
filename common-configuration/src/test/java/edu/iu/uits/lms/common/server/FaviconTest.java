package edu.iu.uits.lms.common.server;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2024 Indiana University
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the Indiana University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.NestedTestConfiguration;

@ContextConfiguration(classes = {BrandingConfiguration.class})
@ActiveProfiles("none")
@NestedTestConfiguration(NestedTestConfiguration.EnclosingConfiguration.INHERIT)
public class FaviconTest {


    @Nested
    @SpringBootTest
    public class NotConfiguredTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertFalse(faviconControllerAdvice.getBrandingProperties().isFaviconEnabled());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=false"})
    public class DisabledTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertFalse(faviconControllerAdvice.getBrandingProperties().isFaviconEnabled());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true"})
    public class EnabledDefaultTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getBrandingProperties().isFaviconEnabled());
            Assertions.assertEquals("/favicon.ico", faviconControllerAdvice.getBrandingProperties().getFaviconUrl());
            Assertions.assertEquals(BrandingProperties.TYPE.PATH, faviconControllerAdvice.getBrandingProperties().getFaviconType());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true", "lms.favicon.url=http://asdf.foo/favicon.ico"})
    public class EnabledUrlTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getBrandingProperties().isFaviconEnabled());
            Assertions.assertEquals("http://asdf.foo/favicon.ico", faviconControllerAdvice.getBrandingProperties().getFaviconUrl());
            Assertions.assertEquals(BrandingProperties.TYPE.URL, faviconControllerAdvice.getBrandingProperties().getFaviconType());
        }
    }

    @Nested
    @SpringBootTest(properties = {"lms.favicon.enabled=true", "lms.favicon.path=/asdf.ico"})
    public class EnabledPathTest extends FaviconBase {
        @Test
        void testConfig() {
            Assertions.assertTrue(faviconControllerAdvice.getBrandingProperties().isFaviconEnabled());
            Assertions.assertEquals("/asdf.ico", faviconControllerAdvice.getBrandingProperties().getFaviconUrl());
            Assertions.assertEquals(BrandingProperties.TYPE.PATH, faviconControllerAdvice.getBrandingProperties().getFaviconType());
        }
    }



    private static class FaviconBase {
        @Autowired
        protected BrandingControllerAdvice faviconControllerAdvice;
    }

}
