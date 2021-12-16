package edu.iu.uits.lms.common.variablereplacement;

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
        List<String> userRoles = Arrays.asList("Instructor", "urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("urn:lti:instrole:ims/lis/Observer", returnedRole, "Role not a match");
    }

    @Test
    public void testGetLowestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("Instructor");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("Instructor", returnedRole, "Role not a match");
    }

    @Test
    public void testGetHighestRole() throws Exception {
        List<String> userRoles = Arrays.asList("urn:lti:role:ims/lis/TeachingAssistant", "urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("urn:lti:role:ims/lis/TeachingAssistant", returnedRole, "Role not a match");
    }

    @Test
    public void testGetHighestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assertions.assertNotNull(returnedRole);
        Assertions.assertEquals("urn:lti:instrole:ims/lis/Observer", returnedRole, "Role not a match");
    }
}
