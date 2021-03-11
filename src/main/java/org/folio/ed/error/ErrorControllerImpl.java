package org.folio.ed.error;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ErrorControllerImpl implements ErrorController {

  @GetMapping("/error")
  public String handleGetError(HttpServletRequest request) {
    return processException(request);
  }

  @PostMapping("/error")
  public String handlePostError(HttpServletRequest request) {
    return processException(request);
  }

  private String processException(HttpServletRequest request) {
    if (request.getAttribute(RequestDispatcher.ERROR_EXCEPTION) != null && request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)
      .getClass() == AuthorizationException.class) {
      throw new AuthorizationException(((Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION)).getMessage());
    } else {
      return StringUtils.EMPTY;
    }
  }

  @Override
  public String getErrorPath() {
    return null;
  }
}
