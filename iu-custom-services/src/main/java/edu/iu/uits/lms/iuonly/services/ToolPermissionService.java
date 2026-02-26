package edu.iu.uits.lms.iuonly.services;

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

import edu.iu.uits.lms.iuonly.model.tps.AuthPermission;
import edu.iu.uits.lms.iuonly.model.tps.AuthPermissionProperty;
import edu.iu.uits.lms.iuonly.model.tps.AuthTool;
import edu.iu.uits.lms.iuonly.model.tps.AuthUser;
import edu.iu.uits.lms.iuonly.model.tps.AuthUserInformationDto;
import edu.iu.uits.lms.iuonly.model.tps.AuthUserPermission;
import edu.iu.uits.lms.iuonly.model.tps.AuthUserPermissionProperty;
import edu.iu.uits.lms.iuonly.model.tps.AuthUserPermissionPropertyId;
import edu.iu.uits.lms.iuonly.repository.AuthPermissionRepository;
import edu.iu.uits.lms.iuonly.repository.AuthToolRepository;
import edu.iu.uits.lms.iuonly.repository.AuthUserPermissionPropertyRepository;
import edu.iu.uits.lms.iuonly.repository.AuthUserPermissionRepository;
import edu.iu.uits.lms.iuonly.repository.AuthUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ToolPermissionService {

    @Autowired
    private AuthToolRepository authToolRepository;

    @Autowired
    private AuthPermissionRepository authPermissionRepository;

    @Autowired
    private AuthUserPermissionRepository authUserPermissionRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Autowired
    private AuthUserPermissionPropertyRepository authUserPermissionPropertyRepository;

    /**
     * Get a list of tools that use TPS
     * @return
     */
    public List<AuthTool> getTools() {
        return authToolRepository.findAll();
    }

    /**
     * Get the permissions for a given tool
     * @param toolId
     * @return
     */
    public List<AuthPermission> getPermissionsForTool(Long toolId) {
        return authPermissionRepository.findByAuthToolId(toolId);
    }

    /**
     * Get a permission by id, with the option to include the associated properties
     * @param permissionId
     * @param includeProperties
     * @return
     */
    public AuthPermission getPermissionById(Long permissionId, boolean includeProperties) {
        if (includeProperties) {
            return authPermissionRepository.findByIdWithProperties(permissionId);
        } else {
            return authPermissionRepository.findById(permissionId).orElse(null);
        }
    }

    /**
     * Get a list of AuthUserPermissions associated with a given permission id
     * @param authPermissionId
     * @return
     */
    public List<AuthUserPermission> getUsersWithPermission(Long authPermissionId) {
        return authUserPermissionRepository.findByAuthPermissionId(authPermissionId);
    }

    /**
     * Get an AuthUserPermission by id, with the option to include the associated user properties
     * @param authUserPermissionId
     * @param includeProperties
     * @return
     */
    public AuthUserPermission getAuthUserPermissionById(Long authUserPermissionId, boolean includeProperties) {
        if (includeProperties) {
            return authUserPermissionRepository.findByIdWithUserProperties(authUserPermissionId);
        } else {
            return authUserPermissionRepository.findById(authUserPermissionId).orElse(null);
        }
    }

    /**
     * Get an AuthUser by username
     * @param username
     * @return
     */
    public AuthUser getAuthUserByUsername(String username) {
        return authUserRepository.findByUsername(username);
    }

    /**
     * Save an AuthUser
     * @param authUser
     * @return
     */
    public AuthUser saveAuthUser(AuthUser authUser) {
        return authUserRepository.save(authUser);
    }

    /**
     * Get a user permission by username and permission id, loading the user properties as well
     * @param username
     * @param permissionId
     * @return
     */
    public AuthUserPermission getAuthUserPermissionByUsernameAndPermissionId(String username, Long permissionId) {
        return authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties(username, permissionId);
    }

    /**
     * Save AuthUserPermission
     * @param permission
     * @return
     */
    public AuthUserPermission saveAuthUserPermission(AuthUserPermission permission) {
        return authUserPermissionRepository.save(permission);
    }

    /**
     * Check if a user has a permission by authUserId and permissionId.
     *
     * @param authUserId The ID of the authorized user.
     * @param permissionId The ID of the permission.
     * @return True if the user has the permission, false otherwise.
     */
    public boolean authUserHasPermission(Long authUserId, Long permissionId) {
        return authUserPermissionRepository.existsByAuthUserIdAndAuthPermissionId(authUserId, permissionId);
    }

    /**
     * Save an AuthUserPermission for a given username and permission id.
     * If the user permission already exists, it will be updated.
     * If it does not exist, a new user permission will be created.
     *
     * @param username The username of the user.
     * @param permissionId The ID of the associated permission.
     * @param activePermission The active status of the permission.
     * @param notes Any notes associated with the permission.
     * @param userProperties A list of user properties to associate with the permission (optional).
     * @return The saved AuthUserPermission object.
     */
    public AuthUserPermission saveAuthUserPermissionWithProperties(String username, Long permissionId, boolean activePermission, String notes, List<AuthUserPermissionProperty> userProperties) {
        AuthUser authUser = getAuthUserByUsername(username);
        if (authUser == null) {
            throw new IllegalArgumentException("No AuthUser exists with username " + username + ". Cannot add/update permission.");
        }

        AuthPermission authPermission = getPermissionById(permissionId, true);
        if (authPermission == null) {
            throw new IllegalArgumentException("AuthPermission with id " + permissionId + " does not exist. Cannot add/update permission.");
        }

        AuthUserPermission userPermission = getAuthUserPermissionByUsernameAndPermissionId(username, permissionId);
        boolean isNew = false;
        if (userPermission == null) {
            userPermission = new AuthUserPermission();
            userPermission.setAuthUser(authUser);
            userPermission.setAuthPermission(authPermission);
            isNew = true;
        }

        userPermission.setActive(activePermission);
        userPermission.setNotes(notes);

        if (isNew) {
            userPermission = saveAuthUserPermission(userPermission);
        }

        if (authPermission.hasProperties()) {
            for (AuthPermissionProperty authPermissionProperty : authPermission.getProperties()) {
                AuthUserPermissionProperty existingProperty = null;

                if (userPermission.getUserProperties() != null) {
                    for (AuthUserPermissionProperty existingUserProperty : userPermission.getUserProperties()) {
                        if (existingUserProperty.getAuthPermissionProperty().getId().equals(authPermissionProperty.getId())) {
                            existingProperty = existingUserProperty;
                            break;
                        }
                    }
                }

                if (existingProperty != null) {
                    if (userProperties != null) {
                        for (AuthUserPermissionProperty inputProperty : userProperties) {
                            if (inputProperty.getAuthPermissionProperty().getId().equals(authPermissionProperty.getId())) {
                                existingProperty.setValue(inputProperty.getValue());
                                saveAuthUserPermissionProperty(existingProperty);
                                break;
                            }
                        }
                    }
                } else {
                    AuthUserPermissionProperty userPermissionProperty = new AuthUserPermissionProperty();
                    userPermissionProperty.setAuthUserPermission(userPermission);
                    userPermissionProperty.setAuthPermissionProperty(authPermissionProperty);

                    if (userProperties != null) {
                        for (AuthUserPermissionProperty inputProperty : userProperties) {
                            if (inputProperty.getAuthPermissionProperty().getId().equals(authPermissionProperty.getId())) {
                                userPermissionProperty.setValue(inputProperty.getValue());
                                break;
                            }
                        }
                    }

                    if (userPermission.getUserProperties() == null) {
                        userPermission.setUserProperties(new ArrayList<>());
                    }

                    saveAuthUserPermissionProperty(userPermissionProperty);
                    userPermission.getUserProperties().add(userPermissionProperty);
                }
            }
        }

        return saveAuthUserPermission(userPermission);
    }

    @Transactional(transactionManager = "postgresdbTransactionMgr")
    public AuthUserPermissionProperty saveAuthUserPermissionProperty(AuthUserPermissionProperty property) {
        if (property.getAuthUserPermission() == null || property.getAuthPermissionProperty() == null) {
            throw new IllegalArgumentException("AuthUserPermission and AuthPermissionProperty must not be null.");
        }

        if (property.getAuthUserPermission().getId() == null || property.getAuthPermissionProperty().getId() == null) {
            throw new IllegalArgumentException("AuthUserPermission ID and AuthPermissionProperty ID must not be null.");
        }

        // Ensure the composite key is initialized
        if (property.getId() == null) {
            AuthUserPermissionPropertyId id = new AuthUserPermissionPropertyId();
            id.setAuthUserPermissionId(property.getAuthUserPermission().getId());
            id.setAuthPermissionPropertyId(property.getAuthPermissionProperty().getId());
            property.setId(id);
        }

        return authUserPermissionPropertyRepository.save(property);
    }

    /**
     * Get a list of AuthUserPermission for a given username
     * @param username
     * @return
     */
    public List<AuthUserPermission> getToolPermissionsForUser(String username) {
        AuthUser authUser = authUserRepository.findByUsername(username);
        if (authUser == null) {
            throw new IllegalArgumentException("No AuthUser exists with username " + username + ". Cannot retrieve permissions.");
        }

        return authUserPermissionRepository.findByAuthUserId(authUser.getId());
    }

    /**
     * Get available permissions for a user by excluding associated permissions
     * @param username The username to check
     * @return List of available permissions
     */
    public List<AuthPermission> getAvailablePermissionsForUser(String username) {
        // Fetch associated permission IDs directly from the database
        Set<Long> associatedPermissionIds = authUserPermissionRepository.findPermissionIdsByUsername(username);

        // Fetch only the permissions not associated with the user
        return authPermissionRepository.findPermissionsExcludingIds(associatedPermissionIds);
    }

    /**
     * Get all AuthUserInformationDto, which includes user details and their associated tool permissions, if any.
     * @return
     */
    public List<AuthUserInformationDto> getAllAuthUserInformation() {
        List<AuthUserPermission> permissions = authUserPermissionRepository.findAllWithDetails();

        List<AuthUserInformationDto> usersWithPermissions = permissions.stream()
                .collect(Collectors.groupingBy(AuthUserPermission::getAuthUser))
                .entrySet().stream()
                .map(entry -> {
                    var user = entry.getKey();
                    List<String> tools = entry.getValue().stream()
                            .map(aup -> aup.getAuthPermission().getAuthTool().getName())
                            .distinct()
                            .sorted()
                            .collect(Collectors.toList());
                    return new AuthUserInformationDto(
                            user.getId(),
                            user.getUsername(),
                            user.getDisplayName(),
                            user.getEmail(),
                            tools,
                            user.isActive()
                    );
                })
                .collect(Collectors.toList());

        // Add users without permissions
        List<AuthUser> usersWithoutPermissions = authUserRepository.findAllWithoutPermissions();
        List<AuthUserInformationDto> usersWithoutPermissionsDto = usersWithoutPermissions.stream()
                .map(user -> new AuthUserInformationDto(
                        user.getId(),
                        user.getUsername(),
                        user.getDisplayName(),
                        user.getEmail(),
                        new ArrayList<>(),
                        user.isActive()
                ))
                .collect(Collectors.toList());

        usersWithPermissions.addAll(usersWithoutPermissionsDto);

        return usersWithPermissions;
    }

    public List<AuthPermission> getAllPermissionsWithTools() {
        return authPermissionRepository.findAllWithAuthTool();
    }

    /**
     * Check if the user has an active permission with the given property key
     * @param username
     * @param propertyKey
     * @return
     */
    public boolean isAuthorized(String username, String propertyKey) {
        AuthUser authUser = authUserRepository.findByUsername(username);
        if (authUser == null || !authUser.isActive()) {
            return false;
        }

        List<AuthUserPermission> userPermissions = authUserPermissionRepository.findByAuthUserId(authUser.getId());
        for (AuthUserPermission userPermission : userPermissions) {
            if (userPermission.isActive()) {
                for (AuthUserPermissionProperty userProperty : userPermission.getUserProperties()) {
                    if (userProperty.getAuthPermissionProperty().getKey().equals(propertyKey)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean userExists(String username) {
        return authUserRepository.existsByUsername(username);
    }

}
