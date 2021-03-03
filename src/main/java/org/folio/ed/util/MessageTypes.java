package org.folio.ed.util;

public enum MessageTypes {
  STATUS_CHECK("SC", 33),
  STATUS_MESSAGE("SM", 36),
  INVENTORY_ADD("IA", 153),
  INVENTORY_CONFIRM("IC", 36),
  ITEM_RETURNED("IR", 36),
  PICK_REQUEST("PR", 220),
  HEARTBEAT("HM", 19),
  TRANSACTION_RESPONSE("TR", 22);

  private String code;
  private int payloadLength;

  MessageTypes(String code, int payloadLength) {
    this.code = code;
    this.payloadLength = payloadLength;
  }

  public static MessageTypes fromCode(String code) {
    for(MessageTypes mt: values()) {
      if (mt.getCode().equalsIgnoreCase(code)) {
        return mt;
      }
    }
    throw new IllegalArgumentException("Unknown message type: " + code);
  }

  public String getCode() {
    return code;
  }

  public int getPayloadLength() {
    return payloadLength;
  }
}
