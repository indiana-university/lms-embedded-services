package edu.iu.uits.lms.iuonly.repository;

import edu.iu.uits.lms.iuonly.model.acl.AuthorizedUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;

@Component
@RepositoryRestResource(path = "auth_user")
@Tag(name = "AuthorizedUserRepository", description = "Operations involving the AuthorizedUser table")
@CrossOrigin(origins = {"${lms.swagger.cors.origin}"})
public interface AuthorizedUserRepository extends PagingAndSortingRepository<AuthorizedUser, Long>, ListCrudRepository<AuthorizedUser, Long> {

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.username = :username")
   AuthorizedUser findByUsername(@Param("username") String username);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.canvasUserId = :canvasUserId")
   AuthorizedUser findByCanvasUserId(@Param("canvasUserId") String canvasUserId);

   @Query("SELECT au FROM AuthorizedUser au JOIN au.toolPermissions tp WHERE au.active = true AND au.username = :username AND KEY(tp) = :toolPermission")
   AuthorizedUser findByUsernameAndToolPermission(@Param("username") String username, @Param("toolPermission") String toolPermission);

}
