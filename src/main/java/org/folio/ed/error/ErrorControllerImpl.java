package org.folio.ed.error;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ErrorControllerImpl implements ErrorController {

  @GetMapping("/error")
  public void handleGetError(HttpServletRequest request) {
    processAuthorizationException(request);
  }

  @PostMapping("/error")
  public void handlePostError(HttpServletRequest request) {
    processAuthorizationException(request);
  }

  private void processAuthorizationException(HttpServletRequest request) {
    if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null && request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)
      .getClass() == AuthorizationException.class) {
      throw new AuthorizationException(((Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).getMessage());
    }
  }

  @Override
  public String getErrorPath() {
    return null;
  }
}
