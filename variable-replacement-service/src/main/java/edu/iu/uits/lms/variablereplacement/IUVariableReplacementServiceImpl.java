package edu.iu.uits.lms.variablereplacement;

/*-
 * #%L
 * lms-iu-variable-replacement-service
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

import edu.iu.uits.lms.canvas.model.Course;
import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.common.variablereplacement.DefaultVariableReplacementServiceImpl;
import edu.iu.uits.lms.common.variablereplacement.MacroVariableMapper;
import edu.iu.uits.lms.iuonly.model.SisClass;
import edu.iu.uits.lms.iuonly.model.SisCourse;
import edu.iu.uits.lms.iuonly.services.SisServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class IUVariableReplacementServiceImpl extends DefaultVariableReplacementServiceImpl {

    @Autowired
    private CourseService courseService;

    @Autowired
    private SisServiceImpl sisService;

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
         macroVariableMapper.setCanvasCourseCode(course.getCourseCode());

         SisCourse sudsCourse = sisService.getSisCourseBySiteId(course.getSisCourseId());
         if (sudsCourse != null) {
            macroVariableMapper.setSisTermId(sudsCourse.getSTerm());
            macroVariableMapper.setClassNumber(sudsCourse.getClassNumber());
            SisClass sudsClass = sisService.getSisClassByCourse(sudsCourse.getSTerm(), sudsCourse.getClassNumber(),
                  sudsCourse.getCampus(), true);

            if (sudsClass != null) {
               macroVariableMapper.setSisCampus(sudsClass.getInstitution());
            }
         }
      }
   }

}
