package org.folio.ed.domain.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class FilterData {

  private final Boolean accessioned;
  private final String storageId;
  private final String createDate;
  private final int offset;
  private final int limit;
}
