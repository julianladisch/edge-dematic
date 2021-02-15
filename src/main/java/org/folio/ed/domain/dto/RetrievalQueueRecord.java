package org.folio.ed.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrievalQueueRecord {
  private String holdId;
  private String itemBarcode;
  private String instanceTitle;
  private String instanceAuthor;
  private String callNumber;
  private String patronBarcode;
  private String patronName;
  private LocalDateTime retrievedDateTime;
  private String pickupLocation;
  private String requestStatus;
  private String requestNote;
  private LocalDateTime createdDateTime;
  private UUID remoteStorageId;
}
