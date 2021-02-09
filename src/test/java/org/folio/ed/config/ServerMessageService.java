package org.folio.ed.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ServerMessageService {
  public static final String HEARTBEAT_MESSAGE = "HM0000120200101121212";
  public static final String TRANSACTION_RESPONSE_MESSAGE = "TR0000120200101121212000";
  public static final String INVENTORY_CONFIRM_MESSAGE = "IC0000120200101121212697685458679  000";

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerMessageService.class);

  private String messageType;

  public String handleIncomingMessage(String payload, Map<String, Object> headers) {
    LOGGER.info("Server service income: {}", payload);
    return "TR".equals(payload.substring(0, 2)) ? null : TRANSACTION_RESPONSE_MESSAGE;
  }

  public String getMessage() {
    switch (messageType) {
      case "IC":
        return INVENTORY_CONFIRM_MESSAGE;
      default:
        return HEARTBEAT_MESSAGE;
    }
  }

  public void setMessageType(String messageType) {
    this.messageType = messageType;
  }
}
