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

import edu.iu.uits.lms.iuonly.model.tps.AuthUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AuthorizedUserService {

    @Autowired
    private ToolPermissionService toolPermissionService;

    /**
     *
     * @param userId
     * @param permissionKey
     * @return true if the given user is authorized for the given permission in TPS, false otherwise.
     */
    public boolean isAuthorized(String userId, String permissionKey) {
        return toolPermissionService.isAuthorized(userId, permissionKey);
    }

    /**
     *
     * @param username
     * @return the AuthUser record associated with the given username. Use this method for general information
     * about the AuthUser. If you need to check a specific permission for this user, use {@link #isAuthorized(String, String)}
     */
    public AuthUser findByUsername(String username) {
        return toolPermissionService.getAuthUserByUsername(username);
    }

    /**
     *
     * @param permissionKey
     * @return a list of AuthUser objects representing the active users who are authorized for the given permissionKey in TPS.
     *
     */
    public List<AuthUser> findActiveUsersByPermission(String permissionKey) {
        return toolPermissionService.getAuthUsersByPermissionKey(permissionKey, false);
    }

    /**
     *
     * @param userId
     * @param permissionKey
     * @return a map of permission properties associated with the given user and permissionKey in TPS.
     * Returns an empty map if the user is not authorized for the given permission or if there are no properties defined for that user and permission.
     */
    public Map<String, String> getPermissionPropertiesForUser(String userId, String permissionKey) {
        return toolPermissionService.getPermissionPropertiesForUser(userId, permissionKey);
    }

    /**
     * Convert the input to a boolean.  Does a case-insensitive compare to "true".  Anything that doesn't match is false.
     * @param propertyValue
     * @return
     */
    public static boolean convertPropertyToBoolean(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }

    /**
     * Convert the input it a String[].  Trims any leading/trailing spaces for the entire input value, as well as each
     * individual item in the list.  Expects items to be comma-delimited.
     * @param propertyValue
     * @return
     */
    public static String[] convertPropertyToStringArray(String propertyValue) {
        if (propertyValue == null) {
            return new String[]{};
        }
        //Split on the comma and trim all white space
        return propertyValue.trim().split("\\s*,\\s*");
    }

    /**
     * Convert the input it a List<String>.  Trims any leading/trailing spaces for the entire input value, as well as each
     * individual item in the list.  Expects items to be comma-delimited.
     * @param propertyValue
     * @return
     */
    public static List<String> convertPropertyToList(String propertyValue) {
        return Arrays.stream(convertPropertyToStringArray(propertyValue))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
