package edu.iu.uits.lms.canvas.model;

/*-
 * #%L
 * LMS Canvas Services
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
public class GradeDataWrapperTest {

    @Test
    void testFullJson() throws Exception {
        GradeDataWrapper wrapper = new GradeDataWrapper();
        wrapper.addGradeDetail("12345", "A", "Great job!");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(wrapper);

        assertTrue(json.contains("grade_data"));
        assertTrue(json.contains("posted_grade"));
        assertTrue(json.contains("text_comment"));
        assertTrue(json.contains("12345"));
        assertTrue(json.contains("Great job!"));
        assertTrue(json.contains("A"));
    }

    @Test
    void testJsonNoGrade() throws Exception {
        GradeDataWrapper wrapper = new GradeDataWrapper();
        wrapper.addGradeDetail("12345", null, "Great job!");

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(wrapper);

        assertTrue(json.contains("grade_data"));
        assertFalse(json.contains("posted_grade"));
        assertTrue(json.contains("text_comment"));
        assertTrue(json.contains("12345"));
        assertTrue(json.contains("Great job!"));
    }

    @Test
    void testJsonNoComment() throws Exception {
        GradeDataWrapper wrapper = new GradeDataWrapper();
        wrapper.addGradeDetail("12345", "A", null);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(wrapper);

        assertTrue(json.contains("grade_data"));
        assertTrue(json.contains("posted_grade"));
        assertFalse(json.contains("text_comment"));
        assertTrue(json.contains("12345"));
        assertTrue(json.contains("A"));
    }

    @Test
    void testJsonNoData() throws Exception {
        GradeDataWrapper wrapper = new GradeDataWrapper();
        wrapper.addGradeDetail("12345", null, null);

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(wrapper);

        log.debug("{}", json);

        assertTrue(json.contains("grade_data"));
        assertFalse(json.contains("posted_grade"));
        assertFalse(json.contains("text_comment"));
        assertTrue(json.contains("12345"));
//        assertTrue(json.contains("A"));
    }
}
