package edu.iu.uits.lms.lti.controller;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
 * %%
 * Copyright (C) 2015 - 2021 Indiana University
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

import edu.iu.uits.lms.common.variablereplacement.VariableReplacementService;

//TODO - Figure this guy out
public abstract class RedirectableLtiController { //extends LtiController {

   public static final String CUSTOM_REDIRECT_URL_PROP = "custom_redirect_url";

   /**
    * Get the VariableReplacementService
    * @return VariableReplacementService
    */
   protected abstract VariableReplacementService getVariableReplacementService();

   /**
    * Do any variable replacement in the inputUrl
    * @param inputUrl Input url, potentially containing variables
    * @param launchParams Map of launch parameters
    * @return New url with variables replaced
    */
//   protected String performMacroVariableReplacement(String inputUrl, Map<String, String> launchParams) {
//      MacroVariableMapper macroVariableMapper = new MacroVariableMapper();
//
//      macroVariableMapper.setUserLastName(launchParams.get(BasicLTIConstants.LIS_PERSON_NAME_FAMILY));
//      macroVariableMapper.setUserFirstName(launchParams.get(BasicLTIConstants.LIS_PERSON_NAME_GIVEN));
//      macroVariableMapper.setUserNetworkId(launchParams.get(CUSTOM_CANVAS_USER_LOGIN_ID));
//      macroVariableMapper.setUserId(launchParams.get(BasicLTIConstants.LIS_PERSON_SOURCEDID));
//
//      String extRoles = launchParams.get(BasicLTIConstants.ROLES);
//      String[] roles = extRoles.split(",");
//
//      String canvasCourseId = launchParams.get(CUSTOM_CANVAS_COURSE_ID);
//      macroVariableMapper.setCanvasCourseId(canvasCourseId);
//
//      VariableReplacementService variableReplacementService = getVariableReplacementService();
//
//      variableReplacementService.setupMapper(macroVariableMapper, roles);
//
//      return variableReplacementService.performMacroVariableReplacement(macroVariableMapper, inputUrl);
//   }

}
