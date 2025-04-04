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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

public class RepositoryDetectionStrategyTest {

    private LmsRepositoryDetectionStrategy detectionStrategy;
    private RepositoryMetadata metadata;

    @BeforeEach
    public void setUp() {
        detectionStrategy = new LmsRepositoryDetectionStrategy(List.of(RepositoryDetectionStrategyTest.class.getPackageName()));
        metadata = Mockito.mock(RepositoryMetadata.class);
    }

    @Test
    public void testGoodPackageHasRepositoryRestResource() {
        doReturn(TestRepositoryRestResourceExported.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertTrue(result, "Repository should be exported when package is allowed and has @RepositoryRestResource");
    }

    @Test
    public void testGoodPackageHasRepositoryRestResourceButNotExported() {
        doReturn(TestRepositoryRestResourceNotExported.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is allowed and has @RepositoryRestResource, but exported = false");
    }

    @Test
    public void testGoodPackageHasRestResource() {
        doReturn(TestRestRepositoryExported.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertTrue(result, "Repository should be exported when package is allowed and has @RestResource");
    }

    @Test
    public void testGoodPackageHasRestResourceButNotExported() {
        doReturn(TestRestRepositoryNotExported.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is allowed and has @RestResource, but exported = false");
    }

    @Test
    public void testGoodPackageHasBothAnnotations() {
        doReturn(TestBothAnnotationsRepository.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertTrue(result, "Repository should be exported when package is allowed and has @RestResource");
    }

    @Test
    public void testGoodPackageHasRandomAnnotation() {
        doReturn(TestUnrelatedAnnotationRepository.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is allowed and has no appropriate annotation");
    }

    @Test
    public void testBadPackage() {
        //Override the strategy to use a different package
        detectionStrategy = new LmsRepositoryDetectionStrategy(List.of("com.other.repository"));

        doReturn(TestRestRepositoryExported.class).when(metadata).getRepositoryInterface();
        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is not allowed");
    }

    @Test
    public void testGoodPackageNoAnnotations() {
        doReturn(TestNoAnnotationRepository.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when no annotations are present");
    }

    @Test
    public void testNoConfiguredPackage() {
        //Override the strategy to use a different package
        detectionStrategy = new LmsRepositoryDetectionStrategy(List.of());

        doReturn(TestRestRepositoryExported.class).when(metadata).getRepositoryInterface();
        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is not allowed");
    }

    @Test
    public void testNullConfiguredPackage() {
        //Override the strategy to use a different package
        detectionStrategy = new LmsRepositoryDetectionStrategy(null);

        doReturn(TestRestRepositoryExported.class).when(metadata).getRepositoryInterface();
        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is not allowed");
    }

    // Mock repository classes for testing
    @RestResource
    interface TestRestRepositoryExported {}

    @RepositoryRestResource
    interface TestRepositoryRestResourceExported {}

    @RestResource(exported = false)
    interface TestRestRepositoryNotExported {}

    @RepositoryRestResource(exported = false)
    interface TestRepositoryRestResourceNotExported {}

    interface TestNoAnnotationRepository {}

    @RestResource
    @RepositoryRestResource
    interface TestBothAnnotationsRepository {}

    @RandomAnnotation
    interface TestUnrelatedAnnotationRepository {}

    @interface RandomAnnotation{}

}
