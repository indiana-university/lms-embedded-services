package edu.iu.uits.lms.iuonly.repository;

/*-
 * #%L
 * lms-canvas-iu-custom-services
 * %%
 * Copyright (C) 2015 - 2022 Indiana University
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

import edu.iu.uits.lms.iuonly.model.tps.AuthUserPermission;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/**
 * Repository for managing AuthUserPermission entities.
 */
@Component
public interface AuthUserPermissionRepository extends PagingAndSortingRepository<AuthUserPermission, Long>, ListCrudRepository<AuthUserPermission, Long> {

    /**
     * Find an AuthUserPermission by its ID.
     *
     * @param authPermissionId The ID of the authorization permission.
     * @return The authorization user permission associated with the given ID.
     */
    List<AuthUserPermission> findByAuthPermissionId(Long authPermissionId);

    /**
     * Check if a user has a specific permission by authUserId and permissionId.
     *
     * @param authUserId The ID of the authorized user.
     * @param permissionId The ID of the permission.
     * @return True if the user has the permission, false otherwise.
     */
    boolean existsByAuthUserIdAndAuthPermissionId(Long authUserId, Long permissionId);

    /**
     * Find a user permission by username and permission id, loading the user properties as well.
     * @param username The username of the user.
     * @param permissionId The ID of the permission.
     * @return The AuthUserPermission object associated with the given username and permission ID.
     */
    @Query("SELECT aup FROM AuthUserPermission aup " +
            "LEFT JOIN FETCH aup.userProperties " +
            "JOIN aup.authUser au " +
            "WHERE au.username = :username AND aup.authPermission.id = :permissionId")
    AuthUserPermission findByUsernameAndPermissionIdWithUserProperties(@Param("username") String username, @Param("permissionId") Long permissionId);

    /**
     * Find permission IDs associated with a user
     * @param username The username to check
     * @return Set of associated permission IDs
     */
    @Query("SELECT aup.authPermission.id FROM AuthUserPermission aup WHERE aup.authUser.username = :username")
    Set<Long> findPermissionIdsByUsername(@Param("username") String username);

    /**
     * Fetch all AuthUserPermission records.
     * @return List of AuthUserPermission.
     */
    @Query("SELECT aup FROM AuthUserPermission aup " +
            "JOIN FETCH aup.authUser " +
            "JOIN FETCH aup.authPermission ap " +
            "JOIN FETCH ap.authTool")
    List<AuthUserPermission> findAllWithDetails();

    /**
     * Find an AuthUserPermission by its ID, loading the user properties as well.
     * @param authUserPermissionId The ID of the AuthUserPermission.
     * @return The AuthUserPermission with user properties loaded.
     */
    @Query("SELECT aup FROM AuthUserPermission aup " +
            "LEFT JOIN FETCH aup.userProperties " +
            "WHERE aup.id = :authUserPermissionId")
    AuthUserPermission findByIdWithUserProperties(@Param("authUserPermissionId") Long authUserPermissionId);

    /**
     * Find all AuthUserPermission records for a given user ID.
     *
     * @param id The ID of the user.
     * @return A list of AuthUserPermission records associated with the user ID.
     */
    List<AuthUserPermission> findByAuthUserId(Long id);

}
