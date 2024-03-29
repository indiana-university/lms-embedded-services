package edu.iu.uits.lms.iuonly.services.rest;

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

import edu.iu.uits.lms.iuonly.model.StoredFile;
import edu.iu.uits.lms.iuonly.repository.FileStorageRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;

import static edu.iu.uits.lms.iuonly.IuCustomConstants.IUCUSTOMREST_PROFILE;

@Profile(IUCUSTOMREST_PROFILE)
@RestController
@RequestMapping({"/rest/iu/file"})
@Tag(name = "FileAccessRestController", description = "File download functionality")
public class FileAccessRestController {

   @Autowired
   private FileStorageRepository fileStorageRepository = null;

   @RequestMapping(value = "/download/{id}/{filename}", method = RequestMethod.GET)
   @Transactional(transactionManager = "postgresdbTransactionMgr")
   @Operation(summary = "Download a template file from the HierarchyResource table")
   public ResponseEntity download(@PathVariable(name = "id") Long resourceId, @PathVariable(name = "filename") String filename) {

      StoredFile storedFile = fileStorageRepository.findById(resourceId).orElse(null);

      if (storedFile != null) {

         byte[] fileBytes = storedFile.getContent();

         InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(fileBytes));

         HttpHeaders headers = new HttpHeaders();
         headers.setContentDispositionFormData("attachment", storedFile.getDisplayName());

         return ResponseEntity.ok()
               .headers(headers)
               .contentLength(fileBytes.length)
               .contentType(MediaType.APPLICATION_OCTET_STREAM)
               .body(resource);
      }
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cannot find file");
   }

}
