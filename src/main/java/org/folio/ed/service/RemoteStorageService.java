package org.folio.ed.service;

import lombok.RequiredArgsConstructor;

import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.domain.dto.ItemBarcodeRequest;
import org.folio.ed.domain.entity.AccessionQueueRecord;
import org.folio.ed.domain.entity.RetrievalQueueRecord;
import org.folio.rs.domain.dto.AsrItem;
import org.folio.rs.domain.dto.AsrItems;
import org.folio.rs.domain.dto.AsrRequest;
import org.folio.rs.domain.dto.AsrRequests;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {

  private final RemoteStorageClient remoteStorageClient;

  public AsrItems getItems(String storageId) {
    var asrItems = new AsrItems();
    asrItems.asrItems(remoteStorageClient.getAccessionsByQuery(buildQueryByStorageId(storageId))
      .getResult().stream()
      .map(this::mapToAsrItem)
      .collect(Collectors.toList()));
    return asrItems;
  }

  @Async
  public void setAccessionedAsync(String itemBarcode)  {
    remoteStorageClient.setAccessionedByBarcode(itemBarcode);
  }

  @Async
  public void setRetrievedAsync(String itemBarcode)  {
    remoteStorageClient.setRetrievalByBarcode(itemBarcode);
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId) {
    var asrRequests = new AsrRequests();
    asrRequests.asrRequests(remoteStorageClient.getRetrievalsByQuery(buildQueryByStorageId(remoteStorageConfigurationId))
      .getResult().stream()
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
