package uk.ac.ox.ctl.lti13.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

public class OAuthAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

	public void onAuthenticationFailure(HttpServletRequest request,
										HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		if (exception instanceof OAuth2AuthenticationException) {
			response.sendError(HttpStatus.UNAUTHORIZED.value(), ((OAuth2AuthenticationException)exception).getError().getErrorCode()+ " : "+ exception.getMessage());
		} else {
			super.onAuthenticationFailure(request, response, exception);
		}
	}
}
