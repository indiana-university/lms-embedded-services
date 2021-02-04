package edu.iu.uits.lms.variablereplacement;

import canvas.client.generated.api.CoursesApi;
import edu.iu.uits.lms.common.variablereplacement.RoleResolver;
import iuonly.client.generated.api.SudsApi;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 1/23/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Import(VariableReplacementConfig.class)
public class TestRoleResolver {

    @Autowired
    private RoleResolver roleResolver;

    @MockBean
    private CoursesApi coursesApi;

    @MockBean
    private SudsApi sudsService;

    @Test
    public void testGetLowestRole() throws Exception {
        List<String> userRoles = Arrays.asList("Instructor", "urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assert.assertNotNull(returnedRole);
        Assert.assertEquals("Role not a match", "urn:lti:instrole:ims/lis/Observer", returnedRole);
    }

    @Test
    public void testGetLowestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("Instructor");
        String returnedRole = roleResolver.returnLowestRole(userRoles);
        Assert.assertNotNull(returnedRole);
        Assert.assertEquals("Role not a match", "Instructor", returnedRole);
    }

    @Test
    public void testGetHighestRole() throws Exception {
        List<String> userRoles = Arrays.asList("urn:lti:role:ims/lis/TeachingAssistant", "urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assert.assertNotNull(returnedRole);
        Assert.assertEquals("Role not a match", "urn:lti:role:ims/lis/TeachingAssistant", returnedRole);
    }

    @Test
    public void testGetHighestRole2() throws Exception {
        List<String> userRoles = Arrays.asList("urn:lti:instrole:ims/lis/Observer");
        String returnedRole = roleResolver.returnHighestRole(userRoles);

        Assert.assertNotNull(returnedRole);
        Assert.assertEquals("Role not a match", "urn:lti:instrole:ims/lis/Observer", returnedRole);
    }
}
