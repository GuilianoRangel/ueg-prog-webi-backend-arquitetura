package br.ueg.prog.webi.api.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
public class LogoutService implements LogoutHandler {

  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {
    final String authHeader = request.getHeader("Authorization");
    if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
      return;
    }
    SecurityContextHolder.clearContext();
  }
}
