package edu.iu.uits.lms.canvas.model;

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
