package edu.iu.uits.lms.lti.config;

/*-
 * #%L
 * LMS Canvas LTI Framework Services
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

import edu.iu.uits.lms.lti.repository.DefaultInstructorRoleRepository;
import edu.iu.uits.lms.lti.repository.KeyPairRepository;
import edu.iu.uits.lms.lti.repository.LtiAuthorizationRepository;
import edu.iu.uits.lms.lti.service.Lti13Service;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.transaction.PlatformTransactionManager;
import uk.ac.ox.ctl.lti13.nrps.NamesRoleService;

import javax.sql.DataSource;

@TestConfiguration
public class LtiClientTestConfig {

   @MockBean
   protected LtiAuthorizationRepository ltiAuthorizationRepository;

   @MockBean
   protected KeyPairRepository keyPairRepository;

   @MockBean
   @Qualifier("ltiDataSource")
   public DataSource dataSource;


   @MockBean
   @Qualifier("ltiEntityMgrFactory")
   public LocalContainerEntityManagerFactoryBean ltiEntityMgrFactory;

   @MockBean
   @Qualifier("ltiTransactionMgr")
   public PlatformTransactionManager ltiTransactionMgr;

   @MockBean
   public ClientRegistrationRepository clientRegistrationRepository;

   @MockBean
   public Lti13Service lti13Service;

   @MockBean
   public NamesRoleService namesRoleService;

   @MockBean
   public DefaultInstructorRoleRepository defaultInstructorRoleRepository;

}
