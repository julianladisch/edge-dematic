package org.folio.ed.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
