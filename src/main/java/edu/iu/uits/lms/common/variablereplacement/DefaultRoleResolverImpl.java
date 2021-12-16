package edu.iu.uits.lms.common.variablereplacement;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 1/23/15.
 */
@Service
public class DefaultRoleResolverImpl implements RoleResolver {

    private static final String[] orderedRoles = {"Instructor","urn:lti:role:ims/lis/TeachingAssistant","ContentDeveloper","Learner","urn:lti:instrole:ims/lis/Observer"};

    @Override
    public String returnHighestRole(List<String> userRoles) {
        for (String orderedRole : orderedRoles) {
            if (userRoles.contains(orderedRole))
                return orderedRole;
        }
        return null;
    }

    @Override
    public String returnLowestRole(List<String> userRoles) {
        for (int i = orderedRoles.length-1; i>=0; i--) {
            if (userRoles.contains(orderedRoles[i]))
                return orderedRoles[i];
        }
        return null;
    }


    @Override
    public String returnHighestRole(String[] userRoles) {
        return returnHighestRole(Arrays.asList(userRoles));
    }

    @Override
    public String returnLowestRole(String[] userRoles) {
        return returnLowestRole(Arrays.asList(userRoles));
    }
}
