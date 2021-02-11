package org.folio.ed.service;

import lombok.RequiredArgsConstructor;
import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.domain.AsyncFolioExecutionContext;
import org.folio.ed.domain.TenantHolder;
import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configuration;
import org.folio.spring.scope.FolioExecutionScopeExecutionContextManager;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {
  private static final String STAGING_DIRECTOR_NAME = "Dematic_SD";

  private final RemoteStorageClient remoteStorageClient;
  private final SecurityManagerService securityManagerService;
  private final TenantHolder tenantHolder;

  public List<AccessionQueueRecord> getAccessionQueueRecords(String storageId) {
    var systemUserParameters = securityManagerService.getSystemUserParameters(tenantHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
      new AsyncFolioExecutionContext(systemUserParameters, null));
    return remoteStorageClient.query("storageId==" + storageId).getResult();
  }

  public void setAccessionedByBarcode(String barcode) {
    var systemUserParameters = securityManagerService.getSystemUserParameters(tenantHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
      new AsyncFolioExecutionContext(systemUserParameters, null));
    remoteStorageClient.setAccessionedByBarcode(barcode);
  }

  public List<Configuration> getStagingDirectorConfigurations() {
    var systemUserParameters = securityManagerService.getSystemUserParameters(tenantHolder.getTenantId());
    FolioExecutionScopeExecutionContextManager.beginFolioExecutionContext(
      new AsyncFolioExecutionContext(systemUserParameters, null));
    return remoteStorageClient.getStorageConfigurations().getConfigurations().stream()
      .filter(configuration -> STAGING_DIRECTOR_NAME.equals(configuration.getProviderName()))
      .collect(Collectors.toList());
  }
}
