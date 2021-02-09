package org.folio.ed.service;

import lombok.RequiredArgsConstructor;

import org.folio.ed.client.RemoteStorageClient;
import org.folio.ed.domain.entity.AccessionQueueRecord;
import org.folio.rs.domain.dto.AsrItem;
import org.folio.rs.domain.dto.AsrItems;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PollingService {

  private final RemoteStorageClient remoteStorageClient;

  public AsrItems process(String storageId) {
    var asrItems = new AsrItems();

    asrItems.asrItems(remoteStorageClient.query("storageId=" + storageId)
      .getResult().stream()
      .map(this::mapToAsrItem)
      .collect(Collectors.toList()));

    return asrItems;
  }

  private AsrItem mapToAsrItem(AccessionQueueRecord accessionQueueRecord) {
    AsrItem asrItem = new AsrItem();
    asrItem.setTitle(accessionQueueRecord.getInstanceTitle());
    asrItem.setAuthor(accessionQueueRecord.getInstanceAuthor());
    asrItem.setItemNumber(accessionQueueRecord.getItemBarcode());
    asrItem.setCallNumber(accessionQueueRecord.getCallNumber());
    return asrItem;
  }


}
