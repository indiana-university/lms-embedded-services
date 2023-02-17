package edu.iu.uits.lms.common.oauth;

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

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class CustomJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

   private ClaimPair[] claimPairs;

   public CustomJwtGrantedAuthoritiesConverter(ClaimPair... claimPairs) {
      this.claimPairs = claimPairs;
   }

   /**
    * Extract {@link GrantedAuthority}s from the given {@link org.springframework.security.oauth2.jwt.Jwt}.
    *
    * @param jwt The {@link org.springframework.security.oauth2.jwt.Jwt} token
    * @return The {@link GrantedAuthority authorities} read from the token scopes
    */
   @Override
   public Collection<GrantedAuthority> convert(Jwt jwt) {
      Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
      for (ClaimPair claimPair : claimPairs) {
         for (String authority : getAuthorities(jwt, claimPair.getClaimName())) {
            grantedAuthorities.add(new SimpleGrantedAuthority(claimPair.getAuthorityPrefix() + authority));
         }
      }
      return grantedAuthorities;
   }

   private Collection<String> getAuthorities(Jwt jwt, String claimName) {
      if (claimName == null) {
         return Collections.emptyList();
      }

      Object authorities = jwt.getClaim(claimName);
      if (authorities instanceof String) {
         if (StringUtils.hasText((String) authorities)) {
            return Arrays.asList(((String) authorities).split(" "));
         } else {
            return Collections.emptyList();
         }
      } else if (authorities instanceof Collection) {
         return (Collection<String>) authorities;
      }

      return Collections.emptyList();
   }

   @Data
   @AllArgsConstructor
   static class ClaimPair {
      private String claimName;
      private String authorityPrefix;
   }
}
