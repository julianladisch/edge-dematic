package org.folio.ed.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessionQueueRecord {
  private String itemBarcode;
  private String callNumber;
  private String instanceTitle;
  private String instanceAuthor;
}
