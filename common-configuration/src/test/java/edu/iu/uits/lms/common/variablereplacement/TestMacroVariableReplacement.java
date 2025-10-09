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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.text.MessageFormat;

/**
 * Created by chmaurer on 1/21/15.
 */
@ContextConfiguration(classes={DefaultVariableReplacementServiceImpl.class})
@SpringBootTest
public class TestMacroVariableReplacement {

    @Autowired
    private VariableReplacementService variableReplacementService;

    @MockitoBean
    private RoleResolver roleResolver;

    private MacroVariableMapper macroVariableMapper = null;

    @BeforeEach
    public void setUp() throws Exception {
        macroVariableMapper = new MacroVariableMapper();
        macroVariableMapper.setUserFirstName("John");
        macroVariableMapper.setUserLastName("Smith");
        macroVariableMapper.setSisCampus("asdf");
        macroVariableMapper.setSisTermId("1234");
        macroVariableMapper.setSisCourseId("ASDF-1234-QWER-0987");
        macroVariableMapper.setUserNetworkId("jsmith");
        macroVariableMapper.setUserRole("StudentEnrollment");
        macroVariableMapper.setUserId("000123456789");
        macroVariableMapper.setClassNumber("9876");
        macroVariableMapper.setCanvasCourseId("1111111");
        macroVariableMapper.setCanvasAccountId("ABCDE");
        macroVariableMapper.setCanvasCourseCode("A1B2C3");
    }

    @Test
    public void testSimpleReplacement() throws Exception {
        String inputTemplate = "Hello, {0} {1}.  It is {0}, isn''t it?";
        String input = MessageFormat.format(inputTemplate, MacroVariableMapper.MACRO_USER_FIRST_NAME, MacroVariableMapper.MACRO_USER_LAST_NAME);

        String output = "Hello, John Smith.  It is John, isn't it?";

        String processed = variableReplacementService.performMacroVariableReplacement(macroVariableMapper, input);

        Assertions.assertEquals(output, processed, "results don't match");
    }

    @Test
    public void testNullCheck() throws Exception {
        IllegalArgumentException t = Assertions.assertThrows(IllegalArgumentException.class, () ->
              variableReplacementService.performMacroVariableReplacement(macroVariableMapper, null));
        Assertions.assertEquals("inputString cannot be null", t.getMessage());
    }

    @Test
    public void testExpandAll() throws Exception {
        String template = "{0};{1};{2};{3};{4};{5};{6};{7};{8};{9};{10};{11}";
        String input = MessageFormat.format(template, MacroVariableMapper.MACRO_USER_FIRST_NAME, MacroVariableMapper.MACRO_USER_LAST_NAME,
                MacroVariableMapper.MACRO_SIS_CAMPUS, MacroVariableMapper.MACRO_SIS_TERM_ID, MacroVariableMapper.MACRO_SIS_COURSE_ID,
                MacroVariableMapper.MACRO_USER_EID, MacroVariableMapper.MACRO_USER_ROLE, MacroVariableMapper.MACRO_USER_ID,
                MacroVariableMapper.MACRO_CLASS_NBR, MacroVariableMapper.MACRO_CANVAS_COURSE_ID, MacroVariableMapper.MACRO_CANVAS_ACCOUNT_ID,
                MacroVariableMapper.MACRO_CANVAS_COURSE_CODE);

//        String outputTemplate = "{0};{1};{2};{3};{4};{5};{6};{7};";
        String output = MessageFormat.format(template, macroVariableMapper.getUserFirstName(), macroVariableMapper.getUserLastName(),
                macroVariableMapper.getSisCampus(), macroVariableMapper.getSisTermId(), macroVariableMapper.getSisCourseId(),
                macroVariableMapper.getUserNetworkId(), macroVariableMapper.getUserRole(), macroVariableMapper.getUserId(),
                macroVariableMapper.getClassNumber(), macroVariableMapper.getCanvasCourseId(), macroVariableMapper.getCanvasAccountId(),
                macroVariableMapper.getCanvasCourseCode());

        String processed = variableReplacementService.performMacroVariableReplacement(macroVariableMapper, input);

        Assertions.assertEquals(output, processed, "results don't match");
    }

    @Test
    public void testInvalidVariable() throws Exception {
        String input = "This is ${BAD}.";
        String processed = variableReplacementService.performMacroVariableReplacement(macroVariableMapper, input);

        // Output should be unchanged since there was no valid variable to replace.
        Assertions.assertEquals(input, processed, "results don't match");
    }
}
