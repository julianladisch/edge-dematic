package org.folio.ed.controller;

import javax.validation.Valid;

import org.folio.ed.service.RemoteStorageService;
import org.folio.ed.domain.dto.UpdateAsrItem;
import org.folio.ed.rest.resource.UpdateASRItemStatusAvailableApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping(value = "/asrService/asr/")
public class UpdateASRItemStatusAvailableController implements UpdateASRItemStatusAvailableApi {

  private final RemoteStorageService remoteStorageService;

  @Override
  public ResponseEntity<Void> updateAsrItemReturn(
      @ApiParam(required = true) @PathVariable("remoteStorageConfigurationId") String remoteStorageConfigurationId,
      @ApiParam(required = true) @RequestHeader(value = "x-okapi-token") String xOkapiToken,
      @ApiParam(required = true) @RequestHeader(value = "x-okapi-tenant") String xOkapiTenant,
      @ApiParam(required = true) @Valid @RequestBody UpdateAsrItem updateAsrItem) {
    remoteStorageService.returnItemByBarcode(remoteStorageConfigurationId, updateAsrItem.getItemBarcode(), xOkapiTenant,
        xOkapiToken);
    return new ResponseEntity<>(HttpStatus.CREATED);
  }
}
