package edu.iu.uits.lms.iuonly;

import edu.iu.uits.lms.iuonly.model.ProvisioningTerm;
import edu.iu.uits.lms.iuonly.repository.ProvisioningTermRepository;
import edu.iu.uits.lms.iuonly.services.LmsTermService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;

@ContextConfiguration(classes={LmsTermService.class})
@SpringBootTest
@Slf4j
public class LmsTermServiceTest {

   @Autowired
   private LmsTermService lmsTermService;

   @MockitoBean
   private ProvisioningTermRepository provisioningTermRepository = null;

   @BeforeEach
   public void setUp() throws Exception {
      List<ProvisioningTerm> terms = new ArrayList<>();
      terms.add(new ProvisioningTerm("asdf", "ASDF Term", "Today", "Tomorrow"));
      terms.add(new ProvisioningTerm("qwerty", "qwerty Term", "Today", "Tomorrow"));
      terms.add(new ProvisioningTerm("foobar", "foobar Term", "Today", "Tomorrow"));

      Mockito.when(provisioningTermRepository.findAll()).thenReturn(terms);
   }

   @Test
   public void testDelimitedList() {
      String list = lmsTermService.termIds2DelimitedString();
      Assertions.assertEquals("'asdf', 'qwerty', 'foobar'", list);
   }

   @TestConfiguration
   static class TestContextConfiguration {
      @Bean
      public LmsTermService lmsTermService() {
         return new LmsTermService();
      }

   }
}
