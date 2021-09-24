package edu.iu.uits.lms.common.samesite;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Add this annotation to an {@code @Configuration} class to expose the
 * {@link CookieFilter} as a bean.
 * @since 4.0.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CookieFilterConfig.class)
@Configuration(proxyBeanMethods = false)
public @interface EnableCookieFilter {

   /**
    * Specify a list of request patterns that will be ignored by the {@link CookieFilter}.
    * If {@link #overrideIgnoredRequestPatterns()} is set to true, the values specified here will override any
    * specified as system-level defaults.  If {@link #overrideIgnoredRequestPatterns()} is set to false (default),
    * the values specified here will be appended to the system-level defaults.
    * @return The list of request patterns to be ignored
    */
   String[] ignoredRequestPatterns() default {};

   /**
    * Specify whether the system default from {@code session.ignoredRequests} will be overridden with the value
    * from {@link #ignoredRequestPatterns()} or if it will be appended.
    * @return Whether to override or append the values specified
    */
   boolean overrideIgnoredRequestPatterns() default false;

   /**
    * Specify a user agent regex string that will be used by the {@link CookieFilter}.
    * If a value is specified here, it will override the system default from {@code session.uaFilter}.
    * @return The user agent regex string
    */
   String ignoredUserAgentRegex() default "";
}

