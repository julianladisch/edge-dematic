package org.folio.ed.domain.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RetrievalQueueRecord {
  private UUID holdId;
  private String requestStatus;
  private String requestNote;
  private String callNumber;
  private String itemBarcode;
  private String patronName;
  private String patronBarcode;
  private LocalDateTime creationDateTime;
  private LocalDateTime retrievedDateTime;
  private UUID pickupLocation;
}
