package org.folio.ed.client.interceptor;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.folio.ed.domain.TenantHolder;
import org.folio.ed.service.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {

  private final FolioExecutionContext folioExecutionContext;
  private final SecurityManagerService securityManagerService;
  private final TenantHolder tenantHolder;

  @SneakyThrows
  @Override
  public void apply(RequestTemplate template) {
    template.header(TOKEN, Collections.singletonList(folioExecutionContext.getToken()));
    template.header(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
  }
}
