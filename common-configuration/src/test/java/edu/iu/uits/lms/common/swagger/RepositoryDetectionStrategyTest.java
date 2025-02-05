package edu.iu.uits.lms.common.swagger;

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
        doReturn(TestRepositoryRestResource.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertTrue(result, "Repository should be exported when package is allowed and has @RepositoryRestResource");
    }

    @Test
    public void testGoodPackageHasRestResource() {
        doReturn(TestRestRepository.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertTrue(result, "Repository should be exported when package is allowed and has @RestResource");
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

        doReturn(TestRestRepository.class).when(metadata).getRepositoryInterface();
        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when package is not allowed");
    }

    @Test
    public void testGoodPackageNoAnnotations() {
        doReturn(TestNoAnnotationRepository.class).when(metadata).getRepositoryInterface();

        boolean result = detectionStrategy.isExported(metadata);
        assertFalse(result, "Repository should not be exported when no annotations are present");
    }

    // Mock repository classes for testing
    @RestResource
    interface TestRestRepository {}

    @RepositoryRestResource
    interface TestRepositoryRestResource {}

    interface TestNoAnnotationRepository {}

    @RestResource
    @RepositoryRestResource
    interface TestBothAnnotationsRepository {}

    @RandomAnnotation
    interface TestUnrelatedAnnotationRepository {}

    @interface RandomAnnotation{}

}
