package edu.iu.uits.lms.common.swagger;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy;

import java.util.List;

@Data
@AllArgsConstructor
@Slf4j
public class LmsRepositoryDetectionStrategy implements RepositoryDetectionStrategy {

    private List<String> packagesToInclude;

    @Override
    public boolean isExported(RepositoryMetadata metadata) {
        // Check if the repository's package is in the allowed list
        String repositoryPackage = metadata.getRepositoryInterface().getPackageName();
        boolean isPackageAllowed = packagesToInclude.stream()
                .anyMatch(repositoryPackage::startsWith);

        // Check for specific annotations
        boolean hasRepositoryRestResource = metadata.getRepositoryInterface().isAnnotationPresent(RepositoryRestResource.class);
        boolean hasRestResource = metadata.getRepositoryInterface().isAnnotationPresent(RestResource.class);

        log.debug("Packages to check: {}", packagesToInclude);
        log.debug("Checking {}: pkg: {}, repoRest: {}, rest: {}", repositoryPackage, isPackageAllowed, hasRepositoryRestResource, hasRestResource);
        return isPackageAllowed && (hasRepositoryRestResource || hasRestResource);
    }
}
