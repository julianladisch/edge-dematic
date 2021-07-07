package org.folio.ed.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuration {
  private String id;
  private String name;
  private String providerName;
  private String url;
  private String statusUrl;
  private Integer accessionDelay;
  private String accessionTimeUnit;

  @JsonIgnore
  private String tenantId;
}
