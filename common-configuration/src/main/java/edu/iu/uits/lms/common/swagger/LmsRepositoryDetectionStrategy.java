package edu.iu.uits.lms.common.swagger;

/*-
 * #%L
 * lms-canvas-common-configuration
 * %%
 * Copyright (C) 2015 - 2025 Indiana University
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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;

import java.util.List;

/**
 * Create a RepositoryDetectionStrategy that has to match the configured package list, in addition to either a
 * class annotated with RepositoryRestResource or RestResource
 */
@Data
@AllArgsConstructor
@Slf4j
public class LmsRepositoryDetectionStrategy implements RepositoryDetectionStrategy {

    private List<String> initPackagesToInclude;

    @Override
    public boolean isExported(RepositoryMetadata metadata) {
        // If no packages were configured to include, use an empty list
        List<String> packagesToInclude = initPackagesToInclude == null ? List.of() : initPackagesToInclude;

        // Check if the repository's package is in the allowed list
        String repositoryPackage = metadata.getRepositoryInterface().getPackageName();
        boolean isPackageAllowed = packagesToInclude.stream().anyMatch(repositoryPackage::startsWith);

        // Check for allowed annotations
        boolean hasAnnotation = RepositoryDetectionStrategies.ANNOTATED.isExported(metadata);

        log.debug("Packages to check for class {}: {}", metadata.getRepositoryInterface(), packagesToInclude);
        log.debug("Checking {}: pkg: {}, anyAnnotation: {}", repositoryPackage, isPackageAllowed, hasAnnotation);
        return isPackageAllowed && hasAnnotation;
    }
}
