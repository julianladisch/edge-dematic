package org.folio.ed.controller;

import javax.validation.Valid;
import liquibase.exception.LiquibaseException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.folio.ed.domain.TenantHolder;
import org.folio.ed.service.SecurityManagerService;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.liquibase.FolioSpringLiquibase;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.tenant.rest.resource.TenantApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController("folioTenantController")
@RequestMapping(value = "/_/")
@RequiredArgsConstructor
public class TenantController implements TenantApi {

  public static final String PARAMETER_LOAD_SAMPLE = "loadSample";

  private final FolioSpringLiquibase folioSpringLiquibase;
  private final FolioExecutionContext context;
  private final SecurityManagerService securityManagerService;
  private final TenantHolder tenantHolder;

  public static final String SYSTEM_USER = "system-user";


  @SneakyThrows
  @Override
  public ResponseEntity<String> postTenant(@Valid TenantAttributes tenantAttributes) {
    var tenantId = context.getTenantId();

    if (folioSpringLiquibase != null) {

      var schemaName = context.getFolioModuleMetadata()
        .getDBSchemaName(tenantId);

      folioSpringLiquibase.setDefaultSchema(schemaName);
      try {
        folioSpringLiquibase.performLiquibaseUpdate();
      } catch (LiquibaseException e) {
        e.printStackTrace();
        log.error("Liquibase error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Liquibase error: " + e.getMessage());
      }
    }

    try {
      securityManagerService.prepareSystemUser(SYSTEM_USER, SYSTEM_USER, context.getOkapiUrl(), tenantId);
      tenantHolder.setTenantId(tenantId);
    } catch (Exception e) {
      log.error("Error initializing System User", e);
    }

    return ResponseEntity.ok()
      .body("true");
  }


}
