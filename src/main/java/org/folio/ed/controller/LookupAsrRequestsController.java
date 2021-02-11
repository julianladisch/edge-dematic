package org.folio.ed.controller;

import org.folio.ed.service.RemoteStorageService;
import org.folio.rs.domain.dto.AsrRequests;
import org.folio.rs.rest.resource.LookupAsrRequestsApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class LookupAsrRequestsController implements LookupAsrRequestsApi {

  private final RemoteStorageService remoteStorageService;

  @Override
  public ResponseEntity<AsrRequests> getAsrRequests(
      @ApiParam(required = true) @PathVariable("remoteStorageConfigurationId") String remoteStorageConfigurationId) {
    var asrRequests = remoteStorageService.getRequests(remoteStorageConfigurationId);
    try {
      return new ResponseEntity<>(asrRequests, HttpStatus.OK);
    } finally {
      asrRequests.getAsrRequests()
        .forEach(x -> remoteStorageService.setRetrievedAsync(x.getItemBarcode()));
    }
  }
}
