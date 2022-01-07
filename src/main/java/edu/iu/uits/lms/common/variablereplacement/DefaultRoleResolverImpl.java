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
