package org.folio.ed.controller;

import javax.validation.Valid;

import org.folio.ed.domain.SystemParametersHolder;
import org.folio.spring.FolioExecutionContext;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resource.TenantApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController("folioTenantController")
@RequestMapping(value = "/_/")
@RequiredArgsConstructor
public class TenantController implements TenantApi {

  private final FolioExecutionContext context;
  private final SystemParametersHolder systemParametersHolder;

  @SneakyThrows
  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    systemParametersHolder.setTenantId(context.getTenantId());
    systemParametersHolder.setOkapiUrl(context.getOkapiUrl());
    return ResponseEntity.ok()
      .body("true");
  }

}
