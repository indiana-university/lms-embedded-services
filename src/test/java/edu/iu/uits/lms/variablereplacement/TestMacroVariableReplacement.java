package edu.iu.uits.lms.variablereplacement;

import canvas.client.generated.api.CoursesApi;
import edu.iu.uits.lms.common.variablereplacement.MacroVariableMapper;
import edu.iu.uits.lms.common.variablereplacement.VariableReplacementService;
import iuonly.client.generated.api.SudsApi;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.text.MessageFormat;

/**
 * Created by chmaurer on 1/21/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Import(VariableReplacementConfig.class)
public class TestMacroVariableReplacement {

    @Autowired
    private VariableReplacementService variableReplacementService;

    @MockBean
    private CoursesApi coursesApi;

    @MockBean
    private SudsApi sudsService;

    @MockBean
    private RoleResolverImpl roleResolver;

    private MacroVariableMapper macroVariableMapper = null;

    @Before
    public void setUp() throws Exception {
        macroVariableMapper = new MacroVariableMapper();
        macroVariableMapper.setUserFirstName("John");
        macroVariableMapper.setUserLastName("Smith");
        macroVariableMapper.setSisCampus("asdf");
        macroVariableMapper.setSisTermId("1234");
        macroVariableMapper.setSisCourseId("ASDF-1234-QWER-0987");
        macroVariableMapper.setUserNetworkId("jsmith");
        macroVariableMapper.setUserRole("student");
        macroVariableMapper.setUserId("000123456789");
        macroVariableMapper.setClassNumber("9876");
        macroVariableMapper.setCanvasCourseId("1111111");
    }

    @Test
    public void testSimpleReplacement() throws Exception {
        String inputTemplate = "Hello, {0} {1}.  It is {0}, isn''t it?";
        String input = MessageFormat.format(inputTemplate, MacroVariableMapper.MACRO_USER_FIRST_NAME, MacroVariableMapper.MACRO_USER_LAST_NAME);

        String output = "Hello, John Smith.  It is John, isn't it?";

        String processed = variableReplacementService.performMacroVariableReplacement(macroVariableMapper, input);

        Assert.assertEquals("results don't match", output, processed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullCheck() throws Exception {
        variableReplacementService.performMacroVariableReplacement(macroVariableMapper, null);
    }

    @Test
    public void testExpandAll() throws Exception {
        String template = "{0};{1};{2};{3};{4};{5};{6};{7};{8};{9};";
        String input = MessageFormat.format(template, MacroVariableMapper.MACRO_USER_FIRST_NAME, MacroVariableMapper.MACRO_USER_LAST_NAME,
                MacroVariableMapper.MACRO_SIS_CAMPUS, MacroVariableMapper.MACRO_SIS_TERM_ID, MacroVariableMapper.MACRO_SIS_COURSE_ID,
                MacroVariableMapper.MACRO_USER_EID, MacroVariableMapper.MACRO_USER_ROLE, MacroVariableMapper.MACRO_USER_ID,
                MacroVariableMapper.MACRO_CLASS_NBR, MacroVariableMapper.MACRO_CANVAS_COURSE_ID);

//        String outputTemplate = "{0};{1};{2};{3};{4};{5};{6};{7};";
        String output = MessageFormat.format(template, macroVariableMapper.getUserFirstName(), macroVariableMapper.getUserLastName(),
                macroVariableMapper.getSisCampus(), macroVariableMapper.getSisTermId(), macroVariableMapper.getSisCourseId(),
                macroVariableMapper.getUserNetworkId(), macroVariableMapper.getUserRole(), macroVariableMapper.getUserId(),
                macroVariableMapper.getClassNumber(), macroVariableMapper.getCanvasCourseId());

        String processed = variableReplacementService.performMacroVariableReplacement(macroVariableMapper, input);

        Assert.assertEquals("results don't match", output, processed);
    }
}
