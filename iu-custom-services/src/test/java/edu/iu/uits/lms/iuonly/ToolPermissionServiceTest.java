package edu.iu.uits.lms.iuonly;

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

import edu.iu.uits.lms.iuonly.model.tps.*;
import edu.iu.uits.lms.iuonly.repository.*;
import edu.iu.uits.lms.iuonly.services.ToolPermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ToolPermissionServiceTest {
    @Mock
    private AuthToolRepository authToolRepository;
    @Mock
    private AuthPermissionRepository authPermissionRepository;
    @Mock
    private AuthUserPermissionRepository authUserPermissionRepository;
    @Mock
    private AuthUserRepository authUserRepository;

    @InjectMocks
    private ToolPermissionService toolPermissionService;

    @Test
    void testGetTools() {
        List<AuthTool> tools = Arrays.asList(new AuthTool(), new AuthTool());
        when(authToolRepository.findAll()).thenReturn(tools);
        List<AuthTool> result = toolPermissionService.getTools();
        assertEquals(2, result.size());
        verify(authToolRepository).findAll();
    }

    @Test
    void testGetPermissionsForTool() {
        Long toolId = 1L;
        List<AuthPermission> permissions = Arrays.asList(new AuthPermission(), new AuthPermission());
        when(authPermissionRepository.findByAuthToolId(toolId)).thenReturn(permissions);
        List<AuthPermission> result = toolPermissionService.getPermissionsForTool(toolId);
        assertEquals(2, result.size());
        verify(authPermissionRepository).findByAuthToolId(toolId);
    }

    @Test
    void testGetPermissionById_includePropertiesFalse() {
        Long permissionId = 1L;
        AuthPermission permission = new AuthPermission();
        when(authPermissionRepository.findById(permissionId)).thenReturn(Optional.of(permission));
        AuthPermission result = toolPermissionService.getPermissionById(permissionId, false);
        assertEquals(permission, result);
    }

    @Test
    void testGetPermissionById_includePropertiesTrue() {
        Long permissionId = 1L;
        AuthPermission permission = new AuthPermission();
        when(authPermissionRepository.findByIdWithProperties(permissionId)).thenReturn(permission);
        AuthPermission result = toolPermissionService.getPermissionById(permissionId, true);
        assertEquals(permission, result);
    }

    @Test
    void testGetAuthUserByUsername_found() {
        String username = "user1";
        AuthUser user = new AuthUser();
        when(authUserRepository.findByUsername(username)).thenReturn(user);
        AuthUser result = toolPermissionService.getAuthUserByUsername(username);
        assertEquals(user, result);
    }

    @Test
    void testGetAuthUserByUsername_notFound() {
        String username = "user2";
        when(authUserRepository.findByUsername(username)).thenReturn(null);
        AuthUser result = toolPermissionService.getAuthUserByUsername(username);
        assertNull(result);
    }

    @Test
    void testSaveAuthUser() {
        AuthUser user = new AuthUser();
        when(authUserRepository.save(user)).thenReturn(user);
        AuthUser result = toolPermissionService.saveAuthUser(user);
        assertEquals(user, result);
        verify(authUserRepository).save(user);
    }

    @Test
    void testAuthUserHasPermission_true() {
        Long userId = 1L;
        Long permissionId = 2L;
        when(authUserPermissionRepository.existsByAuthUserIdAndAuthPermissionId(userId, permissionId)).thenReturn(true);
        assertTrue(toolPermissionService.authUserHasPermission(userId, permissionId));
    }

    @Test
    void testAuthUserHasPermission_false() {
        Long userId = 1L;
        Long permissionId = 2L;
        when(authUserPermissionRepository.existsByAuthUserIdAndAuthPermissionId(userId, permissionId)).thenReturn(false);
        assertFalse(toolPermissionService.authUserHasPermission(userId, permissionId));
    }

    @Test
    void testUserExists_true() {
        String username = "user1";
        when(authUserRepository.existsByUsername(username)).thenReturn(true);
        assertTrue(toolPermissionService.userExists(username));
    }

    @Test
    void testUserExists_false() {
        String username = "user2";
        when(authUserRepository.existsByUsername(username)).thenReturn(false);
        assertFalse(toolPermissionService.userExists(username));
    }

    @Test
    void testGetUsersWithPermission() {
        Long permissionId = 1L;
        List<AuthUserPermission> perms = Arrays.asList(new AuthUserPermission(), new AuthUserPermission());
        when(authUserPermissionRepository.findByAuthPermissionId(permissionId)).thenReturn(perms);
        List<AuthUserPermission> result = toolPermissionService.getUsersWithPermission(permissionId);
        assertEquals(2, result.size());
    }

    @Test
    void testIsAuthorized_true() {
        String username = "user1";
        String propertyKey = "key1";
        AuthUser user = mock(AuthUser.class);
        when(user.isActive()).thenReturn(true);
        when(authUserRepository.findByUsername(username)).thenReturn(user);
        AuthUserPermissionProperty property = mock(AuthUserPermissionProperty.class);
        AuthPermissionProperty permProp = mock(AuthPermissionProperty.class);
        when(permProp.getKey()).thenReturn(propertyKey);
        when(property.getAuthPermissionProperty()).thenReturn(permProp);
        AuthUserPermission userPerm = mock(AuthUserPermission.class);
        when(userPerm.isActive()).thenReturn(true);
        when(userPerm.getUserProperties()).thenReturn(List.of(property));
        when(authUserPermissionRepository.findByAuthUserId(anyLong())).thenReturn(List.of(userPerm));
        when(user.getId()).thenReturn(1L);
        assertTrue(toolPermissionService.isAuthorized(username, propertyKey));
    }

    @Test
    void testIsAuthorized_false_noUser() {
        String username = "nouser";
        String propertyKey = "key1";
        when(authUserRepository.findByUsername(username)).thenReturn(null);
        assertFalse(toolPermissionService.isAuthorized(username, propertyKey));
    }

    @Test
    void testIsAuthorized_false_inactiveUser() {
        String username = "user1";
        String propertyKey = "key1";
        AuthUser user = mock(AuthUser.class);
        when(user.isActive()).thenReturn(false);
        when(authUserRepository.findByUsername(username)).thenReturn(user);
        assertFalse(toolPermissionService.isAuthorized(username, propertyKey));
    }
}
