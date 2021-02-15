package org.folio.ed.client.interceptor;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import java.util.Collections;

import org.folio.ed.domain.AsyncFolioExecutionContext;
import org.folio.ed.domain.SystemParametersHolder;
import org.folio.ed.service.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.springframework.stereotype.Component;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Component
@AllArgsConstructor
public class FeignRequestInterceptor implements RequestInterceptor {
  private final FolioExecutionContext folioExecutionContext;
  private final SecurityManagerService securityManagerService;
  private final SystemParametersHolder systemParametersHolder;

  @SneakyThrows
  @Override
  public void apply(RequestTemplate template) {
    refreshFolioContext();
    template.header(TOKEN, Collections.singletonList(folioExecutionContext.getToken()));
    template.header(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
  }

  private void refreshFolioContext() {
    var systemUserParameters = securityManagerService.getSystemUserParameters(systemParametersHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager
      .beginFolioExecutionContext(new AsyncFolioExecutionContext(systemUserParameters, null));
  }
}
