package org.folio.ed.support;

public class ServerMessageHelper {
  public static final String HEARTBEAT_MESSAGE = "HM0000120200101121212";
  public static final String INVENTORY_CONFIRM_MESSAGE = "IC0000120200101121212697685458679  000";

  private String messageType;

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
