package org.folio.ed.config;

import java.lang.reflect.Method;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {
    log.error(ExceptionUtils.getStackTrace(throwable));
  }
}
