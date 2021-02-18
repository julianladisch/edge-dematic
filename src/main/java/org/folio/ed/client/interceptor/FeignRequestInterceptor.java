package org.folio.ed.client.interceptor;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;
import static org.folio.spring.integration.XOkapiHeaders.TOKEN;

import java.util.Collections;

import org.folio.ed.domain.AsyncFolioExecutionContext;
import org.folio.ed.domain.SystemParametersHolder;
import org.folio.ed.service.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.internal.lang3.StringUtils;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.SneakyThrows;

@Component
public class FeignRequestInterceptor implements RequestInterceptor {

  @Autowired
  private FolioExecutionContext folioExecutionContext;
  @Autowired
  private SecurityManagerService securityManagerService;
  @Autowired
  private SystemParametersHolder systemParametersHolder;

  private static final String[] REMOTE_STORAGE_URLS = { "/retrieve", "/configurations", "/accessions", "/retrievals" };

  @SneakyThrows
  @Override
  public void apply(RequestTemplate template) {

    if (isContextNeededToBeRefreshed(template)) {
      refreshFolioContext();
    }

    template.header(TOKEN, Collections.singletonList(folioExecutionContext.getToken()));
    template.header(TENANT, Collections.singletonList(folioExecutionContext.getTenantId()));
  }

  private boolean isContextNeededToBeRefreshed(RequestTemplate template) {
    return StringUtils.startsWithAny(template.url(), REMOTE_STORAGE_URLS);
  }

  private void refreshFolioContext() {
    var systemUserParameters = securityManagerService.getSystemUserParameters(systemParametersHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager
      .beginFolioExecutionContext(new AsyncFolioExecutionContext(systemUserParameters, null));
  }
}
