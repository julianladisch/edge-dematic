package org.folio.ed.util;

public enum StagingDirectorErrorCodes {
  SUCCESS("000"),
  INVENTORY_ALREADY_COMMITTED("001"),
  INVENTORY_NOT_IN_DATABASE("002"),
  SKU_NOT_IN_DATABASE("003"),
  INVALID_SKU_FORMAT("006"),
  INVENTORY_IS_AVAILABLE("007"),
  SKU_ALREADY_IN_DATABASE("008"),
  SKU_HAS_INVENTORY("009"),
  INVENTORY_IS_NOT_AVAILABLE("010"),
  SKU_HAS_WORK("011");

  private String value;

  StagingDirectorErrorCodes(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static StagingDirectorErrorCodes fromCode(String code) {
    for(StagingDirectorErrorCodes ec: values()) {
      if (ec.value.equals(code)) {
        return ec;
      }
    }
    throw new IllegalArgumentException("Unknown error code: " + code);
  }
}
