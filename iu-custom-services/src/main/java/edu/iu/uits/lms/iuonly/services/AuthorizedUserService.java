package edu.iu.uits.lms.iuonly.services;

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

    public AuthorizedUser findByUsernameAndToolPermission(String username, String toolPermission) {
        return authorizedUserRepository.findByUsernameAndToolPermission(username, toolPermission);
    }

//    public AuthorizedUser findByCanvasUserId(String canvasUserId) {
//        return authorizedUserRepository.findByCanvasUserId(canvasUserId);
//    }

    /**
     * Convert the input to a boolean.  Does a case-insensitive compare to "true".  Anything that doesn't match is false.
     * @param propertyValue
     * @return
     */
    public static boolean convertPropertyToBoolean(String propertyValue) {
        return Boolean.parseBoolean(propertyValue);
    }

    public static String[] convertPropertyToStringArray(String propertyValue) {
        if (propertyValue == null) {
            return new String[]{};
        }
        //Split on the comma and trim all white space
        return propertyValue.trim().split("\\s*,\\s*");
    }

    public static List<String> convertPropertyToList(String propertyValue) {
        return Arrays.stream(convertPropertyToStringArray(propertyValue))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}
