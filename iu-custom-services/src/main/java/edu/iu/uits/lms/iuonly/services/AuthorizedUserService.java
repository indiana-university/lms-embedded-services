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

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import edu.iu.uits.lms.iuonly.repository.AuthorizedUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorizedUserService {

    @Autowired
    private AuthorizedUserRepository authorizedUserRepository;

    /**
     * Find the AuthorizedUser with the given username
     *
     * @param username
     * @return
     */
    public AuthorizedUser findByUsername(String username) {
        return authorizedUserRepository.findByUsername(username);
    }

    /**
     * Find the active AuthorizedUser with the given username and toolPermission
     * @param username
     * @param toolPermission
     * @return
     */
    public AuthorizedUser findByActiveUsernameAndToolPermission(String username, String toolPermission) {
        return authorizedUserRepository.findByActiveUsernameAndToolPermission(username, toolPermission);
    }

    /**
     * Find the active AuthorizedUser with the given canvasUserId and toolPermission
     * @param canvasUserId
     * @param toolPermission
     * @return
     */
    public AuthorizedUser findByActiveCanvasUserIdAndToolPermission(String canvasUserId, String toolPermission) {
        return authorizedUserRepository.findByActiveCanvasUserIdAndToolPermission(canvasUserId, toolPermission);
    }

    /**
     * Find all the active AuthorizedUser records that have the given toolPermission
     * @param toolPermission
     * @return
     */
    public List<AuthorizedUser> findActiveUsersByPermission(String toolPermission) {
        return authorizedUserRepository.findByActiveToolPermission(toolPermission);
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

    /**
     * Get all AuthorizedUsers associated with the given permission name
     * @param permissionName
     * @return
     */
    public List<AuthorizedUser> getUsersWithPermission(String permissionName) {
        return authorizedUserRepository.findByToolPermission(permissionName);
    }

    public List<AuthorizedUser> getAllAuthorizedUsers() {
        return authorizedUserRepository.findAll();
    }

    public AuthorizedUser createOrUpdateAuthorizedUser(AuthorizedUser authorizedUser) {
       return authorizedUserRepository.save(authorizedUser);
    }

}
