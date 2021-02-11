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

  private final RemoteStorageClient remoteStorageClient;

  public AsrItems getItems(String storageId) {
    var asrItems = new AsrItems();
    asrItems.asrItems(remoteStorageClient.getAccessionsByQuery(buildQueryByStorageId(storageId))
      .getResult()
      .stream()
      .map(this::mapToAsrItem)
      .collect(Collectors.toList()));
    return asrItems;
  }

  @Async
  public ResponseEntity<String> setAccessionedAsync(String itemBarcode) {
    return remoteStorageClient.setAccessionedByBarcode(itemBarcode);
  }

  @Async
  public ResponseEntity<String> setRetrievedAsync(String itemBarcode) {
    return remoteStorageClient.setRetrievalByBarcode(itemBarcode);
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId) {
    var asrRequests = new AsrRequests();
    asrRequests.asrRequests(remoteStorageClient.getRetrievalsByQuery(buildQueryByStorageId(remoteStorageConfigurationId))
      .getResult()
      .stream()
      .map(this::mapToAsrRequest)
      .collect(Collectors.toList()));
    return asrRequests;
  }

  private String buildQueryByStorageId(String storageId) {
    return "storageId=" + storageId;
  }

  private AsrItem mapToAsrItem(AccessionQueueRecord accessionQueueRecord) {
    AsrItem asrItem = new AsrItem();
    asrItem.setTitle(accessionQueueRecord.getInstanceTitle());
    asrItem.setAuthor(accessionQueueRecord.getInstanceAuthor());
    asrItem.setItemNumber(accessionQueueRecord.getItemBarcode());
    asrItem.setCallNumber(accessionQueueRecord.getCallNumber());
    return asrItem;
  }

  private AsrRequest mapToAsrRequest(RetrievalQueueRecord retrievalQueueRecord) {
    var asrRequest = new AsrRequest();
    asrRequest.setHoldId(retrievalQueueRecord.getHoldId());
//    asrRequest.setAuthor(retrievalQueueRecord.get);
    return asrRequest;
  }

}
