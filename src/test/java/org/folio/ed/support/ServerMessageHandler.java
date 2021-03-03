package org.folio.ed.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.handler.GenericHandler;
import org.springframework.messaging.MessageHeaders;

public class ServerMessageHandler implements GenericHandler<String> {
  public static final String TRANSACTION_RESPONSE_MESSAGE = "TR0000120200101121212000";

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMessageHandler.class);

  @Override
  public String handle(String payload, MessageHeaders messageHeaders) {
    LOGGER.info("Server message handler income: {}", payload);
    return "TR".equals(payload.substring(0, 2)) ? null : TRANSACTION_RESPONSE_MESSAGE;
  }
}
