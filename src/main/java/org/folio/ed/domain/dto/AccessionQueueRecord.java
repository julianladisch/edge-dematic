package org.folio.ed.domain.dto;

import lombok.Data;

@Data
public class AccessionQueueRecord {
  private String id;
  private String itemBarcode;
  private String callNumber;
  private String instanceTitle;
  private String instanceAuthor;
}
