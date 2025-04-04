package edu.iu.uits.lms.iuonly.rest;

/*-
 * #%L
 * lms-canvas-iu-custom-services
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

import edu.iu.uits.lms.canvas.services.CourseService;
import edu.iu.uits.lms.iuonly.repository.AuthorizedUserRepository;
import edu.iu.uits.lms.iuonly.repository.ErrorContactEventRepository;
import edu.iu.uits.lms.iuonly.repository.ErrorContactJobProfileRepository;
import edu.iu.uits.lms.iuonly.repository.FeatureAccessRepository;
import edu.iu.uits.lms.iuonly.repository.FileStorageRepository;
import edu.iu.uits.lms.iuonly.repository.HierarchyResourceRepository;
import edu.iu.uits.lms.iuonly.repository.LmsBatchEmailRepository;
import edu.iu.uits.lms.iuonly.repository.NodeHierarchyRepository;
import edu.iu.uits.lms.iuonly.repository.TemplatedCourseRepository;
import edu.iu.uits.lms.iuonly.services.FeatureAccessServiceImpl;
import edu.iu.uits.lms.iuonly.services.HierarchyResourceService;
import edu.iu.uits.lms.iuonly.services.SisServiceImpl;
import edu.iu.uits.lms.iuonly.services.rest.BatchEmailRestController;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootConfiguration
@ComponentScan(basePackageClasses = BatchEmailRestController.class, value = "IuRestTestConfig")
public class IuRestTestConfig {

   @MockitoBean
   public AuthorizedUserRepository authorizedUserRepository;

   @MockitoBean
   public ErrorContactEventRepository errorContactEventRepository;

   @MockitoBean
   public ErrorContactJobProfileRepository errorContactJobProfileRepository;

   @MockitoBean
   public FeatureAccessRepository featureAccessRepository;

   @MockitoBean
   public LmsBatchEmailRepository lmsBatchEmailRepository;

   @MockitoBean
   public NodeHierarchyRepository nodeHierarchyRepository;

   @MockitoBean
   public TemplatedCourseRepository templatedCourseRepository;

   @MockitoBean
   public HierarchyResourceRepository hierarchyResourceRepository;

   @MockitoBean
   public FileStorageRepository fileStorageRepository;

   @MockitoBean
   private FeatureAccessServiceImpl featureAccessService;

   @MockitoBean
   private HierarchyResourceService hierarchyResourceService;

   @MockitoBean
   private SisServiceImpl sisService;

   @MockitoBean
   private CourseService courseService;

}
