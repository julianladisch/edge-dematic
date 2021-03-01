package org.folio.ed.handler;

import static javax.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

import javax.servlet.http.HttpServletResponse;

import org.folio.ed.error.AuthorizationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestControllerAdvice
public class BasicExceptionHandler {

  @ExceptionHandler(Exception.class)
  public String handleGenericException(Exception exception, HttpServletResponse response) {
    log.error("Exception:", exception);
    response.setStatus(SC_INTERNAL_SERVER_ERROR);
    return "Internal server error";
  }

  @ExceptionHandler(AuthorizationException.class)
  public String handleAuthorizationException(AuthorizationException exception, HttpServletResponse response) {
    log.error("Exception:", exception);
    response.setStatus(SC_FORBIDDEN);
    return exception.getMessage();
  }
}
