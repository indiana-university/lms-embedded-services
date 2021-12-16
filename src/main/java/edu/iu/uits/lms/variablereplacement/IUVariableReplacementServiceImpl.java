package edu.iu.uits.lms.variablereplacement;

import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.common.variablereplacement.DefaultVariableReplacementServiceImpl;
import edu.iu.uits.lms.common.variablereplacement.MacroVariableMapper;
import edu.iu.uits.lms.iuonly.model.SudsClass;
import edu.iu.uits.lms.iuonly.model.SudsCourse;
import edu.iu.uits.lms.iuonly.services.SudsServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IUVariableReplacementServiceImpl extends DefaultVariableReplacementServiceImpl {

    @Autowired
    private CourseService courseService;

    @Autowired
    private SudsServiceImpl sudsService;

   public IUVariableReplacementServiceImpl() {
      log.info("IUVariableReplacementServiceImpl()");
   }

   @Override
   public void setupMapper(MacroVariableMapper macroVariableMapper, String[] roles) {
      super.setupMapper(macroVariableMapper, roles);

      //lookup canvas course info
      Course course = courseService.getCourse(macroVariableMapper.getCanvasCourseId());
      if (course != null) {
         macroVariableMapper.setSisCourseId(course.getSisCourseId());

         SudsCourse sudsCourse = sudsService.getSudsCourseBySiteId(course.getSisCourseId());
         if (sudsCourse != null) {
            macroVariableMapper.setSisTermId(sudsCourse.getSTerm());
            macroVariableMapper.setClassNumber(sudsCourse.getClassNumber());
            SudsClass sudsClass = sudsService.getSudsClassByCourse(sudsCourse.getSTerm(), sudsCourse.getClassNumber(),
                  sudsCourse.getCampus(), true);

            if (sudsClass != null) {
               macroVariableMapper.setSisCampus(sudsClass.getInstitution());
            }
         }
      }
   }

}
