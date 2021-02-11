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
public class AccessionQueueRecord {

  private String itemBarcode;

  private LocalDateTime createdDateTime;

  private LocalDateTime accessionedDateTime;

  private UUID remoteStorageId;

  private String callNumber;

  private String instanceTitle;

  private String instanceAuthor;
}
