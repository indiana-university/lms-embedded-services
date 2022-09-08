package edu.iu.uits.lms.common.variablereplacement;

/*-
 * #%L
 * lms-canvas-common-configuration
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 1/23/15.
 */
@ContextConfiguration(classes={DefaultRoleResolverImpl.class})
@SpringBootTest
public class TestDefaultRoleResolver {

    @Autowired
    private RoleResolver roleResolver;

    @Test
    public void testGetLowestRole() throws Exception {
        List<String> userRoles = Arrays.asList("TeacherEnrollment", "ObserverEnrollment");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("ObserverEnrollment", returnedRole, "Role not a match");
    }

    @Test
    public void testGetLowestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("TeacherEnrollment");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("TeacherEnrollment", returnedRole, "Role not a match");
    }

    @Test
    public void testGetHighestRole() throws Exception {
        List<String> userRoles = Arrays.asList("TaEnrollment", "ObserverEnrollment");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("TaEnrollment", returnedRole, "Role not a match");
    }

    @Test
    public void testGetHighestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("ObserverEnrollment");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("ObserverEnrollment", returnedRole, "Role not a match");
    }
}
