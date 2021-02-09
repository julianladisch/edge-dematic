package org.folio.ed.domain.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccessionQueueRecord {

  private String id;

  private String itemBarcode;

  private LocalDateTime createdDateTime;

  private LocalDateTime accessionedDateTime;

  private UUID remoteStorageId;

  private String callNumber;

  private String instanceTitle;

  private String instanceAuthor;
}
