package org.folio.ed.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.converter.AccessionQueueRecordToAsrItemConverter;
import org.folio.ed.converter.RetrievalQueueRecordToAsrRequestConverter;
import org.folio.ed.domain.dto.AccessionQueueRecord;
import org.folio.ed.domain.dto.Configuration;
import org.folio.ed.domain.dto.RetrievalQueueRecord;
import org.folio.ed.domain.request.ItemBarcodeRequest;
import org.folio.rs.domain.dto.AsrItem;
import org.folio.rs.domain.dto.AsrItems;
import org.folio.rs.domain.dto.AsrRequest;
import org.folio.rs.domain.dto.AsrRequests;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RemoteStorageService {
  private static final String STAGING_DIRECTOR_NAME = "Dematic_SD";

  private final RemoteStorageClient remoteStorageClient;
  private final AccessionQueueRecordToAsrItemConverter accessionQueueRecordToAsrItemConverter;
  private final RetrievalQueueRecordToAsrRequestConverter retrievalQueueRecordToAsrRequestConverter;

  public List<AccessionQueueRecord> getAccessionQueueRecords(String storageId) {
    return remoteStorageClient.getAccessionsByQuery("storageId=" + storageId + "&accessioned=false")
      .getResult();
  }

  public void setAccessionedByBarcode(String barcode) {
    remoteStorageClient.setAccessionedByBarcode(barcode);
  }

  public List<Configuration> getStagingDirectorConfigurations() {
    return remoteStorageClient.getStorageConfigurations()
      .getConfigurations()
      .stream()
      .filter(configuration -> STAGING_DIRECTOR_NAME.equals(configuration.getProviderName()))
      .collect(Collectors.toList());
  }

  public AsrItems getAsrItems(String storageId) {
    var asrItems = new AsrItems();
    asrItems.asrItems(getAccessionQueueRecords(storageId)
      .stream()
      .map(accessionQueueRecordToAsrItemConverter::convert)
      .collect(Collectors.toList()));
    return asrItems;
  }

  public ResponseEntity<String> checkInItemByBarcode(String remoteStorageConfigurationId, String itemBarcode) {
    var itemBarcodeRequest = new ItemBarcodeRequest();
    itemBarcodeRequest.setItemBarcode(itemBarcode);
    return remoteStorageClient.checkInItem(remoteStorageConfigurationId, itemBarcodeRequest);
  }

  public AsrRequests getRequests(String remoteStorageConfigurationId) {
    var asrRequests = new AsrRequests();
    asrRequests.asrRequests(remoteStorageClient.getRetrievalsByQuery("storageId=" + remoteStorageConfigurationId + "&retrieved=false")
      .getResult()
      .stream()
      .map(retrievalQueueRecordToAsrRequestConverter::convert)
      .collect(Collectors.toList()));
    return asrRequests;
  }

  @Async
  public ResponseEntity<String> setAccessionedAsync(String itemBarcode) {
    return remoteStorageClient.setAccessionedByBarcode(itemBarcode);
  }

  @Async
  public ResponseEntity<String> setRetrievedAsync(String itemBarcode) {
    return remoteStorageClient.setRetrievalByBarcode(itemBarcode);
  }
}
