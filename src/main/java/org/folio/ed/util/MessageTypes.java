package org.folio.ed.util;

public enum MessageTypes {
  DELETE_CONFIRM("DC"),
  STATUS_CHECK("SC"),
  INVENTORY_ADD("IA"),
  INVENTORY_CONFIRM("IC"),
  INVENTORY_DELETE("ID"),
  ITEM_RETURNED("IR"),
  PICK_REQUEST("PR"),
  EXPECTED_STORE("ES"),
  HEARTBEAT("HM"),
  TRANSACTION_RESPONSE("TR");

  private String code;

  MessageTypes(String code) {
    this.code = code;
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
}
