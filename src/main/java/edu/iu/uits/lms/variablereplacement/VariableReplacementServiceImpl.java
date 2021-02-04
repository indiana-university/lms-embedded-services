package edu.iu.uits.lms.variablereplacement;

import canvas.client.generated.api.CoursesApi;
import canvas.client.generated.model.Course;
import edu.iu.uits.lms.common.variablereplacement.AbstractVariableReplacementService;
import edu.iu.uits.lms.common.variablereplacement.MacroVariableMapper;
import edu.iu.uits.lms.common.variablereplacement.RoleResolver;
import iuonly.client.generated.api.SudsApi;
import iuonly.client.generated.model.SudsClass;
import iuonly.client.generated.model.SudsCourse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VariableReplacementServiceImpl extends AbstractVariableReplacementService {

    @Autowired
    @Qualifier("coursesApiViaAnonymous")
    private CoursesApi coursesApi;

    @Autowired
    @Qualifier("sudsApiViaAnonymous")
    private SudsApi sudsService;

    @Autowired
    private RoleResolver roleResolver;

   @Override
   public void setupMapper(MacroVariableMapper macroVariableMapper, String[] roles) {
      String userRole = roleResolver.returnHighestRole(roles);
      macroVariableMapper.setUserRole(userRole);

      //lookup canvas course info
      Course course = coursesApi.getCourse(macroVariableMapper.getCanvasCourseId());
      if (course != null) {
         macroVariableMapper.setSisCourseId(course.getSisCourseId());

         SudsCourse sudsCourse = sudsService.getSudsCourseBySiteId(course.getSisCourseId());
         if (sudsCourse != null) {
            macroVariableMapper.setSisTermId(sudsCourse.getSterm());
            macroVariableMapper.setClassNumber(sudsCourse.getClassNumber());
            SudsClass sudsClass = sudsService.getSudsClassByCourse(sudsCourse.getSterm(), sudsCourse.getClassNumber(),
                  sudsCourse.getCampus(), true);

            if (sudsClass != null) {
               macroVariableMapper.setSisCampus(sudsClass.getInstitution());
            }
         }
      }
   }

}
