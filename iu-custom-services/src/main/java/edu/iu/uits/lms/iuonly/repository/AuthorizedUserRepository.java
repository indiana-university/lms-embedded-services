package edu.iu.uits.lms.iuonly.repository;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;

@Component
@RepositoryRestResource(path = "auth_user")
@Tag(name = "AuthorizedUserRepository", description = "Operations involving the AuthorizedUser table")
@CrossOrigin(origins = {"${lms.swagger.cors.origin}"})
public interface AuthorizedUserRepository extends PagingAndSortingRepository<AuthorizedUser, Long>, ListCrudRepository<AuthorizedUser, Long> {

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.username = :username")
   AuthorizedUser findByUsername(@Param("username") String username);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.canvasUserId = :canvasUserId")
   AuthorizedUser findByCanvasUserId(@Param("canvasUserId") String canvasUserId);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.active = true AND au.username = :username AND KEY(tp) = :toolPermission")
   AuthorizedUser findByActiveUsernameAndToolPermission(@Param("username") String username, @Param("toolPermission") String toolPermission);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.active = true AND au.canvasUserId = :canvasUserId AND KEY(tp) = :toolPermission")
   AuthorizedUser findByActiveCanvasUserIdAndToolPermission(@Param("canvasUserId") String canvasUserId, @Param("toolPermission") String toolPermission);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.active = true AND KEY(tp) = :toolPermission")
   List<AuthorizedUser> findByActiveToolPermission(@Param("toolPermission") String toolPermission);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE KEY(tp) = :toolPermission")
   List<AuthorizedUser> findByToolPermission(@Param("toolPermission") String toolPermission);
}
