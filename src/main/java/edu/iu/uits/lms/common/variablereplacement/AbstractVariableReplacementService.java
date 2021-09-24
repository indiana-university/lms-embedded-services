package edu.iu.uits.lms.common.variablereplacement;

/*-
 * #%L
 * lms-canvas-common-configuration
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

import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
public abstract class AbstractVariableReplacementService implements VariableReplacementService {

   @Override
   public String performMacroVariableReplacement(MacroVariableMapper macroVariableMapper, String inputString) {
      if (inputString == null)
         throw new IllegalArgumentException("inputString cannot be null");

      /*
       * Quit now if no macros are embedded in the text
       */
      if (!MacroVariableMapper.containsMacros(inputString)) {
         return inputString;
      }
      /*
       * Expand each macro
       */
      StringBuilder sb = new StringBuilder(inputString);

      for (String macroName : MacroVariableMapper.ALLOWED_MACROS_LIST) {
         expand(macroVariableMapper, sb, macroName);
      }

      log.debug("Input String: " + inputString);
      log.debug("Output String: " + sb.toString());
      return sb.toString();
   }

   /**
    * Expand one macro reference
    * @param sb Expand macros found in this text
    * @param macroName Macro name
    */
   private void expand(MacroVariableMapper macroVariableMapper, StringBuilder sb, String macroName) {
      int index = sb.indexOf(macroName);

      //Replace every occurrence of the macro in the parameter list
      while (index != -1) {
         String  macroValue = URLEncoder.encode(macroVariableMapper.getMacroValue(macroName), StandardCharsets.UTF_8);

         sb.replace(index, (index + macroName.length()), macroValue);
         index = sb.indexOf(macroName, (index + macroValue.length()));
      }
   }
}
