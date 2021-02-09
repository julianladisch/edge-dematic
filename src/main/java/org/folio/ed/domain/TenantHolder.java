package org.folio.ed.domain;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class TenantHolder {
  private String tenantId;
}
