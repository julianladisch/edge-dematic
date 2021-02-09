package org.folio.ed.service;

import lombok.RequiredArgsConstructor;
import org.folio.ed.client.RemoteStorageClient;
import org.folio.rs.domain.dto.AsrItem;
import org.folio.rs.domain.dto.AsrItems;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AsyncRemoteStorageService {

  private final RemoteStorageClient remoteStorageClient;

  @Async
  public void setAsyncAccession(AsrItem asrItem)  {
    remoteStorageClient.setAccessionedByBarcode(asrItem.getItemNumber());
  }
}
