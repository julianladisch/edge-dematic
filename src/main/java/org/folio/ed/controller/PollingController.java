package org.folio.ed.controller;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.ed.service.AsyncRemoteStorageService;
import org.folio.ed.service.PollingService;
import org.folio.rs.domain.dto.AsrItems;
import org.folio.rs.rest.resource.LookupNewAsrItemsApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class PollingController implements LookupNewAsrItemsApi {

  private final PollingService pollingService;
  private final AsyncRemoteStorageService asyncRemoteStorageService;


  @Override
  public ResponseEntity<AsrItems> getConfigurations(String storageId) {
    var asrItems = pollingService.process(storageId);
    try {
      return new ResponseEntity<>(asrItems, HttpStatus.OK);
    } finally {
      asrItems.getAsrItems().forEach(asyncRemoteStorageService::setAsyncAccession);
    }
  }
}
