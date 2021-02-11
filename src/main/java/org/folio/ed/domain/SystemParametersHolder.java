package org.folio.ed.domain;

import org.springframework.stereotype.Component;

import lombok.Data;

@Component
@Data
public class SystemParametersHolder {
  private String tenantId;
  private String okapiUrl;
}
