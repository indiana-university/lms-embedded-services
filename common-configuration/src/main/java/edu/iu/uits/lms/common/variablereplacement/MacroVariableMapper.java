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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Created by chmaurer on 1/21/15.
 */
@Slf4j
@NoArgsConstructor
public class MacroVariableMapper implements Serializable {

    /** Macro name: User id */
    public static final String MACRO_USER_ID             = "${USER_ID}";

    /** Macro name: User enterprise id */
    public static final String MACRO_USER_EID            = "${USER_EID}";

    /** Macro name: First name */
    public static final String MACRO_USER_FIRST_NAME     = "${USER_FIRST_NAME}";

    /** Macro name: Last name */
    public static final String MACRO_USER_LAST_NAME      = "${USER_LAST_NAME}";

    /** Macro name: Role */
    public static final String MACRO_USER_ROLE           = "${USER_ROLE}";

    /** Macro name: Course Id */
    public static final String MACRO_SIS_COURSE_ID       = "${SIS_COURSE_ID}";

    /** Macro name: class number */
    public static final String MACRO_CLASS_NBR           = "${CLASS_NBR}";

    /** Macro name: Term Id */
    public static final String MACRO_SIS_TERM_ID         = "${SIS_TERM_ID}";

    /** Macro name: Campus */
    public static final String MACRO_SIS_CAMPUS          = "${SIS_CAMPUS}";

    /** Macro name: Canvas Course Id */
    public static final String MACRO_CANVAS_COURSE_ID    = "${CANVAS_COURSE_ID}";

    /** Macro name: Canvas Account Id */
    public static final String MACRO_CANVAS_ACCOUNT_ID    = "${CANVAS_ACCOUNT_ID}";

    public static List<String> ALLOWED_MACROS_LIST = Arrays.asList(MACRO_USER_ID, MACRO_USER_EID, MACRO_USER_FIRST_NAME,
            MACRO_USER_LAST_NAME, MACRO_USER_ROLE, MACRO_SIS_COURSE_ID, MACRO_CLASS_NBR, MACRO_SIS_TERM_ID, MACRO_SIS_CAMPUS ,
            MACRO_CANVAS_COURSE_ID, MACRO_CANVAS_ACCOUNT_ID);

    @Setter @Getter
    private String userId;

    @Setter @Getter
    private String userFirstName;

    @Setter @Getter
    private String userLastName;

    @Setter @Getter
    private String userNetworkId;

    @Setter @Getter
    private String userRole;

    @Setter @Getter
    private String sisCourseId;

    @Setter @Getter
    private String sisTermId;

    @Setter @Getter
    private String sisCampus;

    @Setter @Getter
    private String classNumber;

    @Setter @Getter
    private String canvasCourseId;

    @Setter @Getter
    private String canvasAccountId;


    /**
     * Lookup value for requested macro name
     * @param macroName Macro name
     * @return Return the result for the passed in input variable
     */
    protected String getMacroValue(String macroName) {
        String output = null;

        switch (macroName) {
            case MACRO_USER_ID:
                output = getUserId();
                break;
            case MACRO_USER_EID:
                output = getUserNetworkId();
                break;
            case MACRO_USER_FIRST_NAME:
                output = getUserFirstName();
                break;
            case MACRO_USER_LAST_NAME:
                output = getUserLastName();
                break;
            case MACRO_USER_ROLE:
                output = getUserRole();
                break;
            case MACRO_SIS_CAMPUS:
                output = getSisCampus();
                break;
            case MACRO_SIS_COURSE_ID:
                output = getSisCourseId();
                break;
            case MACRO_SIS_TERM_ID:
                output = getSisTermId();
                break;
            case MACRO_CLASS_NBR:
                output = getClassNumber();
                break;
            case MACRO_CANVAS_COURSE_ID:
                output = getCanvasCourseId();
                break;
            case MACRO_CANVAS_ACCOUNT_ID:
                output = getCanvasAccountId();
                break;
            default:
                output = macroName;
                break;
        }

        //In case something still had no value, set back to the input.
        if (output == null)
            output = macroName;

        return output;
    }

    /**
     * Determine if the input url contains any macros that need expanding
     * @param inputString Input string
     * @return Boolean indicating if the string contains any variables
     */
    public static boolean containsMacros(String inputString) {
        return inputString.contains("${");
    }

}
