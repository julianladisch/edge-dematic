package org.folio.ed.controller;

import org.folio.ed.service.RemoteStorageService;
import org.folio.rs.domain.dto.AsrItems;
import org.folio.rs.rest.resource.LookupNewAsrItemsApi;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class LookupNewAsrItemsController implements LookupNewAsrItemsApi {

  private final RemoteStorageService remoteStorageService;

  @Override
  public ResponseEntity<AsrItems> getAsrItems(String remoteStorageConfigurationId) {

    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_XML);

    var asrItems = remoteStorageService.getAsrItems(remoteStorageConfigurationId);
    try {
      return new ResponseEntity<>(asrItems, headers, HttpStatus.OK);
    } finally {
      asrItems.getAsrItems()
        .forEach(asrItem -> remoteStorageService.setAccessionedAsync(asrItem.getItemNumber()));
    }
  }
}
