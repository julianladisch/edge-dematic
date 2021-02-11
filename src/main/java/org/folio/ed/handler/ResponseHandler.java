package org.folio.ed.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;

@Component
public class ResponseHandler implements GenericHandler<String> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ResponseHandler.class);

  @Override
  public Object handle(String payload, MessageHeaders messageHeaders) {
    LOGGER.info("Response handler income: {}", payload);
    return null;
  }
}
