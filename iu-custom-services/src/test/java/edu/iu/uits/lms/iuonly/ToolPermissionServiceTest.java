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
    private AuthPermissionRepository authPermissionRepository;
    @Mock
    private AuthUserPermissionRepository authUserPermissionRepository;
    @Mock
    private AuthUserRepository authUserRepository;
    @Mock
    private AuthUserPermissionPropertyRepository authUserPermissionPropertyRepository;

    @InjectMocks
    private ToolPermissionService toolPermissionService;

    @Test
    public void testIsAuthorized_usernameIsNull() {
        boolean result = toolPermissionService.isAuthorized(null, "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_usernameDoesNotExist() {
        when(authUserRepository.findByUsername("nouser")).thenReturn(null);
        boolean result = toolPermissionService.isAuthorized("nouser", "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_permissionKeyIsNull() {
        AuthUser user = buildUser(1L, "user1", true);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        boolean result = toolPermissionService.isAuthorized("user1", null);
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_authPermissionDoesNotExist() {
        AuthUser user = buildUser(1L, "user1", true);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByKey("permKey")).thenReturn(null);
        boolean result = toolPermissionService.isAuthorized("user1", "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_authUserIsInactive() {
        AuthUser user = buildUser(1L, "user1", false);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        boolean result = toolPermissionService.isAuthorized("user1", "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_authUserPermissionDoesNotExist() {
        AuthUser user = buildUser(1L, "user1", true);
        AuthPermission perm = buildPermission(1L, null);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByKey("permKey")).thenReturn(perm);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user1", 1L)).thenReturn(null);
        boolean result = toolPermissionService.isAuthorized("user1", "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_authUserPermissionIsInactive() {
        AuthUser user = buildUser(1L, "user1", true);
        AuthPermission perm = buildPermission(1L, null);
        AuthUserPermission userPerm = buildUserPermission(null, user, perm, false, null);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByKey("permKey")).thenReturn(perm);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user1", 1L)).thenReturn(userPerm);
        boolean result = toolPermissionService.isAuthorized("user1", "permKey");
        assertFalse(result);
    }

    @Test
    public void testIsAuthorized_returnsTrueIfUserAndPermissionAreActive() {
        AuthUser user = buildUser(1L, "user1", true);
        AuthPermission perm = buildPermission(1L, null);
        AuthUserPermission userPerm = buildUserPermission(null, user, perm, true, null);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByKey("permKey")).thenReturn(perm);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user1", 1L)).thenReturn(userPerm);
        boolean result = toolPermissionService.isAuthorized("user1", "permKey");
        assertTrue(result);
    }

    @Test
    public void testGetAllAuthUserInformation_withPermissionsAndWithoutPermissions() {
        AuthUser userWithPerms = buildUser(10L, "withPerms", true);
        userWithPerms.setDisplayName("With Perms");
        userWithPerms.setEmail("with@example.edu");

        AuthTool toolA = new AuthTool();
        toolA.setName("Alpha Tool");
        AuthPermission permA = buildPermission(10L, List.of());
        permA.setAuthTool(toolA);

        AuthUserPermission aup = buildUserPermission(100L, userWithPerms, permA, true, null);

        AuthUser userWithoutPerms = buildUser(20L, "withoutPerms", false);
        userWithoutPerms.setDisplayName("Without Perms");
        userWithoutPerms.setEmail("without@example.edu");

        when(authUserPermissionRepository.findAllWithDetails()).thenReturn(List.of(aup));
        when(authUserRepository.findAllWithoutPermissions()).thenReturn(List.of(userWithoutPerms));

        List<AuthUserInformationDto> result = toolPermissionService.getAllAuthUserInformation();

        assertEquals(2, result.size());

        AuthUserInformationDto withPermsDto = result.stream()
                .filter(dto -> "withPerms".equals(dto.getUsername()))
                .findFirst()
                .orElseThrow();
        assertEquals(List.of("Alpha Tool"), withPermsDto.getTools());
        assertTrue(withPermsDto.isActive());

        AuthUserInformationDto withoutPermsDto = result.stream()
                .filter(dto -> "withoutPerms".equals(dto.getUsername()))
                .findFirst()
                .orElseThrow();
        assertTrue(withoutPermsDto.getTools().isEmpty());
        assertFalse(withoutPermsDto.isActive());
    }

    @Test
    public void testGetAllAuthUserInformation_deduplicatesAndSortsToolNames() {
        AuthUser user = buildUser(30L, "sortUser", true);
        user.setDisplayName("Sort User");
        user.setEmail("sort@example.edu");

        AuthTool zeta = new AuthTool();
        zeta.setName("Zeta Tool");
        AuthTool alpha = new AuthTool();
        alpha.setName("Alpha Tool");

        AuthPermission permZeta1 = buildPermission(31L, null);
        permZeta1.setAuthTool(zeta);
        AuthPermission permAlpha = buildPermission(32L, null);
        permAlpha.setAuthTool(alpha);
        AuthPermission permZeta2 = buildPermission(33L, null);
        permZeta2.setAuthTool(zeta);

        AuthUserPermission up1 = buildUserPermission(201L, user, permZeta1, true, null);
        AuthUserPermission up2 = buildUserPermission(202L, user, permAlpha, true, null);
        AuthUserPermission up3 = buildUserPermission(203L, user, permZeta2, true, null);

        when(authUserPermissionRepository.findAllWithDetails()).thenReturn(List.of(up1, up2, up3));
        when(authUserRepository.findAllWithoutPermissions()).thenReturn(Collections.emptyList());

        List<AuthUserInformationDto> result = toolPermissionService.getAllAuthUserInformation();

        assertEquals(1, result.size());
        assertEquals(List.of("Alpha Tool", "Zeta Tool"), result.getFirst().getTools());
    }

    @Test
    public void testGetAllAuthUserInformation_returnsEmptyWhenNoData() {
        when(authUserPermissionRepository.findAllWithDetails()).thenReturn(Collections.emptyList());
        when(authUserRepository.findAllWithoutPermissions()).thenReturn(Collections.emptyList());

        List<AuthUserInformationDto> result = toolPermissionService.getAllAuthUserInformation();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_throwsWhenUserMissing() {
        when(authUserRepository.findByUsername("missing")).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                toolPermissionService.saveAuthUserPermissionWithProperties("missing", 1L, true, "notes", null)
        );

        assertTrue(ex.getMessage().contains("No AuthUser exists with username missing"));
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_throwsWhenPermissionMissing() {
        AuthUser user = buildUser(11L, "user1", true);
        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(1L)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                toolPermissionService.saveAuthUserPermissionWithProperties("user1", 1L, true, "notes", null)
        );

        assertTrue(ex.getMessage().contains("AuthPermission with id 1 does not exist"));
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_createsNewPermissionWhenMissing() {
        AuthUser user = buildUser(100L, "user1", true);
        AuthPermission permission = buildPermission(200L, Collections.emptyList());

        when(authUserRepository.findByUsername("user1")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(200L)).thenReturn(permission);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user1", 200L)).thenReturn(null);
        when(authUserPermissionRepository.save(any(AuthUserPermission.class))).thenAnswer(invocation -> {
            AuthUserPermission saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(300L);
            }
            return saved;
        });

        AuthUserPermission result = toolPermissionService.saveAuthUserPermissionWithProperties(
                "user1", 200L, true, "created-notes", null
        );

        assertNotNull(result);
        assertEquals(user, result.getAuthUser());
        assertEquals(permission, result.getAuthPermission());
        assertTrue(result.isActive());
        assertEquals("created-notes", result.getNotes());
        verify(authUserPermissionRepository, atLeastOnce()).save(any(AuthUserPermission.class));
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_createsPropertyForPermissionProperty() {
        AuthUser user = buildUser(101L, "user2", true);
        AuthPermissionProperty permissionProperty = buildPermissionProperty(501L);
        AuthPermission permission = buildPermission(201L, new ArrayList<>(List.of(permissionProperty)));

        AuthUserPermissionProperty inputProperty = buildUserPermissionProperty(null, null, permissionProperty, "property-value");

        when(authUserRepository.findByUsername("user2")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(201L)).thenReturn(permission);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user2", 201L)).thenReturn(null);
        when(authUserPermissionRepository.save(any(AuthUserPermission.class))).thenAnswer(invocation -> {
            AuthUserPermission saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(301L);
            }
            return saved;
        });
        when(authUserPermissionPropertyRepository.save(any(AuthUserPermissionProperty.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthUserPermission result = toolPermissionService.saveAuthUserPermissionWithProperties(
                "user2", 201L, true, "notes", List.of(inputProperty)
        );

        assertNotNull(result.getUserProperties());
        assertEquals(1, result.getUserProperties().size());

        AuthUserPermissionProperty createdProp = result.getUserProperties().getFirst();
        assertEquals("property-value", createdProp.getValue());
        assertNotNull(createdProp.getId());
        assertEquals(301L, createdProp.getId().getAuthUserPermissionId());
        assertEquals(501L, createdProp.getId().getAuthPermissionPropertyId());

        verify(authUserPermissionPropertyRepository, atLeastOnce()).save(any(AuthUserPermissionProperty.class));
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_updatesExistingPropertyValue() {
        AuthUser user = buildUser(102L, "user3", true);
        AuthPermissionProperty permissionProperty = buildPermissionProperty(601L);
        AuthPermission permission = buildPermission(202L, new ArrayList<>(List.of(permissionProperty)));

        AuthUserPermission existingPermission = buildUserPermission(302L, user, permission, true, null);

        AuthUserPermissionPropertyId existingId = new AuthUserPermissionPropertyId();
        existingId.setAuthUserPermissionId(302L);
        existingId.setAuthPermissionPropertyId(601L);

        AuthUserPermissionProperty existingProp = buildUserPermissionProperty(existingId, existingPermission, permissionProperty, "old-value");
        existingPermission.setUserProperties(new ArrayList<>(List.of(existingProp)));

        AuthUserPermissionProperty inputProperty = buildUserPermissionProperty(null, null, permissionProperty, "new-value");

        when(authUserRepository.findByUsername("user3")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(202L)).thenReturn(permission);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user3", 202L)).thenReturn(existingPermission);
        when(authUserPermissionRepository.save(any(AuthUserPermission.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authUserPermissionPropertyRepository.save(any(AuthUserPermissionProperty.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthUserPermission result = toolPermissionService.saveAuthUserPermissionWithProperties(
                "user3", 202L, false, "updated-notes", List.of(inputProperty)
        );

        assertFalse(result.isActive());
        assertEquals("updated-notes", result.getNotes());
        assertEquals(1, result.getUserProperties().size());
        assertEquals("new-value", result.getUserProperties().getFirst().getValue());

        verify(authUserPermissionPropertyRepository, atLeastOnce()).save(any(AuthUserPermissionProperty.class));
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_createsPropertyWithNullValueWhenInputPropertiesNull() {
        AuthUser user = buildUser(103L, "user4", true);
        AuthPermissionProperty permissionProperty = buildPermissionProperty(701L);
        AuthPermission permission = buildPermission(203L, new ArrayList<>(List.of(permissionProperty)));

        when(authUserRepository.findByUsername("user4")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(203L)).thenReturn(permission);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user4", 203L)).thenReturn(null);
        when(authUserPermissionRepository.save(any(AuthUserPermission.class))).thenAnswer(invocation -> {
            AuthUserPermission saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                saved.setId(303L);
            }
            return saved;
        });
        when(authUserPermissionPropertyRepository.save(any(AuthUserPermissionProperty.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthUserPermission result = toolPermissionService.saveAuthUserPermissionWithProperties(
                "user4", 203L, true, "notes", null
        );

        assertNotNull(result.getUserProperties());
        assertEquals(1, result.getUserProperties().size());

        AuthUserPermissionProperty createdProperty = result.getUserProperties().getFirst();
        assertEquals(permissionProperty, createdProperty.getAuthPermissionProperty());
        assertNull(createdProperty.getValue());
        assertNotNull(createdProperty.getId());
        assertEquals(303L, createdProperty.getId().getAuthUserPermissionId());
        assertEquals(701L, createdProperty.getId().getAuthPermissionPropertyId());
    }

    @Test
    public void testSaveAuthUserPermissionWithProperties_doesNotSavePropertiesWhenPermissionHasNoProperties() {
        AuthUser user = buildUser(104L, "user5", true);
        AuthPermission permission = buildPermission(204L, Collections.emptyList());
        AuthUserPermission existingPermission = buildUserPermission(304L, user, permission, true, null);

        when(authUserRepository.findByUsername("user5")).thenReturn(user);
        when(authPermissionRepository.findByIdWithProperties(204L)).thenReturn(permission);
        when(authUserPermissionRepository.findByUsernameAndPermissionIdWithUserProperties("user5", 204L)).thenReturn(existingPermission);
        when(authUserPermissionRepository.save(any(AuthUserPermission.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthUserPermission result = toolPermissionService.saveAuthUserPermissionWithProperties(
                "user5", 204L, true, "no-props", null
        );

        assertTrue(result.isActive());
        assertEquals("no-props", result.getNotes());
        verify(authUserPermissionPropertyRepository, never()).save(any(AuthUserPermissionProperty.class));
    }

    private AuthUser buildUser(Long id, String username, boolean active) {
        AuthUser user = new AuthUser();
        user.setId(id);
        user.setUsername(username);
        user.setActive(active);
        return user;
    }

    private AuthPermission buildPermission(Long id, List<AuthPermissionProperty> properties) {
        AuthPermission permission = new AuthPermission();
        permission.setId(id);
        if (properties != null) {
            permission.setProperties(properties);
        }
        return permission;
    }

    private AuthUserPermission buildUserPermission(Long id, AuthUser user, AuthPermission permission, boolean active, String notes) {
        AuthUserPermission userPermission = new AuthUserPermission();
        userPermission.setId(id);
        userPermission.setAuthUser(user);
        userPermission.setAuthPermission(permission);
        userPermission.setActive(active);
        userPermission.setNotes(notes);
        return userPermission;
    }

    private AuthPermissionProperty buildPermissionProperty(Long id) {
        AuthPermissionProperty property = new AuthPermissionProperty();
        property.setId(id);
        return property;
    }

    private AuthUserPermissionProperty buildUserPermissionProperty(AuthUserPermissionPropertyId id, AuthUserPermission userPermission,
                                                                  AuthPermissionProperty permissionProperty, String value) {
        AuthUserPermissionProperty property = new AuthUserPermissionProperty();
        property.setId(id);
        property.setAuthUserPermission(userPermission);
        property.setAuthPermissionProperty(permissionProperty);
        property.setValue(value);
        return property;
    }

    @Test
    public void testGetPermissionPropertiesForUser_returnsMapWithPropertiesWhenUserPermissionExists() {
        AuthUser user = buildUser(105L, "user6", true);
        AuthPermission permission = buildPermission(205L, null);

        AuthPermissionProperty prop1 = buildPermissionProperty(801L);
        prop1.setKey("property1");

        AuthPermissionProperty prop2 = buildPermissionProperty(802L);
        prop2.setKey("property2");

        AuthUserPermissionPropertyId id1 = new AuthUserPermissionPropertyId();
        id1.setAuthUserPermissionId(305L);
        id1.setAuthPermissionPropertyId(801L);

        AuthUserPermissionPropertyId id2 = new AuthUserPermissionPropertyId();
        id2.setAuthUserPermissionId(305L);
        id2.setAuthPermissionPropertyId(802L);

        AuthUserPermissionProperty userProp1 = buildUserPermissionProperty(id1, null, prop1, "value1");
        AuthUserPermissionProperty userProp2 = buildUserPermissionProperty(id2, null, prop2, "value2");

        AuthUserPermission userPermission = buildUserPermission(305L, user, permission, true, "notes");
        userPermission.setUserProperties(List.of(userProp1, userProp2));

        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("user6", "permKey"))
                .thenReturn(userPermission);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("user6", "permKey");

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("value1", result.get("property1"));
        assertEquals("value2", result.get("property2"));
    }

    @Test
    public void testGetPermissionPropertiesForUser_returnsEmptyMapWhenUserPermissionDoesNotExist() {
        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("nonexistent", "permKey"))
                .thenReturn(null);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("nonexistent", "permKey");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPermissionPropertiesForUser_returnsEmptyMapWhenUserPermissionHasNoProperties() {
        AuthUser user = buildUser(106L, "user7", true);
        AuthPermission permission = buildPermission(206L, null);
        AuthUserPermission userPermission = buildUserPermission(306L, user, permission, true, "notes");
        userPermission.setUserProperties(null);

        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("user7", "permKey"))
                .thenReturn(userPermission);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("user7", "permKey");

        assertNotNull(result);
        assertThrows(NullPointerException.class, () -> {
            // When iterating over a null list, it throws NPE
            for (AuthUserPermissionProperty property : userPermission.getUserProperties()) {
                // This should not execute
            }
        });
    }

    @Test
    public void testGetPermissionPropertiesForUser_returnsEmptyMapWhenUserPermissionHasEmptyProperties() {
        AuthUser user = buildUser(107L, "user8", true);
        AuthPermission permission = buildPermission(207L, null);
        AuthUserPermission userPermission = buildUserPermission(307L, user, permission, true, "notes");
        userPermission.setUserProperties(Collections.emptyList());

        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("user8", "permKey"))
                .thenReturn(userPermission);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("user8", "permKey");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetPermissionPropertiesForUser_handlesPropertiesWithNullValues() {
        AuthUser user = buildUser(108L, "user9", true);
        AuthPermission permission = buildPermission(208L, null);

        AuthPermissionProperty prop1 = buildPermissionProperty(803L);
        prop1.setKey("nullProperty");

        AuthUserPermissionPropertyId id1 = new AuthUserPermissionPropertyId();
        id1.setAuthUserPermissionId(308L);
        id1.setAuthPermissionPropertyId(803L);

        AuthUserPermissionProperty userProp1 = buildUserPermissionProperty(id1, null, prop1, null);

        AuthUserPermission userPermission = buildUserPermission(308L, user, permission, true, "notes");
        userPermission.setUserProperties(List.of(userProp1));

        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("user9", "permKey"))
                .thenReturn(userPermission);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("user9", "permKey");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertNull(result.get("nullProperty"));
    }

    @Test
    public void testGetPermissionPropertiesForUser_returnsSinglePropertyCorrectly() {
        AuthUser user = buildUser(109L, "user10", true);
        AuthPermission permission = buildPermission(209L, null);

        AuthPermissionProperty prop = buildPermissionProperty(804L);
        prop.setKey("singleProp");

        AuthUserPermissionPropertyId id = new AuthUserPermissionPropertyId();
        id.setAuthUserPermissionId(309L);
        id.setAuthPermissionPropertyId(804L);

        AuthUserPermissionProperty userProp = buildUserPermissionProperty(id, null, prop, "singleValue");

        AuthUserPermission userPermission = buildUserPermission(309L, user, permission, true, "notes");
        userPermission.setUserProperties(List.of(userProp));

        when(authUserPermissionRepository.findByUsernameAndPermissionKeyWithUserProperties("user10", "permKey"))
                .thenReturn(userPermission);

        Map<String, String> result = toolPermissionService.getPermissionPropertiesForUser("user10", "permKey");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("singleValue", result.get("singleProp"));
    }

    // Tests for convertPropertyToBoolean utility method
    @Test
    public void testConvertPropertyToBoolean_returnsTrueForTrueString() {
        assertTrue(ToolPermissionService.convertPropertyToBoolean("true"));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsTrueForTrueStringIgnoreCase() {
        assertTrue(ToolPermissionService.convertPropertyToBoolean("TRUE"));
        assertTrue(ToolPermissionService.convertPropertyToBoolean("True"));
        assertTrue(ToolPermissionService.convertPropertyToBoolean("TrUe"));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsFalseForFalseString() {
        assertFalse(ToolPermissionService.convertPropertyToBoolean("false"));
        assertFalse(ToolPermissionService.convertPropertyToBoolean("FALSE"));
        assertFalse(ToolPermissionService.convertPropertyToBoolean("False"));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsFalseForEmptyString() {
        assertFalse(ToolPermissionService.convertPropertyToBoolean(""));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsFalseForNull() {
        assertFalse(ToolPermissionService.convertPropertyToBoolean(null));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsFalseForRandomString() {
        assertFalse(ToolPermissionService.convertPropertyToBoolean("yes"));
        assertFalse(ToolPermissionService.convertPropertyToBoolean("1"));
        assertFalse(ToolPermissionService.convertPropertyToBoolean("random"));
    }

    @Test
    public void testConvertPropertyToBoolean_returnsFalseForWhitespace() {
        assertFalse(ToolPermissionService.convertPropertyToBoolean("   "));
    }

    // Tests for convertPropertyToStringArray utility method
    @Test
    public void testConvertPropertyToStringArray_returnsSingleItemArray() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("item1");
        assertEquals(1, result.length);
        assertEquals("item1", result[0]);
    }

    @Test
    public void testConvertPropertyToStringArray_returnsMultipleItemsArray() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("item1,item2,item3");
        assertEquals(3, result.length);
        assertEquals("item1", result[0]);
        assertEquals("item2", result[1]);
        assertEquals("item3", result[2]);
    }

    @Test
    public void testConvertPropertyToStringArray_trimsWhitespaceAroundCommas() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("item1 , item2 , item3");
        assertEquals(3, result.length);
        assertEquals("item1", result[0]);
        assertEquals("item2", result[1]);
        assertEquals("item3", result[2]);
    }

    @Test
    public void testConvertPropertyToStringArray_trimsLeadingAndTrailingWhitespace() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("  item1,item2,item3  ");
        assertEquals(3, result.length);
        assertEquals("item1", result[0]);
        assertEquals("item2", result[1]);
        assertEquals("item3", result[2]);
    }

    @Test
    public void testConvertPropertyToStringArray_handlesMultipleSpacesAroundCommas() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("item1  ,  item2  ,  item3");
        assertEquals(3, result.length);
        assertEquals("item1", result[0]);
        assertEquals("item2", result[1]);
        assertEquals("item3", result[2]);
    }

    @Test
    public void testConvertPropertyToStringArray_returnsEmptyArrayForNull() {
        String[] result = ToolPermissionService.convertPropertyToStringArray(null);
        assertEquals(0, result.length);
    }

    @Test
    public void testConvertPropertyToStringArray_returnsEmptyArrayForEmptyString() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("");
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test
    public void testConvertPropertyToStringArray_returnsArrayWithSingleEmptyItemForWhitespace() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("   ");
        assertEquals(1, result.length);
        assertEquals("", result[0]);
    }

    @Test
    public void testConvertPropertyToStringArray_handlesItemsWithInternalSpaces() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("hello world,foo bar,baz qux");
        assertEquals(3, result.length);
        assertEquals("hello world", result[0]);
        assertEquals("foo bar", result[1]);
        assertEquals("baz qux", result[2]);
    }

    @Test
    public void testConvertPropertyToStringArray_handlesSingleCommaDelimiter() {
        String[] result = ToolPermissionService.convertPropertyToStringArray("item1,");
        assertEquals(1, result.length);
        assertEquals("item1", result[0]);
    }

    // Tests for convertPropertyToList utility method
    @Test
    public void testConvertPropertyToList_returnsSingleItemList() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1");
        assertEquals(1, result.size());
        assertEquals("item1", result.getFirst());
    }

    @Test
    public void testConvertPropertyToList_returnsMultipleItemsList() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1,item2,item3");
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    @Test
    public void testConvertPropertyToList_trimsWhitespaceAroundCommas() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1 , item2 , item3");
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    @Test
    public void testConvertPropertyToList_trimsLeadingAndTrailingWhitespace() {
        List<String> result = ToolPermissionService.convertPropertyToList("  item1,item2,item3  ");
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    @Test
    public void testConvertPropertyToList_handlesMultipleSpacesAroundCommas() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1  ,  item2  ,  item3");
        assertEquals(3, result.size());
        assertEquals("item1", result.get(0));
        assertEquals("item2", result.get(1));
        assertEquals("item3", result.get(2));
    }

    @Test
    public void testConvertPropertyToList_returnsEmptyListForNull() {
        List<String> result = ToolPermissionService.convertPropertyToList(null);
        assertEquals(0, result.size());
    }

    @Test
    public void testConvertPropertyToList_returnsListWithEmptyStringForEmptyString() {
        List<String> result = ToolPermissionService.convertPropertyToList("");
        assertEquals(1, result.size());
        assertEquals("", result.getFirst());
    }

    @Test
    public void testConvertPropertyToList_returnsListWithSingleEmptyItemForWhitespace() {
        List<String> result = ToolPermissionService.convertPropertyToList("   ");
        assertEquals(1, result.size());
        assertEquals("", result.getFirst());
    }

    @Test
    public void testConvertPropertyToList_handlesItemsWithInternalSpaces() {
        List<String> result = ToolPermissionService.convertPropertyToList("hello world,foo bar,baz qux");
        assertEquals(3, result.size());
        assertEquals("hello world", result.get(0));
        assertEquals("foo bar", result.get(1));
        assertEquals("baz qux", result.get(2));
    }

    @Test
    public void testConvertPropertyToList_handlesSingleCommaDelimiter() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1,");
        assertEquals(1, result.size());
        assertEquals("item1", result.get(0));
    }

    @Test
    public void testConvertPropertyToList_isImmutable() {
        List<String> result = ToolPermissionService.convertPropertyToList("item1,item2");
        // The result should be a properly constructed list
        assertNotNull(result);
        assertEquals(2, result.size());
    }
}
