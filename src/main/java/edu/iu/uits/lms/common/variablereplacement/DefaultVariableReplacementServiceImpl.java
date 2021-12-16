package edu.iu.uits.lms.common.variablereplacement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaultVariableReplacementServiceImpl extends AbstractVariableReplacementService {

    @Autowired
    private RoleResolver roleResolver;

   @Override
   public void setupMapper(MacroVariableMapper macroVariableMapper, String[] roles) {
      String userRole = roleResolver.returnHighestRole(roles);
      macroVariableMapper.setUserRole(userRole);

      /*
      There are still things that need to be implemented by a costom service
      The following still need to be resolved:
      sisCourseId
      sisTermId
      classNumber
      sisCampus
       */

   }

}
