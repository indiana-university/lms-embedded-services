/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ox.ctl.oauth2.client.annotation;

/*-
 * #%L
 * LMS Canvas OAuth2 Client
 * %%
 * Copyright (C) 2015 - 2026 Indiana University
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

import org.springframework.core.annotation.AliasFor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.web.method.annotation.OAuth2AuthorizedClientArgumentResolver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation may be used to resolve a method parameter to an argument value of type {@link
 * OAuth2AuthorizedClient}.
 *
 * <p>For example:
 *
 * <pre>
 * &#64;Controller
 * public class MyController {
 *     &#64;GetMapping("/authorized-client")
 *     public String authorizedClient(@RegisteredOAuth2AuthorizedClient("login-client") OAuth2AuthorizedClient authorizedClient) {
 *         // do something with authorizedClient
 *     }
 * }
 * </pre>
 *
 * @author Joe Grandja
 * @see OAuth2AuthorizedClientArgumentResolver
 * @since 5.1
 */
@Target({ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RegisteredOAuth2AuthorizedClient {

  /**
   * Sets the client registration identifier.
   *
   * @return the client registration identifier
   */
  @AliasFor("value")
  String registrationId() default "";

  /**
   * The default attribute for this annotation. This is an alias for {@link #registrationId()}. For
   * example, {@code @RegisteredOAuth2AuthorizedClient("login-client")} is equivalent to
   * {@code @RegisteredOAuth2AuthorizedClient(registrationId="login-client")}.
   *
   * @return the client registration identifier
   */
  @AliasFor("registrationId")
  String value() default "";

  /**
   * Set if this OAuth2AuthorizedClient is required.
   * @return if the parameter is required.
   */
  boolean required() default true;

  /**
   * Set if the existing authorized client should be deleted so we get another one.
   * @return if any existing authorized client should be deleted.
   */
  boolean renew() default false;
}
